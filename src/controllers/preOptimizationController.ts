import express, { Request, Response, Router } from 'express';
import { Pool } from 'pg';
// Assume authentication and authorization middleware exist
// Replace with your actual middleware imports
// import { checkAuth, checkRole } from '../middleware/authMiddleware'; // Placeholder - Uncomment and fix path

// Assume a configured pg Pool is available, potentially via dependency injection or a shared module
// Example: import { pool } from '../db/pool';
declare const pool: Pool; // Placeholder for actual pool import/injection

const router: Router = express.Router();
const LOGS_TABLE_NAME = 'pre_optimization_logs'; // Consistent table name

/**
 * GET /api/pre-optimizations/:id/results
 * Retrieves the execution log history for a specific pre-optimization.
 * Requires authentication and 'consultant' or 'manager' role.
 */
router.get(
    '/:id/results',
    // checkAuth, // 1. TODO: Uncomment and ensure middleware is correctly imported and used
    // checkRole(['consultant', 'manager']), // 2. TODO: Uncomment and ensure middleware is correctly imported and used
    async (req: Request, res: Response) => {
        const { id } = req.params; // Extract pre-optimization ID from URL parameter

        if (!id) {
            return res.status(400).json({ message: 'Pre-optimization ID is required.' });
        }

        try {
            const query = `
                SELECT
                    log_id,
                    pre_optimization_id,
                    start_time,
                    end_time,
                    status,
                    affected_lines,
                    details,
                    created_at
                FROM ${LOGS_TABLE_NAME}
                WHERE pre_optimization_id = $1
                ORDER BY start_time DESC; -- Show most recent attempts first
            `;

            const result = await pool.query(query, [id]);

            if (result.rowCount === 0) {
                // It's valid for an optimization to exist but not have run yet
                return res.status(404).json({ message: `No execution logs found for pre-optimization ID ${id}. It might not have been run yet.` });
            }

            // Return the array of log entries
            // Map snake_case from DB to camelCase if needed, but here we return as-is
            res.status(200).json(result.rows);

        } catch (error: any) {
            console.error(`Error fetching execution logs for pre-optimization ID ${id}:`, error);
            res.status(500).json({ message: 'Failed to retrieve pre-optimization results.', error: error.message });
        }
    }
);

// --- Add other pre-optimization related routes here ---
// Example: POST /api/pre-optimizations to create a new one
// router.post('/', checkAuth, checkRole(['manager']), async (req, res) => { ... });

// Example: POST /api/pre-optimizations/:id/execute to trigger execution
// router.post('/:id/execute', checkAuth, checkRole(['manager']), async (req, res) => { ... });


export default router; 