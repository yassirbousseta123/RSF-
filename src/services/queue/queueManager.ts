import { Pool, QueryResult } from 'pg';
import { v4 as uuidv4 } from 'uuid';
import pool from '../../db'; // Import the shared pool
import { broadcastTaskUpdate } from '../../websocket'; // Import for broadcasting updates

// Define the database table name
const TABLE_NAME = 'task_queue';

// Define task types and statuses as enums or constants for better type safety
export enum TaskType {
    IMPORT = 'IMPORT',
    PRE_OPTIMIZATION = 'PRE_OPTIMIZATION',
    // Add other task types as needed
}

export enum TaskStatus {
    PENDING = 'PENDING',
    RUNNING = 'RUNNING',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED',
    CANCELLED = 'CANCELLED', // Add CANCELLED status
    // Add other statuses as needed
}

// Interface for task data stored in the JSONB column
export interface TaskData {
    fileId?: string; // Example: For IMPORT tasks
    preOptimizationId?: string; // Example: For PRE_OPTIMIZATION tasks
    userId?: string; // User who initiated the task
    // Add other relevant metadata fields
}

// Interface for a task record
export interface TaskRecord {
    id: string; // UUID
    type: TaskType;
    priority: number;
    status: TaskStatus;
    createdAt: Date;
    updatedAt: Date;
    data: TaskData;
}

// Define a simple User interface expected from JWT verification
// Adjust based on your actual JWT payload structure
interface AuthenticatedUser {
    id: string; // Or number, depending on your user ID type
    username: string;
    role: string; // e.g., 'manager', 'consultant'
}

/**
 * Manages the task queue stored in the 'task_queue' PostgreSQL table.
 * Handles adding tasks, fetching the next task to process, updating task statuses, priority, and cancellation.
 */
export class QueueManager {
    private dbPool: Pool;

    /**
     * Creates an instance of QueueManager.
     */
    constructor() {
        this.dbPool = pool; // Use the imported shared pool
        // Consider adding a check here to ensure the task_queue table exists
        // using dbPool.query("SELECT 1 FROM information_schema.tables WHERE table_name = $1", [TABLE_NAME])
    }

    /**
     * Adds a new task to the queue and broadcasts the update.
     *
     * @param type - The type of the task.
     * @param priority - The initial priority of the task.
     * @param data - Task-specific data.
     * @returns {Promise<string>} - The ID of the newly created task.
     * @throws {Error} - If there is a database error.
     */
    async addTask(type: TaskType, priority: number, data: TaskData): Promise<string> {
        const taskId = uuidv4();
        const currentTime = new Date();
        const query = `
            INSERT INTO ${TABLE_NAME} (id, type, priority, status, created_at, updated_at, data)
            VALUES ($1, $2, $3, $4, $5, $6, $7)
            RETURNING id, created_at, updated_at; -- Return timestamps for broadcasting
        `;
        const values = [
            taskId,
            type,
            priority,
            TaskStatus.PENDING,
            currentTime,
            currentTime,
            JSON.stringify(data),
        ];

        try {
            const result: QueryResult = await this.dbPool.query(query, values);
            const newRecord = result.rows[0];
            console.log(`Task ${newRecord.id} of type ${type} added to the queue.`);

            // Broadcast update after successful insertion
            broadcastTaskUpdate({
                id: newRecord.id,
                status: TaskStatus.PENDING,
                type: type,
                priority: priority, // Include priority
                data: data,
                createdAt: newRecord.created_at, // Use actual DB timestamp
                updatedAt: newRecord.updated_at // Use actual DB timestamp
            });

            return newRecord.id;
        } catch (error) {
            console.error('Error adding task to queue:', error);
            throw new Error('Database error occurred while adding task.');
        }
    }

