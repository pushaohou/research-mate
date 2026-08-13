package com.pushaohou.researchmate.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "用户名或邮箱不能为空")
        String usernameOrEmail,

        @NotBlank(message = "密码不能为空")
        String password
) {
}