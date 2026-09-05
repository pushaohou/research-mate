package com.pushaohou.researchmate.service;

import com.pushaohou.researchmate.dto.CreateKnowledgeBaseRequest;
import com.pushaohou.researchmate.dto.KnowledgeBaseResponse;
import com.pushaohou.researchmate.dto.UpdateKnowledgeBaseRequest;
import com.pushaohou.researchmate.entity.KnowledgeBase;
import com.pushaohou.researchmate.entity.User;
import com.pushaohou.researchmate.repository.KnowledgeBaseRepository;
import com.pushaohou.researchmate.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final UserRepository userRepository;

    public KnowledgeBaseService(
            KnowledgeBaseRepository knowledgeBaseRepository,
            UserRepository userRepository
    ) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public KnowledgeBaseResponse create(String username, CreateKnowledgeBaseRequest request) {
        User user = getCurrentUser(username);
        String name = request.name().trim();

        if (knowledgeBaseRepository.existsByUserIdAndName(user.getId(), name)) {
            throw badRequest("当前用户已存在同名知识库");
        }

        KnowledgeBase knowledgeBase = new KnowledgeBase(name, request.description(), user);
        return toResponse(knowledgeBaseRepository.save(knowledgeBase));
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBaseResponse> list(String username) {
        User user = getCurrentUser(username);
        return knowledgeBaseRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeBaseResponse getById(String username, Long id) {
        User user = getCurrentUser(username);
        return toResponse(getOwnedKnowledgeBase(user, id));
    }

    @Transactional
    public KnowledgeBaseResponse update(
            String username,
            Long id,
            UpdateKnowledgeBaseRequest request
    ) {
        User user = getCurrentUser(username);
        KnowledgeBase knowledgeBase = getOwnedKnowledgeBase(user, id);
        String name = request.name().trim();

        if (!knowledgeBase.getName().equals(name)
                && knowledgeBaseRepository.existsByUserIdAndName(user.getId(), name)) {
            throw badRequest("当前用户已存在同名知识库");
        }

        knowledgeBase.update(name, request.description());
        return toResponse(knowledgeBaseRepository.save(knowledgeBase));
    }

    @Transactional
    public void delete(String username, Long id) {
        User user = getCurrentUser(username);
        knowledgeBaseRepository.delete(getOwnedKnowledgeBase(user, id));
    }

    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "当前用户不存在或登录已失效"
                ));
    }

    private KnowledgeBase getOwnedKnowledgeBase(User user, Long id) {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "知识库不存在"
                ));

        if (!knowledgeBase.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该知识库");
        }

        return knowledgeBase;
    }

    private KnowledgeBaseResponse toResponse(KnowledgeBase knowledgeBase) {
        return new KnowledgeBaseResponse(
                knowledgeBase.getId(),
                knowledgeBase.getName(),
                knowledgeBase.getDescription(),
                knowledgeBase.getCreatedAt(),
                knowledgeBase.getUpdatedAt()
        );
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
