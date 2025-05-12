package com.rsf.service;

import com.rsf.domain.FileEntity;
import com.rsf.domain.FileStatus;
import com.rsf.domain.User;
import com.rsf.repo.FileRepo;
import com.rsf.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final FileRepo files;
    private final UnzipService unzip;
    @Value("${file.storage-path}") private Path root;

    public FileEntity store(MultipartFile multipart, User uploader) throws IOException {
        Files.createDirectories(root);
        String ext = FilenameUtils.getExtension(multipart.getOriginalFilename());
        String stored = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(stored);
        multipart.transferTo(target);

        FileEntity e = new FileEntity();
        e.setOriginalName(multipart.getOriginalFilename());
        e.setStoredName(stored);
        e.setType(FileUtils.detectType(multipart.getOriginalFilename()));
        e.setStatus(FileStatus.PROCESSING);
        e.setUploader(uploader);
        e = files.save(e);

        // async unzip
        Path dest = root.resolve(e.getId().toString());
        FileEntity finalE = e;
        unzip.extract(target, dest,
            () -> { finalE.setStatus(FileStatus.READY);  files.save(finalE); },
            () -> { finalE.setStatus(FileStatus.ERROR);  files.save(finalE); });

        return e;
    }

    public Resource load(UUID id) {
        FileEntity e = files.findById(id)
            .orElseThrow(() -> new RuntimeException("file not found"));
        return new FileSystemResource(root.resolve(e.getStoredName()));
    }
} 