    /**
     * Fetches the highest-priority pending task, updates its status to RUNNING, and broadcasts the update.
     *
     * @returns {Promise<TaskRecord | null>} - The task record if found, otherwise null.
     * @throws {Error} - If there is a database error.
     */
    async fetchAndStartHighestPriorityTask(): Promise<TaskRecord | null> {
        const client = await this.dbPool.connect();
        try {
            await client.query('BEGIN');
            const selectQuery = `
                SELECT id
                FROM ${TABLE_NAME}
                WHERE status = $1
                ORDER BY priority DESC, created_at ASC
                LIMIT 1
                FOR UPDATE SKIP LOCKED;
            `;
            const selectResult = await client.query(selectQuery, [TaskStatus.PENDING]);

            if (selectResult.rowCount === 0) {
                await client.query('COMMIT');
                return null;
            }

            const taskId = selectResult.rows[0].id;
            const currentTime = new Date();
            const updateQuery = `
                UPDATE ${TABLE_NAME}
                SET status = $1, updated_at = $2
                WHERE id = $3 AND status = $4
                RETURNING id, type, priority, status, created_at, updated_at, data;
            `;
            const updateResult = await client.query(updateQuery, [
                TaskStatus.RUNNING,
                currentTime,
                taskId,
                TaskStatus.PENDING,
            ]);

            await client.query('COMMIT');

            if (updateResult.rowCount === 0) {
                console.warn(`Task ${taskId} was locked but couldn't be updated to RUNNING.`);
                return null;
            }

            const task = updateResult.rows[0];
            console.log(`Task ${task.id} fetched and marked as RUNNING.`);

             // Map DB result to TaskRecord interface
            const taskRecord: TaskRecord = {
                id: task.id,
                type: task.type as TaskType,
                priority: task.priority,
                status: task.status as TaskStatus,
                createdAt: task.created_at,
                updatedAt: task.updated_at,
                data: task.data as TaskData,
            };

            // Broadcast the update
            broadcastTaskUpdate(taskRecord);

            return taskRecord;

        } catch (error) {
            await client.query('ROLLBACK');
            console.error('Error fetching or starting task:', error);
            throw new Error('Database error occurred while fetching/starting task.');
        } finally {
            client.release();
        }
    }

    /**
     * Updates the status of a specific task and broadcasts the update.
     *
     * @param taskId - The ID of the task to update.
     * @param status - The new status for the task.
     * @param details - Optional details (currently not stored, consider adding a column).
     * @returns {Promise<void>}
     * @throws {Error} - If the task is not found or a database error occurs.
     */
    async updateTaskStatus(taskId: string, status: TaskStatus, details?: string): Promise<void> {
        const currentTime = new Date();
        // If storing details, add a 'details' column and include it here
        const query = `
            UPDATE ${TABLE_NAME}
            SET status = $1, updated_at = $2
            WHERE id = $3
            RETURNING id, type, priority, status, created_at, updated_at, data; -- Return full record for broadcast
        `;
        const values = [status, currentTime, taskId];

        try {
            const result: QueryResult = await this.dbPool.query(query, values);
            if (result.rowCount === 0) {
                throw new Error(`Task with ID ${taskId} not found.`);
            }
            const updatedTask = result.rows[0];
            console.log(`Task ${taskId} status updated to ${status}.`);

            // Broadcast the update
            broadcastTaskUpdate({
                id: updatedTask.id,
                type: updatedTask.type as TaskType,
                priority: updatedTask.priority,
                status: updatedTask.status as TaskStatus,
                createdAt: updatedTask.created_at,
                updatedAt: updatedTask.updated_at,
                data: updatedTask.data as TaskData, // Include data
            });

        } catch (error: any) {
            console.error(`Error updating status for task ${taskId}:`, error);
            if (error.message.includes('not found')) {
                 throw error;
            }
            throw new Error('Database error occurred while updating task status.');
        }
    }

