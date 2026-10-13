package com.example.sqs.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统能力枚举。用于驱动前端功能开关显隐与后端编排分支。
 *
 * <p>{@code implemented=false} 表示仅为接入点占位，实际链路尚未实现。
 */
@Getter
@AllArgsConstructor
public enum FeatureType {

    MULTI_MODEL("multi_model", "多模型切换", "在 qwen-plus / qwen-max / qwen-turbo 之间自由切换", true),
    RAG("rag", "知识库问答", "上传文档构建知识库，基于私有资料精准作答", false),
    WEB_SEARCH("web_search", "联网搜索", "联网检索最新信息，增强回答时效性", false),
    MULTIMODAL("multimodal", "文件与图片理解", "上传图片或文档，让模型看懂并分析", false);

    private final String code;
    private final String name;
    private final String desc;
    /** 是否已完成实现，未实现的能力前端展示「即将开放」。 */
    private final boolean implemented;

    public static FeatureType of(String code) {
        for (FeatureType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
