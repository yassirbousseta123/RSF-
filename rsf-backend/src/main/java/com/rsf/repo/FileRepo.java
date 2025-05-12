package com.rsf.repo;

import com.rsf.domain.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FileRepo extends JpaRepository<FileEntity, UUID> {
} 