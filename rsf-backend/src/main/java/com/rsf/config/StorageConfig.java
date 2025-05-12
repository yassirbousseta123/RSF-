package com.rsf.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class StorageConfig {
    
    @Value("${file.storage-path}")
    private String storagePath;
    
    @Bean
    public CommandLineRunner initStorageDirectory() {
        return args -> {
            Path path = Paths.get(storagePath);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                    log.info("Storage directory created: {}", path.toAbsolutePath());
                } catch (Exception e) {
                    log.error("Failed to create storage directory: {}", path.toAbsolutePath(), e);
                    throw new RuntimeException("Could not initialize storage: " + e.getMessage(), e);
                }
            } else {
                if (!Files.isWritable(path)) {
                    log.error("Storage directory exists but is not writable: {}", path.toAbsolutePath());
                    throw new RuntimeException("Storage directory is not writable: " + path.toAbsolutePath());
                }
                log.info("Storage directory exists and is writable: {}", path.toAbsolutePath());
            }
        };
    }
} 