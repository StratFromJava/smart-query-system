package com.example.sqs.core.vo;

import lombok.Data;

/**
 * 非流式对话响应体（预留）。
 */
@Data
public class ChatReplyVO {

    private Long sessionId;
    private Long messageId;
    private String content;
    private String model;
    private Integer tokens;
}
