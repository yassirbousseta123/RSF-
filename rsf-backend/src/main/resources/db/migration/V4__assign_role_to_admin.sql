-- Assign ROLE_MANAGER to admin user if not already assigned
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_MANAGER'
AND NOT EXISTS (
    SELECT 1 FROM user_roles 
    WHERE user_id = u.id AND role_id = r.id
); 