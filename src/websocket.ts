import { WebSocketServer, WebSocket } from 'ws';
import http from 'http';
import url from 'url'; // Needed to parse query parameters
import jwt, { JwtPayload } from 'jsonwebtoken'; // Import jsonwebtoken and JwtPayload type
import { TaskRecord, TaskStatus } from './services/queue/queueManager'; // Adjust path if needed

// Assume AuthenticatedUser interface matches your JWT payload structure
interface AuthenticatedUser {
    id: string; // Or number
    username: string;
    role: string; // e.g., 'manager', 'consultant'
    // Add other fields from your JWT payload if needed (like iat, exp)
    iat?: number;
    exp?: number;
}

// Extend the WebSocket interface to hold user information
interface WebSocketWithAuth extends WebSocket {
    user?: AuthenticatedUser; // Store authenticated user data
}

let wss: WebSocketServer | null = null;
const JWT_SECRET = process.env.JWT_SECRET; // Ensure JWT_SECRET is in your environment variables

if (!JWT_SECRET) {
    console.error("FATAL ERROR: JWT_SECRET environment variable is not set.");
    process.exit(1); // Exit if secret is missing
}

interface BroadcastMessage {
    type: 'TASK_UPDATE';
    payload: {
        taskId: string;
        status: TaskStatus;
        type?: string;
        priority?: number;
        data?: any;
        createdAt?: Date;
        updatedAt: Date;
        // Note: 'progress' is not currently tracked/included.
    };
}

// Updated authorization check using the authenticated user data
function isAuthorized(ws: WebSocketWithAuth): boolean {
    // Check if user data exists and role is 'manager'
    return ws.user?.role === 'manager';
}

// Helper type guard to check if payload matches AuthenticatedUser
function isUserPayload(payload: string | JwtPayload): payload is AuthenticatedUser {
  return (
    typeof payload === 'object' &&
    payload !== null &&
    typeof (payload as AuthenticatedUser).id !== 'undefined' && // Check existence and potentially type
    typeof (payload as AuthenticatedUser).username === 'string' &&
    typeof (payload as AuthenticatedUser).role === 'string'
  );
}

export function initializeWebSocket(server: http.Server): WebSocketServer {
    if (wss) {
        console.warn("WebSocket server already initialized.");
        return wss;
    }

    console.log("Initializing WebSocket server...");
    wss = new WebSocketServer({ server }); // Attach WebSocket server to the existing HTTP server

    wss.on('connection', (ws: WebSocketWithAuth, req: http.IncomingMessage) => {
        console.log('WebSocket client attempting connection...');

        // 1. Extract Token from Query Parameter
        const queryParams = url.parse(req.url || '', true).query;
        const token = queryParams.token as string | undefined;

        if (!token) {
            console.log('WebSocket connection rejected: No token provided.');
            ws.close(1008, "Token required"); // 1008 = Policy Violation
            return;
        }

        // 2. Verify JWT Token
        try {
            // Assert JWT_SECRET is a string here for type safety, runtime check above ensures it's set
            const decoded = jwt.verify(token, JWT_SECRET as string);

            // Use the type guard to validate the payload structure
            if (isUserPayload(decoded)) {
                // Attach validated user information
                ws.user = decoded;
                console.log(`WebSocket client connected and authenticated as user: ${ws.user.username} (Role: ${ws.user.role})`);

                // Role check (allow manager/consultant to connect)
                if (ws.user.role !== 'manager' && ws.user.role !== 'consultant') {
                    console.log(`WebSocket connection closed: User ${ws.user.username} role (${ws.user.role}) not authorized for monitoring.`);
                    ws.close(1008, "Role not authorized");
                    return;
                }
            } else {
                 // Payload structure is invalid
                 console.log(`WebSocket connection rejected: Invalid token payload structure.`);
                 ws.close(1008, "Invalid token payload");
                 return;
            }

        } catch (err: any) {
            // Handle verification errors (expired, signature mismatch, etc.)
            console.log(`WebSocket connection rejected: Token verification failed. Error: ${err.message}`);
            ws.close(1008, "Invalid token");
            return;
        }

        // 3. Set up message/error/close handlers for authenticated connection
        ws.on('message', (message: Buffer) => {
            console.log(`Received WebSocket message from ${ws.user?.username}:`, message.toString());
        });

        ws.on('close', () => {
            console.log(`WebSocket client disconnected: ${ws.user?.username || 'Unknown'}`);
        });

        ws.on('error', (error) => {
            console.error(`WebSocket error for client ${ws.user?.username || 'Unknown'}:`, error);
        });

    });

     wss.on('error', (error) => {
        console.error('WebSocket Server Error:', error);
    });

    console.log("WebSocket server initialized and attached to HTTP server.");
    return wss;
}

// Broadcast function remains largely the same, but uses WebSocketWithAuth type
export function broadcastTaskUpdate(task: Pick<TaskRecord, 'id' | 'status' | 'type' | 'priority' | 'data' | 'createdAt' | 'updatedAt'>) {
    if (!wss) {
        console.warn("WebSocket server not initialized, cannot broadcast update.");
        return;
    }

    const message: BroadcastMessage = {
        type: 'TASK_UPDATE',
        payload: {
            taskId: task.id,
            status: task.status,
            type: task.type,
            priority: task.priority,
            data: task.data,
            createdAt: task.createdAt,
            updatedAt: task.updatedAt,
            // Note: 'progress' is not currently tracked/included.
        },
    };
    const messageString = JSON.stringify(message);

    console.log(`Broadcasting task update to managers: ${messageString}`);

    wss.clients.forEach((client: WebSocket) => {
         // Type assertion needed here as wss.clients provides generic WebSocket
         const clientWithAuth = client as WebSocketWithAuth;

        // Check if client is authenticated, authorized (manager), and ready
        if (clientWithAuth.readyState === WebSocket.OPEN && isAuthorized(clientWithAuth)) {
            clientWithAuth.send(messageString, (error) => {
                if (error) {
                    console.error(`Failed to send message to client ${clientWithAuth.user?.username}:`, error);
                }
            });
        } else if (clientWithAuth.readyState === WebSocket.OPEN && clientWithAuth.user && !isAuthorized(clientWithAuth)) {
             // Optional: Log that a connected, authenticated user is not getting the broadcast due to role
             // console.log(`WebSocket client ${clientWithAuth.user.username} not authorized for task update broadcast.`);
        }
    });
}

// Helper function to get the WSS instance if needed elsewhere (use carefully)
export function getWssInstance(): WebSocketServer | null {
    return wss;
} 