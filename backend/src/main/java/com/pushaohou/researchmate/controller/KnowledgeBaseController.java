package com.pushaohou.researchmate.controller;

import com.pushaohou.researchmate.dto.CreateKnowledgeBaseRequest;
import com.pushaohou.researchmate.dto.KnowledgeBaseResponse;
import com.pushaohou.researchmate.dto.UpdateKnowledgeBaseRequest;
import com.pushaohou.researchmate.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeBaseResponse create(
            @Valid @RequestBody CreateKnowledgeBaseRequest request,
            Authentication authentication
    ) {
        return knowledgeBaseService.create(authentication.getName(), request);
    }

    @GetMapping
    public List<KnowledgeBaseResponse> list(Authentication authentication) {
        return knowledgeBaseService.list(authentication.getName());
    }

    @GetMapping("/{id}")
    public KnowledgeBaseResponse getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return knowledgeBaseService.getById(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    public KnowledgeBaseResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeBaseRequest request,
            Authentication authentication
    ) {
        return knowledgeBaseService.update(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        knowledgeBaseService.delete(authentication.getName(), id);
    }
}
