package com.example.sqs.security.service;

import com.example.sqs.common.exception.BizException;
import com.example.sqs.common.result.ResultCode;
import com.example.sqs.core.entity.SysUser;
import com.example.sqs.core.service.SysUserService;
import com.example.sqs.core.vo.UserVO;
import com.example.sqs.security.JwtTokenProvider;
import com.example.sqs.security.vo.AuthVO;
import com.example.sqs.security.vo.LoginRequest;
import com.example.sqs.security.vo.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 注册与登录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(rollbackFor = Exception.class)
    public AuthVO register(RegisterRequest request) {
        if (sysUserService.existsUsername(request.getUsername())) {
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }
        String encoded = passwordEncoder.encode(request.getPassword());
        Long userId = sysUserService.register(request.getUsername(), encoded, request.getNickname());
        log.info("用户注册成功, userId={}, username={}", userId, request.getUsername());
        return buildAuthVO(userId, request.getUsername());
    }

    public AuthVO login(LoginRequest request) {
        SysUser user = sysUserService.getByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ResultCode.ACCOUNT_DISABLED);
        }
        return buildAuthVO(user.getId(), user.getUsername());
    }

    /**
     * 查询当前登录用户信息。
     */
    public UserVO currentUser(Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return sysUserService.toVO(user);
    }

    private AuthVO buildAuthVO(Long userId, String username) {
        SysUser user = sysUserService.getById(userId);
        UserVO userVO = sysUserService.toVO(user);

        AuthVO vo = new AuthVO();
        vo.setToken(jwtTokenProvider.generateToken(userId, username));
        vo.setExpiresIn(jwtTokenProvider.getExpirationMillis());
        vo.setUser(userVO);
        return vo;
    }
}
