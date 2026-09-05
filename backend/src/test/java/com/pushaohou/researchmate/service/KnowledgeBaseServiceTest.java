package com.pushaohou.researchmate.service;

import com.pushaohou.researchmate.dto.CreateKnowledgeBaseRequest;
import com.pushaohou.researchmate.dto.UpdateKnowledgeBaseRequest;
import com.pushaohou.researchmate.entity.KnowledgeBase;
import com.pushaohou.researchmate.entity.User;
import com.pushaohou.researchmate.repository.KnowledgeBaseRepository;
import com.pushaohou.researchmate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseServiceTest {

    @Mock
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KnowledgeBaseService knowledgeBaseService;

    @Test
    void createShouldRejectDuplicateNameForSameUser() {
        User alice = user(1L);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(knowledgeBaseRepository.existsByUserIdAndName(1L, "机器学习"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                knowledgeBaseService.create("alice", new CreateKnowledgeBaseRequest("  机器学习  ", null))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("当前用户已存在同名知识库", exception.getReason());
        verify(knowledgeBaseRepository, never()).save(any());
    }

    @Test
    void updateShouldRejectKnowledgeBaseOwnedByAnotherUser() {
        User alice = user(1L);
        User bob = user(2L);
        KnowledgeBase bobsKnowledgeBase = knowledgeBase(bob);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(knowledgeBaseRepository.findById(8L)).thenReturn(Optional.of(bobsKnowledgeBase));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                knowledgeBaseService.update("alice", 8L, new UpdateKnowledgeBaseRequest("新名称", null))
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(knowledgeBaseRepository, never()).save(any());
    }

    @Test
    void deleteShouldRejectKnowledgeBaseOwnedByAnotherUser() {
        User alice = user(1L);
        User bob = user(2L);
        KnowledgeBase bobsKnowledgeBase = knowledgeBase(bob);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(knowledgeBaseRepository.findById(8L)).thenReturn(Optional.of(bobsKnowledgeBase));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                knowledgeBaseService.delete("alice", 8L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(knowledgeBaseRepository, never()).delete(any());
    }

    @Test
    void getByIdShouldReturn404WhenKnowledgeBaseDoesNotExist() {
        User alice = new User("alice", "alice@example.com", "hash");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(knowledgeBaseRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                knowledgeBaseService.getById("alice", 99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void listShouldQueryOnlyTheAuthenticatedUsersKnowledgeBases() {
        User alice = user(1L);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(knowledgeBaseRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        knowledgeBaseService.list("alice");

        verify(knowledgeBaseRepository).findAllByUserIdOrderByCreatedAtDesc(1L);
    }

    private User user(Long id) {
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private KnowledgeBase knowledgeBase(User owner) {
        KnowledgeBase knowledgeBase = org.mockito.Mockito.mock(KnowledgeBase.class);
        when(knowledgeBase.getUser()).thenReturn(owner);
        return knowledgeBase;
    }
}
