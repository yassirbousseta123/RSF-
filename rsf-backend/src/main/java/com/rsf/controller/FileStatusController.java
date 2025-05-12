package com.rsf.controller;

import com.rsf.domain.FileStatus;
import com.rsf.repo.FileRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileStatusController {

    private final FileRepo files;

    @GetMapping("/{id}/status")
    public Map<String, FileStatus> status(@PathVariable UUID id) {
        return Map.of("status",
            files.findById(id)
                 .orElseThrow(() -> new RuntimeException("file not found"))
                 .getStatus());
    }
} 