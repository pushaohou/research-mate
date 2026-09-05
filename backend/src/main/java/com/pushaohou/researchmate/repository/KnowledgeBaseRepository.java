package com.pushaohou.researchmate.repository;

import com.pushaohou.researchmate.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<KnowledgeBase> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndName(Long userId, String name);
}
