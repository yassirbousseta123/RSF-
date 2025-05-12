package com.rsf.controller;

import com.rsf.domain.Role;
import com.rsf.domain.User;
import com.rsf.repo.RoleRepo;
import com.rsf.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/utility")
@RequiredArgsConstructor
public class UserRoleUtility {
    
    private final UserRepo users;
    private final RoleRepo roles;
    
    @PostMapping("/assign-manager-role/{username}")
    public String assignManagerRole(@PathVariable String username) {
        User user = users.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        Role managerRole = roles.findByName("ROLE_MANAGER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        // Check if role is already assigned
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_MANAGER"))) {
            return "User already has ROLE_MANAGER";
        }
        
        // Add role and save
        user.getRoles().add(managerRole);
        users.save(user);
        
        return "ROLE_MANAGER assigned to " + username;
    }
} 