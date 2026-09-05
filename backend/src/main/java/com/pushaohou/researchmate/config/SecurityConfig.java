package com.pushaohou.researchmate.config;

import com.pushaohou.researchmate.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   ApiErrorWriter apiErrorWriter) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService, apiErrorWriter);

        return http
                // REST API 不使用 Cookie 会话，因此关闭 CSRF。
                .csrf(AbstractHttpConfigurer::disable)

                // 不创建或读取服务端 Session，认证完全由后续 JWT 完成。
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 浏览器登录页和 HTTP Basic 都不是本项目的认证方式。
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())

                // 未登录访问受保护接口时明确返回 401。
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                apiErrorWriter.write(request, response, 401, "未认证或 Token 无效"))
                        .accessDeniedHandler((request, response, exception) ->
                                apiErrorWriter.write(request, response, 403, "无权访问该资源")))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}
