package com.example.sqs.security;

import com.example.sqs.common.exception.BizException;
import com.example.sqs.common.result.ResultCode;
import com.example.sqs.security.entity.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户访问入口。
 *
 * <p>业务层一律通过本类获取 userId，禁止信任前端传入的用户标识。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 当前登录用户 ID，未登录时抛 401。
     */
    public static Long currentUserId() {
        return principal().getUserId();
    }

    public static String currentUsername() {
        return principal().getUsername();
    }

    private static LoginUser principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser principal)) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return principal;
    }
}
