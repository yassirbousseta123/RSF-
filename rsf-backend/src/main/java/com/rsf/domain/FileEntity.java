package com.rsf.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {
    @Id
    private UUID id = UUID.randomUUID();
    
    private String originalName;
    private String storedName;
    private String type;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FileStatus status;
    
    private Instant uploadedAt = Instant.now();
    
    @ManyToOne
    private User uploader;
} 