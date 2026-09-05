package com.pushaohou.researchmate.dto;

import java.time.LocalDateTime;

public record KnowledgeBaseResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
