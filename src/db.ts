import { Pool } from 'pg';
import dotenv from 'dotenv';

// Load environment variables from .env file
dotenv.config();

// Basic configuration - adjust according to your environment variable names
// Ensure these environment variables are set in your .env file or system environment
const pool = new Pool({
    user: process.env.DB_USER || 'postgres', // Fallback for local dev if needed
    host: process.env.DB_HOST || 'localhost',
    database: process.env.DB_NAME || 'rsf_db', // Match your DB name
    password: process.env.DB_PASSWORD || 'postgres', // Be cautious with default passwords
    port: parseInt(process.env.DB_PORT || '5432', 10),
    // Optional Pool settings:
    // max: 20, // max number of clients in the pool
    // idleTimeoutMillis: 30000, // how long a client is allowed to remain idle before being closed
    // connectionTimeoutMillis: 2000, // how long to wait for a connection before timing out
});

// Test the connection (optional but recommended)
pool.connect((err, client, release) => {
    if (err) {
        console.error('Error acquiring database client', err.stack);
        // Consider exiting the process if the DB connection is critical at startup
        process.exit(1);
    } else {
        console.log('Database pool connected successfully.');
        client?.query('SELECT NOW()', (err, result) => {
            release(); // Release the client back to the pool
            if (err) {
                console.error('Error executing initial query', err.stack);
            } else {
                console.log('Initial DB query successful:', result.rows[0]);
            }
        });
    }
});

// Handle pool errors
pool.on('error', (err, client) => {
    console.error('Unexpected error on idle database client', err);
    // Optional: Decide if this requires process termination
    // process.exit(-1);
});

export default pool; 