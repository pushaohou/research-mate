package com.pushaohou.researchmate.controller;

import com.pushaohou.researchmate.config.ApiErrorWriter;
import com.pushaohou.researchmate.config.JacksonConfig;
import com.pushaohou.researchmate.config.SecurityConfig;
import com.pushaohou.researchmate.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
@Import({
        SecurityConfig.class,
        ApiErrorWriter.class,
        JacksonConfig.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void meShouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("未认证或 Token 无效"));
    }

    @Test
    void meShouldReturn200WithValidToken() throws Exception {
        Claims claims = Jwts.claims()
                .subject("demo_user")
                .build();

        when(jwtService.parseToken("valid-token")).thenReturn(claims);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo_user"));
    }
}
