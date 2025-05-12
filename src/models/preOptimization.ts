import { Pool, QueryResult } from 'pg'; // Assuming 'pg' library and pool is configured elsewhere
import { v4 as uuidv4 } from 'uuid';

// Define the database table name for consistency
const TABLE_NAME = 'pre_optimizations';

/**
 * Represents a Pre-optimization definition.
 * Pre-optimizations define rules applied to RSF files within a specific date range.
 * Instances of this class correspond to rows in the 'pre_optimizations' table.
 *
 * Note: Per requirements, created pre-optimizations cannot be modified or deleted.
 * Database constraints should enforce this where possible.
 */
export class PreOptimization {
    id: string; // UUID primary key
    type: string; // e.g., "FIDES"
    startDate: Date;
    endDate: Date;
    createdAt: Date;
    createdBy: string; // User ID of the creator

    /**
     * Creates an instance of PreOptimization.
     * @param data - Initialization data for the pre-optimization.
     * @throws {Error} - If the end date is not after the start date.
     */
    constructor(data: {
        id?: string; // Optional: Provide if reconstructing from DB
        type: string;
        startDate: Date;
        endDate: Date;
        createdAt?: Date; // Optional: Provide if reconstructing from DB
        createdBy: string;
    }) {
        if (data.endDate <= data.startDate) {
            throw new Error("Validation Error: End date must be after start date.");
        }
        this.id = data.id ?? uuidv4();
        this.type = data.type;
        this.startDate = data.startDate;
        this.endDate = data.endDate;
        this.createdAt = data.createdAt ?? new Date();
        this.createdBy = data.createdBy;
    }

    /**
     * Validates that a given date range for a specific pre-optimization type
     * does not overlap with any existing entries in the database.
     *
     * This static method should be called *before* attempting to save a new
     * PreOptimization instance.
     *
     * @param dbPool - The PostgreSQL connection pool.
     * @param type - The type of the pre-optimization (e.g., "FIDES").
     * @param startDate - The proposed start date.
     * @param endDate - The proposed end date.
     * @param excludeId - Optional: A pre-optimization ID to exclude from the overlap check.
     *                    This is typically used when checking validity for an update scenario,
     *                    although updates are disallowed by current requirements.
     * @returns {Promise<boolean>} - Resolves to `true` if the range is valid (no overlap),
     *                               `false` otherwise.
     * @throws {Error} - If there is a database query error or if endDate <= startDate.
     */
    static async validateDateRangeOverlap(
        dbPool: Pool,
        type: string,
        startDate: Date,
        endDate: Date,
        excludeId?: string
    ): Promise<boolean> {
        // Re-validate date order for robustness of the static method
        if (endDate <= startDate) {
            throw new Error("Validation Error: End date must be after start date.");
        }

        // Use PostgreSQL's range types and the overlap operator (&&) for efficient checking.
        // Ensure the 'pre_optimizations' table has start_date and end_date columns
        // suitable for creating a tsrange, and potentially a GIST index for performance.
        // Example GIST index: CREATE INDEX idx_preopt_type_date_overlap ON pre_optimizations USING gist (type, tsrange(start_date, end_date));
        const query = `
            SELECT 1
            FROM ${TABLE_NAME}
            WHERE type = $1                                     -- Match the type
              AND tsrange(start_date, end_date, '[]') && tsrange($2, $3, '[]') -- Check for overlap (inclusive bounds '[]')
              ${excludeId ? 'AND id != $4' : ''}                -- Optionally exclude a specific record
            LIMIT 1;                                            -- We only need to know if at least one overlap exists
        `;

        const values: any[] = [type, startDate, endDate];
        if (excludeId) {
            values.push(excludeId);
        }

        try {
            const result: QueryResult = await dbPool.query(query, values);
            // If rowCount is 0, there is NO overlap, hence the range is valid.
            return result.rowCount === 0;
        } catch (error) {
            console.error('Database error during date range overlap validation:', error);
            // Rethrowing allows calling code to handle DB errors appropriately
            throw new Error('Database error during overlap validation.');
        }
    }

    /**
     * Saves the current PreOptimization instance to the database.
     * It first validates the date range to prevent overlaps.
     * Assumes the instance represents a *new* pre-optimization as modifications are disallowed.
     *
     * @param dbPool - The PostgreSQL connection pool.
     * @throws {Error} - If the date range overlaps with an existing record,
     *                   or if a database error occurs during validation or insertion.
     */
    async save(dbPool: Pool): Promise<void> {
        // Validate overlap before attempting to insert
        const isValidRange = await PreOptimization.validateDateRangeOverlap(
            dbPool,
            this.type,
            this.startDate,
            this.endDate
            // No excludeId needed for new records
        );

        if (!isValidRange) {
            throw new Error(`Operation failed: A pre-optimization of type '${this.type}' already exists which overlaps with the specified date range [${this.startDate.toISOString()}, ${this.endDate.toISOString()}].`);
        }

        // Proceed with insertion if validation passes
        const insertQuery = `
            INSERT INTO ${TABLE_NAME} (id, type, start_date, end_date, created_at, created_by)
            VALUES ($1, $2, $3, $4, $5, $6);
            -- Consider adding ON CONFLICT DO NOTHING or specific error handling
            -- if concurrent creation attempts are possible, though validation helps prevent this.
        `;

        const values = [
            this.id,
            this.type,
            this.startDate,
            this.endDate,
            this.createdAt,
            this.createdBy,
        ];

        try {
            await dbPool.query(insertQuery, values);
            console.log(`PreOptimization ${this.id} of type ${this.type} saved successfully.`);
        } catch (error) {
            console.error('Error saving pre-optimization to database:', error);
            // Check for specific DB errors if needed (e.g., constraint violations)
            throw new Error('Database error occurred while saving pre-optimization.');
        }
    }

