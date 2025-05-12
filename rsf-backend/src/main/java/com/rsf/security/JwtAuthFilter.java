package com.rsf.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    
    // Define public endpoints that should bypass JWT authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/auth/",
        "/api/v1/auth/",
        "/api/v1/import/upload",
        "/api/v1/import/progress/",
        "/api/v1/import/results/",
        "/api/v1/import/status"
    );

    private final JwtTokenProvider tokens;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtTokenProvider tokens, UserDetailsService userDetailsService) {
        this.tokens = tokens;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = req.getRequestURI();
        log.debug("JwtAuthFilter processing request: {} {}", req.getMethod(), requestURI);

        // Check if the request URI is in the public endpoints list
        boolean isPublicEndpoint = PUBLIC_ENDPOINTS.stream()
                .anyMatch(requestURI::startsWith);
                
        if (isPublicEndpoint) {
            log.debug("Skipping JWT processing for public endpoint: {}", requestURI);
            chain.doFilter(req, res);
            return;
        }
        
        // --- Original JWT Processing Logic ---
        final String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.debug("Extracted token for {}: {}", requestURI, token);

            try {
            if (tokens.validate(token)) {
                String username = tokens.getUsername(token);
                    log.debug("Valid token for user: {}", username);

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(username);
                    log.debug("Authorities from userDetailsService: {}", user.getAuthorities());

                var auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Set SecurityContext for user: {}, Authorities: {}", username, auth.getAuthorities());
                    } else {
                         log.debug("SecurityContext already contains authentication for user: {}", SecurityContextHolder.getContext().getAuthentication().getName());
                    }
                } else {
                    log.warn("Invalid JWT token detected for request: {} {}", req.getMethod(), requestURI);
                    SecurityContextHolder.clearContext(); 
                }
            } catch (Exception e) {
                log.error("Error processing JWT token for request: {} {}", req.getMethod(), requestURI, e);
                SecurityContextHolder.clearContext(); 
            }
        } else {
            log.debug("No Bearer token found for request: {} {}", req.getMethod(), requestURI);
        }

        chain.doFilter(req, res);
    }
}
