package com.example.sqs.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话视图对象。
 */
@Data
public class SessionVO {

    private Long id;
    private String title;
    private String model;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
