package com.example.sqs.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息视图对象（不含密码）。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
}
