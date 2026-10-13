package com.example.sqs.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体。
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long userId;

    /** user / assistant / system，见 {@link com.example.sqs.core.enums.MessageRole}。 */
    private String role;

    private String content;

    private String model;

    private Integer tokens;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
