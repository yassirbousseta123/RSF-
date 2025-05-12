import express, { Request, Response, Router } from 'express';
import pool from '../db'; // Import the shared database pool
import { TaskStatus } from '../services/queue/queueManager'; // Import enum if needed for filtering/sorting
import { checkAuth, checkRole } from '../middleware/authMiddleware'; // Import actual middleware

// Placeholder for your actual JWT authentication and role-checking middleware
// import { checkAuth, checkRole } from '../middleware/authMiddleware'; // Adjust path as needed
/* Remove placeholder middleware 
const checkAuth = (req: Request, res: Response, next: Function) => {
    console.warn("Using placeholder checkAuth middleware");
    // TODO: Replace with actual JWT verification logic
    // Example: Verify req.headers.authorization, attach user to req.user
    // For testing, you might temporarily attach a mock user:
    // req.user = { id: 'mock-manager-id', username: 'manager', role: 'manager' };
    if (!req.headers.authorization) { // Basic check for presence
         return res.status(401).json({ message: 'Authentication required.' });
    }
    next();
};
const checkRole = (roles: string[]) => {
    return (req: Request, res: Response, next: Function) => {
        console.warn(`Using placeholder checkRole middleware for roles: ${roles.join(', ')}`);
         // TODO: Replace with actual role checking logic based on req.user set by checkAuth
         // For testing with the mock user above:
         // if (!req.user || !roles.includes(req.user.role)) {
         //     return res.status(403).json({ message: 'Forbidden: Insufficient role.' });
         // }
        next();
    };
};
*/
// --- End Placeholder Middleware ---


const router: Router = express.Router();
const TABLE_NAME = 'task_queue';

/**
 * GET /api/queue/status
 * Retrieves a list of all tasks in the queue with essential details.
 * Requires authentication and 'manager' role.
 */
router.get(
    '/status',
    checkAuth, // Apply authentication middleware
    checkRole(['manager']), // Apply role check middleware for 'manager'
    async (req: Request, res: Response) => {
        console.log("GET /api/queue/status requested");
        try {
            // Query to select necessary fields
            // Order by status (e.g., Running, Pending first), then priority, then creation time
            const query = `
                SELECT
                    id,
                    type,
                    priority,
                    status,
                    created_at,
                    updated_at
                FROM ${TABLE_NAME}
                ORDER BY
                    CASE status
                        WHEN '${TaskStatus.RUNNING}' THEN 1
                        WHEN '${TaskStatus.PENDING}' THEN 2
                        WHEN '${TaskStatus.CANCELLED}' THEN 3
                        WHEN '${TaskStatus.FAILED}' THEN 4
                        WHEN '${TaskStatus.COMPLETED}' THEN 5
                        ELSE 6
                    END ASC,
                    priority DESC,
                    created_at ASC;
            `;

            const result = await pool.query(query);

            // Map snake_case to camelCase if desired by frontend (optional)
            // const tasks = result.rows.map(row => ({
            //     taskId: row.id,
            //     taskType: row.type,
            //     taskPriority: row.priority,
            //     taskStatus: row.status,
            //     createdAt: row.created_at,
            //     updatedAt: row.updated_at
            // }));

            res.status(200).json(result.rows); // Return as is for now

        } catch (error: any) {
            console.error('Error fetching queue status:', error);
            res.status(500).json({ message: 'Failed to retrieve queue status.', error: error.message });
        }
    }
);

// Add other queue-related routes here later (e.g., POST for stopTask, PATCH for setTaskPriority)

export default router; 