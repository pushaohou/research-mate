package com.pushaohou.researchmate.controller;

import com.pushaohou.researchmate.config.ApiErrorWriter;
import com.pushaohou.researchmate.config.JacksonConfig;
import com.pushaohou.researchmate.config.SecurityConfig;
import com.pushaohou.researchmate.dto.LoginResponse;
import com.pushaohou.researchmate.service.AuthService;
import com.pushaohou.researchmate.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
@Import({
        SecurityConfig.class,
        ApiErrorWriter.class,
        JacksonConfig.class,
        GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void loginShouldReturnTokenWhenCredentialsAreCorrect() throws Exception {
        LoginResponse response = new LoginResponse(
                "test-access-token",
                "Bearer",
                3_600_000,
                null
        );

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "usernameOrEmail": "demo_user",
                                  "password": "ChangeMe@2026"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3_600_000));
    }

    @Test
    void loginShouldReturn401WhenPasswordIsIncorrect() throws Exception {
        when(authService.login(any())).thenThrow(
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "用户名或密码错误"
                )
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "usernameOrEmail": "demo_user",
                                  "password": "WrongPassword"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }
}