    /**
     * Sets the priority for a specific task. Only 'manager' role allowed.
     * Broadcasts the update on success.
     *
     * @param taskId - The ID of the task to update.
     * @param newPriority - The new priority value.
     * @param user - The authenticated user performing the action.
     * @returns {Promise<void>}
     * @throws {Error} - If the user is not authorized, the task is not found, or a database error occurs.
     */
    async setTaskPriority(taskId: string, newPriority: number, user: AuthenticatedUser): Promise<void> {
        // 1. Authorization Check
        if (user?.role !== 'manager') {
            console.warn(`Unauthorized attempt to set priority for task ${taskId} by user ${user?.id} (${user?.role})`);
            throw new Error('Authorization Error: Only managers can set task priority.');
        }

        // Validate priority (optional, but good practice)
        if (typeof newPriority !== 'number' || !Number.isInteger(newPriority)) {
             throw new Error('Validation Error: Priority must be an integer.');
        }

        const currentTime = new Date();
        const query = `
            UPDATE ${TABLE_NAME}
            SET priority = $1, updated_at = $2
            WHERE id = $3
            RETURNING id, type, priority, status, created_at, updated_at, data; -- Return full record for broadcast
        `;
        const values = [newPriority, currentTime, taskId];

        try {
             // 2. Database Update
            const result: QueryResult = await this.dbPool.query(query, values);
            if (result.rowCount === 0) {
                throw new Error(`Task with ID ${taskId} not found.`);
            }
            const updatedTask = result.rows[0];
            console.log(`Task ${taskId} priority set to ${newPriority} by manager ${user.id}.`);

            // 3. Broadcast Update
             broadcastTaskUpdate({
                id: updatedTask.id,
                type: updatedTask.type as TaskType,
                priority: updatedTask.priority, // Send the new priority
                status: updatedTask.status as TaskStatus,
                createdAt: updatedTask.created_at,
                updatedAt: updatedTask.updated_at,
                data: updatedTask.data as TaskData,
            });

        } catch (error: any) {
            console.error(`Error setting priority for task ${taskId}:`, error);
             if (error.message.includes('not found') || error.message.includes('Authorization Error') || error.message.includes('Validation Error')) {
                 throw error; // Re-throw specific errors
            }
            throw new Error('Database error occurred while setting task priority.');
        }
    }

    /**
     * Attempts to stop a running task by setting its status to CANCELLED.
     * Only 'manager' role allowed. Broadcasts the update on success.
     * Note: The worker needs to periodically check this status to halt execution.
     *
     * @param taskId - The ID of the task to stop.
     * @param user - The authenticated user performing the action.
     * @returns {Promise<void>}
     * @throws {Error} - If the user is not authorized, the task is not found,
     *                   the task is not running, or a database error occurs.
     */
    async stopTask(taskId: string, user: AuthenticatedUser): Promise<void> {
        // 1. Authorization Check
        if (user?.role !== 'manager') {
            console.warn(`Unauthorized attempt to stop task ${taskId} by user ${user?.id} (${user?.role})`);
            throw new Error('Authorization Error: Only managers can stop tasks.');
        }

        const client = await this.dbPool.connect();
        try {
            await client.query('BEGIN');

            // 2. Fetch current task status and lock row
            const checkQuery = `SELECT status FROM ${TABLE_NAME} WHERE id = $1 FOR UPDATE;`;
            const checkResult = await client.query(checkQuery, [taskId]);

            if (checkResult.rowCount === 0) {
                throw new Error(`Task with ID ${taskId} not found.`);
            }

            const currentStatus = checkResult.rows[0].status as TaskStatus;

            // 3. Check if task is actually running
            if (currentStatus !== TaskStatus.RUNNING) {
                throw new Error(`Task ${taskId} cannot be stopped because it is not running (current status: ${currentStatus}).`);
            }

            // 4. Update status to CANCELLED
            const currentTime = new Date();
            const updateQuery = `
                UPDATE ${TABLE_NAME}
                SET status = $1, updated_at = $2
                WHERE id = $3
                RETURNING id, type, priority, status, created_at, updated_at, data; -- Return full record for broadcast
            `;
            const values = [TaskStatus.CANCELLED, currentTime, taskId];
            const updateResult = await client.query(updateQuery, values);

            await client.query('COMMIT'); // Commit transaction

            const updatedTask = updateResult.rows[0]; // Task is guaranteed to exist here
            console.log(`Task ${taskId} status set to CANCELLED by manager ${user.id}.`);

            // 5. Broadcast Update
            broadcastTaskUpdate({
                id: updatedTask.id,
                type: updatedTask.type as TaskType,
                priority: updatedTask.priority,
                status: updatedTask.status as TaskStatus, // Should be CANCELLED
                createdAt: updatedTask.created_at,
                updatedAt: updatedTask.updated_at,
                data: updatedTask.data as TaskData,
            });

        } catch (error: any) {
            await client.query('ROLLBACK'); // Rollback on any error
            console.error(`Error stopping task ${taskId}:`, error);
            // Re-throw specific user-facing errors
            if (error.message.includes('not found') || error.message.includes('Authorization Error') || error.message.includes('cannot be stopped')) {
                throw error;
            }
            // Throw generic error for other DB issues
            throw new Error('Database error occurred while stopping task.');
        } finally {
            client.release();
        }
    }

