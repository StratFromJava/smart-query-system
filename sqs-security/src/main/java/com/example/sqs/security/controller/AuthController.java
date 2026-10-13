package com.example.sqs.security.controller;

import com.example.sqs.common.result.R;
import com.example.sqs.core.vo.UserVO;
import com.example.sqs.security.SecurityUtils;
import com.example.sqs.security.service.AuthService;
import com.example.sqs.security.vo.AuthVO;
import com.example.sqs.security.vo.LoginRequest;
import com.example.sqs.security.vo.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public R<AuthVO> register(@Valid @RequestBody RegisterRequest request) {
        return R.ok(authService.register(request));
    }

    @PostMapping("/login")
    public R<AuthVO> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @GetMapping("/me")
    public R<UserVO> me() {
        Long userId = SecurityUtils.currentUserId();
        UserVO user = authService.currentUser(userId);
        return R.ok(user);
    }
}
