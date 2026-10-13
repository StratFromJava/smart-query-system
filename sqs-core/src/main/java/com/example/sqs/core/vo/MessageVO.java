package com.example.sqs.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息视图对象。
 */
@Data
public class MessageVO {

    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String model;
    private Integer tokens;
    private LocalDateTime createTime;
}