    /**
     * Retrieves only the status of a task by its ID.
     * @param taskId - The ID of the task.
     * @returns {Promise<TaskStatus | null>} - The task status or null if not found.
     * @throws {Error} - If a database error occurs.
     */
    async getTaskStatus(taskId: string): Promise<TaskStatus | null> {
        const query = `SELECT status FROM ${TABLE_NAME} WHERE id = $1;`;
        try {
            const result = await this.dbPool.query(query, [taskId]);
            if (result.rowCount === 0) {
                return null; // Not found
            }
            return result.rows[0].status as TaskStatus;
        } catch (error) {
            console.error(`Error fetching status for task ID ${taskId}:`, error);
            throw new Error('Database error occurred while fetching task status.');
        }
    }

    // --- Optional Helper Methods ---

    /**
     * Retrieves a task by its ID.
     * @param taskId - The ID of the task.
     * @returns {Promise<TaskRecord | null>} - The task record or null if not found.
     */
    async getTaskById(taskId: string): Promise<TaskRecord | null> {
         const query = `SELECT id, type, priority, status, created_at, updated_at, data FROM ${TABLE_NAME} WHERE id = $1;`;
         try {
             const result = await this.dbPool.query(query, [taskId]);
             if (result.rowCount === 0) {
                 return null;
             }
             const task = result.rows[0];
             return {
                id: task.id,
                type: task.type as TaskType,
                priority: task.priority,
                status: task.status as TaskStatus,
                createdAt: task.created_at,
                updatedAt: task.updated_at,
                data: task.data as TaskData,
            };
         } catch (error) {
             console.error(`Error fetching task by ID ${taskId}:`, error);
             throw new Error('Database error occurred while fetching task.');
         }
    }

    // --- TODO ---
    // Add methods for:
    // - Querying tasks by status, type, etc. (potentially role-restricted for certain views)
    // - Cleaning up old completed/failed tasks
}

// Example of how you might initialize and use the QueueManager
// This would typically happen in your main application setup or service layer.
/*
import { Pool } from 'pg'; // Assuming Pool is configured

const dbPool = new Pool({
    // ... your database connection configuration ...
    // e.g., user, host, database, password, port
});

const queueManager = new QueueManager(dbPool);

async function exampleUsage() {
    try {
        // Add tasks
        const importTaskId = await queueManager.addTask(TaskType.IMPORT, 10, { fileId: 'file-abc-123', userId: 'user-1' });
        const preOptTaskId = await queueManager.addTask(TaskType.PRE_OPTIMIZATION, 5, { preOptimizationId: 'preopt-def-456', userId: 'user-2' });

        // Process a task
        const taskToProcess = await queueManager.fetchAndStartHighestPriorityTask();

        if (taskToProcess) {
            console.log(`Processing task: ${taskToProcess.id}, Type: ${taskToProcess.type}`);
            // ... perform the actual task work ...
            const success = true; // Replace with actual outcome

            if (success) {
                await queueManager.updateTaskStatus(taskToProcess.id, TaskStatus.COMPLETED);
            } else {
                await queueManager.updateTaskStatus(taskToProcess.id, TaskStatus.FAILED, 'Processing failed due to XYZ');
            }
        } else {
            console.log("No pending tasks to process.");
        }

    } catch (error) {
        console.error("An error occurred:", error);
    } finally {
        // Close the pool when the application shuts down gracefully
        // await dbPool.end();
    }
}

// exampleUsage();
*/ 