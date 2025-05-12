import express from 'express';
import http from 'http';
import cors from 'cors'; // Assuming CORS is needed
import dotenv from 'dotenv';

// Load environment variables early
dotenv.config();

import { initializeWebSocket } from './websocket'; // Adjust path if needed
import { startWorker, stopWorker } from './services/queue/worker'; // Adjust path, import stopWorker
import pool from './db'; // Import pool for graceful shutdown
import preOptimizationController from './controllers/preOptimizationController'; // Adjust path if needed
import queueController from './controllers/queueController'; // Import the new controller

const app = express();

// --- Middleware Setup ---
// Configure CORS - adjust origins as needed for your frontend
app.use(cors({
    origin: ['http://localhost:3000', 'http://localhost:3001'], // Allow your frontend origins
    methods: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allowedHeaders: ["Authorization", "Content-Type"],
    credentials: true
}));

// Body parsing middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// --- API Routes ---
// TODO: Add other controllers as needed (e.g., for login, file upload if they are in Node)
app.use('/api/pre-optimizations', preOptimizationController); // Example existing controller mount
app.use('/api/queue', queueController); // Mount the new queue controller

// Basic root route (optional)
app.get('/', (req, res) => {
    res.send('RSF Queue Service Backend is running');
});

// --- Error Handling Middleware (Optional but recommended) ---
app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
    console.error("Unhandled Error:", err.stack || err);
    res.status(err.status || 500).json({
        message: err.message || 'Internal Server Error',
        // Optionally include stack trace in development
        // stack: process.env.NODE_ENV === 'development' ? err.stack : undefined,
    });
});


// --- Server Initialization ---
const PORT = process.env.NODE_PORT || process.env.PORT || 3001; // Use a specific port for Node backend
const server = http.createServer(app);

// --- Initialize WebSocket Server ---
// Ensure this happens *after* the HTTP server is created
initializeWebSocket(server);

// --- Start Queue Worker ---
// Start the worker process after other initializations
startWorker();

// --- Start HTTP Server ---
server.listen(PORT, () => {
    console.log(`-------------------------------------------------------`);
    console.log(`🚀 RSF Node Backend running on http://localhost:${PORT}`);
    console.log(`🔌 WebSocket server initialized.`);
    console.log(`👷 Queue Worker started.`);
    console.log(`-------------------------------------------------------`);
});

// Handle graceful shutdown for server
const shutdown = (signal: string) => {
    console.log(`\n${signal} received. Shutting down gracefully...`);
    server.close(() => {
        console.log('HTTP server closed.');
        stopWorker(); // Signal the worker to stop polling
        // Close database pool if necessary (pool.end()) - Ensure worker has finished DB ops if needed
        pool.end(() => {
             console.log('Database pool closed.');
             process.exit(0);
        });
        // Give some time for pool/worker to close before forceful exit
         setTimeout(() => {
             console.error('Could not close connections in time, forcefully shutting down');
             process.exit(1);
         }, 15000); // Increased timeout
    });
};

process.on('SIGTERM', () => shutdown('SIGTERM'));
process.on('SIGINT', () => shutdown('SIGINT')); 