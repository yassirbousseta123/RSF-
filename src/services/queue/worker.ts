import { QueueManager, TaskRecord, TaskStatus, TaskType } from './queueManager';
import pool from '../../db'; // Use the shared pool
import { broadcastTaskUpdate } from '../../websocket'; // Adjust path if needed
// Assume these services exist and have an execute method
// Adjust paths and method names as needed
// import { ImportService } from '../import/importService'; // Example
// import { PreOptimizationService } from '../preOptimization/preOptimizationService'; // Example
// import { executePreOptimization } from '../preOptimization/executionEngine'; // Or directly use functions

// --- Configuration ---
const POLLING_INTERVAL_MS = 5000; // Check for new tasks every 5 seconds
const MAX_CONCURRENT_TASKS = 1; // Process one task at a time as requested
const CANCELLATION_CHECK_INTERVAL_MS = 2000; // How often to check for cancellation within a task

let currentTasks = 0; // Track number of currently running tasks
let shuttingDown = false; // Flag to indicate graceful shutdown

// --- Initialize Services ---
const queueManager = new QueueManager(); // Instantiates with the shared pool
// const importService = new ImportService(pool); // Example instantiation
// const preOptimizationService = new PreOptimizationService(pool); // Example instantiation

// --- Task Execution Logic ---

// Helper function to simulate work and check for cancellation
async function cancellableDelay(ms: number, taskId: string): Promise<boolean> {
    const checkInterval = Math.min(ms, CANCELLATION_CHECK_INTERVAL_MS);
    const endTime = Date.now() + ms;

    while (Date.now() < endTime) {
        const remainingTime = Math.min(checkInterval, endTime - Date.now());
        await new Promise(resolve => setTimeout(resolve, remainingTime));

        // Check cancellation status
        const currentStatus = await queueManager.getTaskStatus(taskId);
        if (currentStatus === TaskStatus.CANCELLED) {
            console.log(`Worker: Detected cancellation for task ${taskId} during delay.`);
            return true; // Indicate cancelled
        }
        // Add check for shuttingDown as well? Optional, depends on desired behavior.
        // if (shuttingDown) return true;
    }
    return false; // Indicate not cancelled
}

async function executeTask(task: TaskRecord): Promise<void> {
    const taskId = task.id; // Store locally for easier access
    console.log(`Worker: Starting task ${taskId} (Type: ${task.type})`);

    // Optional: Broadcast that task is RUNNING (already done by fetchAndStart)
    // broadcastTaskUpdate(task);

    let success = false;
    let wasCancelled = false;
    let errorDetails: string | undefined = undefined;

    try {
        switch (task.type) {
            case TaskType.IMPORT:
                console.log(`Worker: Executing IMPORT task for fileId: ${task.data.fileId}`);
                // TODO: Replace simulation with actual import logic,
                // incorporating cancellation checks within loops or between steps.

                // Example simulation with cancellation checks
                console.log(`Worker [${taskId}]: Simulating step 1...`);
                if (await cancellableDelay(1500, taskId)) { wasCancelled = true; break; }
                 console.log(`Worker [${taskId}]: Simulating step 2...`);
                 if (await cancellableDelay(1500, taskId)) { wasCancelled = true; break; }
                console.log(`Worker [${taskId}]: Import simulation complete.`);
                success = true;
                break;

            case TaskType.PRE_OPTIMIZATION:
                console.log(`Worker: Executing PRE_OPTIMIZATION task for preOptId: ${task.data.preOptimizationId}`);
                // TODO: Replace simulation with actual pre-opt logic,
                // incorporating cancellation checks.

                // Example simulation
                 console.log(`Worker [${taskId}]: Simulating pre-opt step 1...`);
                if (await cancellableDelay(2000, taskId)) { wasCancelled = true; break; }
                 console.log(`Worker [${taskId}]: Simulating pre-opt step 2...`);
                 if (await cancellableDelay(2000, taskId)) { wasCancelled = true; break; }
                console.log(`Worker [${taskId}]: Pre-opt simulation complete.`);
                success = true;
                break;

            default:
                console.warn(`Worker: Unknown task type encountered: ${task.type}`);
                errorDetails = `Unknown task type: ${task.type}`;
                success = false;
        }

        // Update task status based on outcome, only if not cancelled
        if (!wasCancelled) {
            const finalStatus = success ? TaskStatus.COMPLETED : TaskStatus.FAILED;
            await queueManager.updateTaskStatus(
                taskId,
                finalStatus,
                errorDetails // Only relevant for FAILED status
            );
            // Broadcasting is handled by updateTaskStatus now
            console.log(`Worker: Task ${taskId} finished with status: ${finalStatus}`);
        } else {
            // If cancelled, the status was already set by stopTask.
            // We just log that the worker acknowledged the cancellation.
            console.log(`Worker: Task ${taskId} execution stopped due to cancellation.`);
            // Broadcast is handled by stopTask
        }

    } catch (error: any) {
        console.error(`Worker: Error processing task ${taskId}:`, error);
        if (!wasCancelled) { // Avoid double update if cancelled during error handling somehow
             // Update status to FAILED and broadcast (handled by updateTaskStatus)
             await queueManager.updateTaskStatus(taskId, TaskStatus.FAILED, error.message || 'Unknown error');
        }
    } finally {
         currentTasks--; // Decrement task counter regardless of outcome
         triggerCheck(); // Immediately check if we can process another task
    }
}

