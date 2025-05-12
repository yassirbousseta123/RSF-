package com.rsf.controller;

import com.rsf.domain.FileEntity;
import com.rsf.domain.User;
import com.rsf.repo.FileRepo;
import com.rsf.repo.UserRepo;
import com.rsf.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storage;
    private final UserRepo      users;
    private final FileRepo      files;

    @PostMapping("/upload")
    public FileEntity upload(@RequestPart MultipartFile file,
                             @AuthenticationPrincipal UserDetails auth) throws IOException {
        User uploader = users.findByUsername(auth.getUsername())
                             .orElseThrow(() -> new RuntimeException("user not found"));
        return storage.store(file, uploader);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Resource res = storage.load(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + res.getFilename() + "\"")
                .body(res);
    }

    /** Poll endpoint to fetch current metadata/status of the file */
    @GetMapping("/{id}/meta")
    public FileEntity meta(@PathVariable UUID id) {
        return files.findById(id)
                    .orElseThrow(() -> new RuntimeException("file not found"));
    }
}
