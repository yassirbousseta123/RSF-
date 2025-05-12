package com.rsf.rsf.domain.models;

import lombok.Getter;

@Getter
public class FieldDefinition {
    private final String name;
    private final int startPosition; // 1-based index
    private final int length;

    // Explicit constructor
    public FieldDefinition(String name, int startPosition, int length) {
        this.name = name;
        this.startPosition = startPosition;
        this.length = length;
    }

    // Helper to get 0-based start index for substring operations
    public int getStartIndex() {
        return startPosition - 1;
    }

    // Helper to get 0-based end index (exclusive) for substring operations
    public int getEndIndex() {
        return getStartIndex() + length;
    }
} 