// --- Polling Loop ---
async function checkQueue() {
    if (shuttingDown) {
        console.log("Worker: Shutting down, not checking queue.");
        return;
    }
     if (currentTasks >= MAX_CONCURRENT_TASKS) {
        // console.log(`Worker: Max concurrent tasks (${MAX_CONCURRENT_TASKS}) reached. Waiting...`);
        return; // Skip checking if already at capacity
    }

    console.log("Worker: Checking for pending tasks...");
    try {
        const task = await queueManager.fetchAndStartHighestPriorityTask();
        if (task) {
            currentTasks++;
            // Execute the task asynchronously without waiting for it to complete
            // This allows the polling interval to continue checking
             executeTask(task).catch(err => {
                 console.error(`Worker: Unhandled error in executeTask wrapper for task ${task?.id}:`, err);
                 // Ensure counter is decremented even if executeTask itself throws unexpectedly
                 currentTasks--;
             });
        } else {
            // console.log("Worker: No pending tasks found.");
        }
    } catch (error) {
        console.error("Worker: Error checking queue:", error);
        // Consider adding backoff logic here if DB errors persist
    }
}

// --- Control Functions ---
let pollIntervalId: NodeJS.Timeout | null = null;

function triggerCheck() {
    // Use setImmediate to check queue ASAP without blocking the event loop heavily
    // or causing deep stack traces if tasks complete very quickly.
    setImmediate(checkQueue);
}

export function startWorker() {
    if (pollIntervalId) {
        console.warn("Worker: Already started.");
        return;
    }
    shuttingDown = false;
    currentTasks = 0;
    console.log(`Worker: Starting polling every ${POLLING_INTERVAL_MS}ms.`);
    // Initial check
    triggerCheck();
    // Set up regular polling interval
    pollIntervalId = setInterval(checkQueue, POLLING_INTERVAL_MS);
}

export function stopWorker() {
    if (!pollIntervalId) {
        console.warn("Worker: Not running.");
        return;
    }
    shuttingDown = true;
    console.log("Worker: Stopping polling...");
    clearInterval(pollIntervalId);
    pollIntervalId = null;
    console.log("Worker: Polling stopped. Will finish processing current tasks.");
    // Note: This doesn't actively cancel running tasks, just stops picking up new ones.
    // Cancellation would require more complex logic (e.g., AbortController).
}

// --- Optional: Graceful Shutdown ---
// Handle process termination signals
process.on('SIGTERM', () => {
    console.log('Worker: SIGTERM signal received. Initiating graceful shutdown...');
    stopWorker();
    // Add a timeout to allow tasks to finish, then exit
    setTimeout(() => {
        console.log(`Worker: Exiting after grace period. ${currentTasks} tasks potentially unfinished.`);
        process.exit(0);
    }, 10000); // Example: 10 second grace period
});

process.on('SIGINT', () => {
     console.log('Worker: SIGINT signal received. Initiating graceful shutdown...');
    stopWorker();
     setTimeout(() => {
        console.log(`Worker: Exiting after grace period. ${currentTasks} tasks potentially unfinished.`);
        process.exit(0);
    }, 5000); // Shorter grace period for Ctrl+C
});

// --- Example Initialization (if run as a standalone process) ---
// if (require.main === module) {
//     console.log("Worker: Starting as main process.");
//     startWorker();
// }

// Typically, you would import `startWorker` and call it from your main application entry point (e.g., server.ts or app.ts) 