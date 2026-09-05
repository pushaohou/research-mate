package com.pushaohou.researchmate.controller;

import com.pushaohou.researchmate.config.ApiErrorWriter;
import com.pushaohou.researchmate.config.JacksonConfig;
import com.pushaohou.researchmate.config.SecurityConfig;
import com.pushaohou.researchmate.dto.KnowledgeBaseResponse;
import com.pushaohou.researchmate.service.JwtService;
import com.pushaohou.researchmate.service.KnowledgeBaseService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KnowledgeBaseController.class)
@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
@Import({
        SecurityConfig.class,
        ApiErrorWriter.class,
        JacksonConfig.class,
        GlobalExceptionHandler.class
})
class KnowledgeBaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeBaseService knowledgeBaseService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void knowledgeBaseEndpointsShouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/knowledge-bases"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void createShouldReturn201ForAuthenticatedUser() throws Exception {
        authenticate("valid-token", "alice");
        when(knowledgeBaseService.create(eq("alice"), any()))
                .thenReturn(new KnowledgeBaseResponse(1L, "机器学习", "论文", null, null));

        mockMvc.perform(post("/api/knowledge-bases")
                        .header("Authorization", "Bearer valid-token")
                        .contentType("application/json")
                        .content("""
                                {"name":"机器学习","description":"论文"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("机器学习"));

        verify(knowledgeBaseService).create(eq("alice"), any());
    }

    @Test
    void listShouldUseAuthenticatedUsersIdentity() throws Exception {
        authenticate("valid-token", "alice");
        when(knowledgeBaseService.list("alice"))
                .thenReturn(List.of(new KnowledgeBaseResponse(1L, "仅 Alice 可见", null, null, null)));

        mockMvc.perform(get("/api/knowledge-bases")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("仅 Alice 可见"));

        verify(knowledgeBaseService).list("alice");
    }

    @Test
    void updateShouldReturn403WhenServiceRejectsForeignKnowledgeBase() throws Exception {
        authenticate("valid-token", "alice");
        when(knowledgeBaseService.update(eq("alice"), eq(2L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该知识库"));

        mockMvc.perform(put("/api/knowledge-bases/2")
                        .header("Authorization", "Bearer valid-token")
                        .contentType("application/json")
                        .content("""
                                {"name":"新名称","description":"描述"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("无权访问该知识库"));
    }

    @Test
    void deleteShouldReturn403WhenServiceRejectsForeignKnowledgeBase() throws Exception {
        authenticate("valid-token", "alice");
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该知识库"))
                .when(knowledgeBaseService).delete("alice", 2L);

        mockMvc.perform(delete("/api/knowledge-bases/2")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("无权访问该知识库"));
    }

    @Test
    void createShouldReturn400WhenNameIsBlank() throws Exception {
        authenticate("valid-token", "alice");

        mockMvc.perform(post("/api/knowledge-bases")
                        .header("Authorization", "Bearer valid-token")
                        .contentType("application/json")
                        .content("""
                                {"name":"   ","description":"论文"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("知识库名称不能为空"));

        verify(knowledgeBaseService, never()).create(any(), any());
    }

    private void authenticate(String token, String username) {
        Claims claims = Jwts.claims().subject(username).build();
        when(jwtService.parseToken(token)).thenReturn(claims);
    }
}
