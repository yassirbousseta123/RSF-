import { Request, Response, NextFunction } from 'express';
import jwt, { JwtPayload } from 'jsonwebtoken';

// Reuse the AuthenticatedUser interface (consider moving to a shared types file)
interface AuthenticatedUser {
    id: string; // Or number
    username: string;
    role: string; // e.g., 'manager', 'consultant'
    iat?: number;
    exp?: number;
}

// Extend Express Request interface to include the user property
declare global {
    namespace Express {
        interface Request {
            user?: AuthenticatedUser;
        }
    }
}

const JWT_SECRET = process.env.JWT_SECRET;

if (!JWT_SECRET) {
    console.error("FATAL ERROR: JWT_SECRET environment variable is not set for auth middleware.");
    process.exit(1);
}

// Helper type guard (same as in websocket.ts - ideal for a shared types file)
function isUserPayload(payload: string | JwtPayload): payload is AuthenticatedUser {
  return (
    typeof payload === 'object' &&
    payload !== null &&
    typeof (payload as AuthenticatedUser).id !== 'undefined' &&
    typeof (payload as AuthenticatedUser).username === 'string' &&
    typeof (payload as AuthenticatedUser).role === 'string'
  );
}

/**
 * Middleware to verify JWT token from Authorization header.
 * Attaches decoded user payload to req.user if valid.
 */
export const checkAuth = (req: Request, res: Response, next: NextFunction) => {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        console.log('Auth Middleware: No or invalid Bearer token header.');
        return res.status(401).json({ message: 'Authentication required: Invalid token format.' });
    }

    const token = authHeader.split(' ')[1];

    try {
        const decoded = jwt.verify(token, JWT_SECRET as string);

        if (isUserPayload(decoded)) {
            // Token is valid and payload structure is correct
            req.user = decoded; // Attach user to request object
            console.log(`Auth Middleware: User ${req.user.username} authenticated.`);
            next(); // Proceed to the next middleware or route handler
        } else {
             console.error('Auth Middleware: Invalid token payload structure.', decoded);
             return res.status(401).json({ message: 'Authentication failed: Invalid token payload.' });
        }
    } catch (error: any) {
        console.error('Auth Middleware: Token verification failed.', error.message);
        if (error.name === 'TokenExpiredError') {
             return res.status(401).json({ message: 'Authentication failed: Token expired.' });
        }
         if (error.name === 'JsonWebTokenError') {
            return res.status(401).json({ message: 'Authentication failed: Invalid token.' });
        }
        // Handle other potential errors during verification
        return res.status(500).json({ message: 'Authentication error.' });
    }
};

/**
 * Middleware factory to check if authenticated user has one of the required roles.
 * Assumes checkAuth middleware has run previously and set req.user.
 *
 * @param allowedRoles Array of role strings allowed to access the route.
 */
export const checkRole = (allowedRoles: string[]) => {
    return (req: Request, res: Response, next: NextFunction) => {
        if (!req.user) {
             // This should ideally not happen if checkAuth runs first
             console.error('Role Check Middleware: req.user not found. checkAuth might have failed or was not used.');
             return res.status(401).json({ message: 'Authentication required.' });
        }

        const userRole = req.user.role;
        if (!allowedRoles.includes(userRole)) {
             console.warn(`Role Check Middleware: User ${req.user.username} (Role: ${userRole}) attempt to access restricted route. Allowed: ${allowedRoles.join(', ')}`);
             return res.status(403).json({ message: 'Forbidden: You do not have permission to access this resource.' });
        }

        console.log(`Role Check Middleware: User ${req.user.username} (Role: ${userRole}) authorized for roles [${allowedRoles.join(', ')}}].`);
        next(); // User has the required role
    };
}; 