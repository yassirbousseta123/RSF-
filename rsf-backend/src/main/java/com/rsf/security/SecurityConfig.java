package com.rsf.security;

import com.rsf.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final UserRepo users;

    /* ---------- Beans ---------- */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            var user = users.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            log.debug("Loaded user: {} with roles: {}", username, 
                     user.getRoles().stream().map(r -> r.getName()).toList());
            return user;
        };
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration conf) throws Exception {
        return conf.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /** Main security filter-chain */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            // Disable CSRF completely for all requests
            .csrf(AbstractHttpConfigurer::disable)
            
            // Enable CORS 
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session management
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Request authorization rules
            .authorizeHttpRequests(auth -> {
                // Public endpoints
                auth.requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    
                    // Import endpoints
                    .requestMatchers("/api/v1/import/upload").permitAll()
                    .requestMatchers("/api/v1/import/status").permitAll()
                    .requestMatchers("/api/v1/import/progress/**").permitAll()
                    .requestMatchers("/api/v1/import/results/**").permitAll()
                    
                    // TEMPORARY: Allow validation endpoints for testing
                    .requestMatchers("/api/v1/validation/**").permitAll()
                    
                    // TEMPORARY: Allow all file upload requests for testing
                    .requestMatchers(HttpMethod.POST, "/api/files/upload").permitAll()
                    
                    // All other file endpoints require authentication
                    .requestMatchers("/api/files/**").authenticated()
                    
                    // Default rule for all other endpoints
                    .anyRequest().authenticated();
            })
            
            // Add filters
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}