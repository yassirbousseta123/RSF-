package com.rsf.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MultipartRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
            log.info("Processing multipart request to: {}", request.getRequestURI());
            log.info("Content type: {}", request.getContentType());
            log.info("Method: {}", request.getMethod());
            log.info("Content length: {}", request.getContentLength());
            
            if (request instanceof MultipartHttpServletRequest) {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                multipartRequest.getFileMap().forEach((name, file) -> {
                    log.info("File part: name={}, original filename={}, size={}", 
                            name, file.getOriginalFilename(), file.getSize());
                });
            } else {
                log.warn("Request has multipart content type but is not a MultipartHttpServletRequest");
            }
        }
        
        filterChain.doFilter(request, response);
    }
} 