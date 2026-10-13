package com.example.sqs.core.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

/**
 * 对话请求体。
 */
@Data
public class ChatRequest {

    /** 会话 ID，为空则由后端自动创建新会话。 */
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 指定模型，为空则使用默认模型。 */
    private String model;

    /** 启用的预留能力，见 FeatureType.code。 */
    private Set<String> features;
}
