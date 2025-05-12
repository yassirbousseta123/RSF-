package com.rsf.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.io.File;

@Configuration
public class MultipartConfig {

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Set maximum file size
        factory.setMaxFileSize(DataSize.ofMegabytes(70));
        factory.setMaxRequestSize(DataSize.ofMegabytes(70));
        
        // Create temp directory if it doesn't exist
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempDirectory = new File(tempDir);
        if (!tempDirectory.exists()) {
            tempDirectory.mkdirs();
        }
        
        factory.setLocation(tempDir);
        
        return factory.createMultipartConfig();
    }
} 