     /**
      * Fetches a single pre-optimization record by its UUID.
      * @param dbPool - The PostgreSQL connection pool.
      * @param id - The UUID of the pre-optimization to find.
      * @returns {Promise<PreOptimization | null>} - A PreOptimization instance if found, otherwise null.
      * @throws {Error} - If a database error occurs.
      */
    static async findById(dbPool: Pool, id: string): Promise<PreOptimization | null> {
        const query = `SELECT id, type, start_date, end_date, created_at, created_by FROM ${TABLE_NAME} WHERE id = $1;`;
        try {
            const result = await dbPool.query(query, [id]);
            if (result.rowCount === 0) {
                return null; // Not found
            }
            // Map database row (snake_case) to constructor properties (camelCase)
            const row = result.rows[0];
            return new PreOptimization({
                id: row.id,
                type: row.type,
                startDate: row.start_date, // Ensure correct Date object conversion if needed
                endDate: row.end_date,     // Ensure correct Date object conversion if needed
                createdAt: row.created_at, // Ensure correct Date object conversion if needed
                createdBy: row.created_by,
            });
        } catch (error) {
            console.error(`Error fetching pre-optimization by ID ${id}:`, error);
            throw new Error('Database error occurred while fetching pre-optimization.');
        }
    }

     /**
      * Fetches all pre-optimization records from the database.
      * Consider adding pagination parameters for large datasets in a real application.
      * @param dbPool - The PostgreSQL connection pool.
      * @returns {Promise<PreOptimization[]>} - An array of PreOptimization instances.
      * @throws {Error} - If a database error occurs.
      */
    static async findAll(dbPool: Pool): Promise<PreOptimization[]> {
        const query = `SELECT id, type, start_date, end_date, created_at, created_by FROM ${TABLE_NAME} ORDER BY created_at DESC;`; // Example ordering
        try {
            const result = await dbPool.query(query);
            return result.rows.map(row => new PreOptimization({
                id: row.id,
                type: row.type,
                startDate: row.start_date,
                endDate: row.end_date,
                createdAt: row.created_at,
                createdBy: row.created_by,
            }));
        } catch (error) {
            console.error('Error fetching all pre-optimizations:', error);
            throw new Error('Database error occurred while fetching all pre-optimizations.');
        }
    }

    // Note: No 'update' or 'delete' methods are provided as per the requirement
    // that pre-optimizations cannot be modified or deleted after creation.
    // Database-level constraints (e.g., triggers disallowing UPDATE/DELETE)
    // are recommended for robust enforcement.
}

// --- Database Schema Assumption ---
// CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- If using uuid_generate_v4() in DB
//
// CREATE TABLE pre_optimizations (
//     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
//     type VARCHAR(255) NOT NULL,
//     start_date TIMESTAMP WITH TIME ZONE NOT NULL,
//     end_date TIMESTAMP WITH TIME ZONE NOT NULL,
//     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
//     created_by VARCHAR(255) NOT NULL, -- Assuming user ID is a string
//     CHECK (end_date > start_date), -- Basic date check
//     -- Enforce uniqueness of type and non-overlapping date ranges
//     CONSTRAINT pre_optimizations_type_overlap_excl EXCLUDE USING GIST (
//         type WITH =,
//         tsrange(start_date, end_date) WITH &&
//     )
// );
// CREATE INDEX idx_preopt_type ON pre_optimizations (type);
// CREATE INDEX idx_preopt_created_at ON pre_optimizations (created_at DESC);

// --- Usage Example ---
/*
import { Pool } from 'pg';

async function exampleUsage() {
    // Assume pool is configured and connected
    const pool = new Pool({ ...dbConfig });

    try {
        const startDate = new Date('2024-01-01T00:00:00Z');
        const endDate = new Date('2024-01-31T23:59:59Z');
        const type = 'FIDES';
        const createdBy = 'user-123';

        // 1. Check for overlap before creating
        const isValid = await PreOptimization.validateDateRangeOverlap(pool, type, startDate, endDate);

        if (isValid) {
            // 2. Create instance
            const newOpt = new PreOptimization({ type, startDate, endDate, createdBy });
            // 3. Save to DB
            await newOpt.save(pool);
            console.log(`Created pre-optimization ${newOpt.id}`);

            // 4. Fetch it back
            const fetchedOpt = await PreOptimization.findById(pool, newOpt.id);
            console.log('Fetched:', fetchedOpt);

        } else {
            console.log(`Cannot create pre-optimization: Overlap detected for type ${type} in range ${startDate.toISOString()} - ${endDate.toISOString()}.`);
        }

        // 5. Fetch all
        const allOpts = await PreOptimization.findAll(pool);
        console.log('All PreOptimizations:', allOpts.length);

    } catch (error) {
        console.error('Example usage failed:', error);
    } finally {
        await pool.end(); // Close the pool connection
    }
}

// exampleUsage();
*/ 