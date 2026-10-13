package com.example.sqs.security.vo;

import com.example.sqs.core.vo.UserVO;
import lombok.Data;

/**
 * 认证响应体。
 */
@Data
public class AuthVO {

    private String token;
    private String tokenType = "Bearer";
    /** 有效期，单位毫秒。 */
    private long expiresIn;
    private UserVO user;
}
