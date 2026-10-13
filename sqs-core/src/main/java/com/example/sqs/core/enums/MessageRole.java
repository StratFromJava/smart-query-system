package com.example.sqs.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息角色。
 */
@Getter
@AllArgsConstructor
public enum MessageRole {

    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private final String value;
}
