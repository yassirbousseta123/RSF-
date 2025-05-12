package com.rsf.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnzipService {

    @Value("${file.storage-path}") private Path root;

    @Async("rsfExecutor")
    public void extract(Path zipFile, Path destDir, Runnable onSuccess, Runnable onError) {
        try {
            Files.createDirectories(destDir);
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path target = destDir.resolve(entry.getName()).normalize();
                    if (entry.isDirectory()) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }
            onSuccess.run();
        } catch (Exception ex) {
            log.error("Failed to extract {}", zipFile, ex);
            onError.run();
        }
    }
} 