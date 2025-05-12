package com.rsf.controller;

import com.rsf.domain.User;
import com.rsf.domain.Role;
import com.rsf.dto.*;
import com.rsf.repo.UserRepo;
import com.rsf.repo.RoleRepo;
import com.rsf.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokens;
    private final UserRepo users;
    private final RoleRepo roles;
    private final PasswordEncoder encoder;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            String token = tokens.generate(req.username());
            return new AuthResponse(token);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/whoami")
    public Map<String, Object> whoami(@AuthenticationPrincipal UserDetails auth) {
        return Map.of(
            "username", auth.getUsername(),
            "authorities", auth.getAuthorities().stream()
                             .map(a -> a.getAuthority()).toList()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Valid LoginRequest req) {
        try {
            if (users.findByUsername(req.username()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
            }
            
            Role defaultRole = roles.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Default role USER not found."));

            User u = new User();
            u.setUsername(req.username());
            u.setPassword(encoder.encode(req.password()));
            u.setRoles(Set.of(defaultRole));
            
            users.save(u);
            return ResponseEntity.ok(Map.of("message", "Registration successful"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }
} 