package com.example.sqs.ai.rag;

import com.example.sqs.ai.properties.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 知识库检索（预留能力）。开启 sqs.ai.rag.enabled=true 后生效。
 *
 * <p>当前为占位实现：检索逻辑待接入向量库（如 Milvus / PGVector / AnalyticDB）
 * 或 AgentScope Knowledge 后补充，无命中时返回空串（不注入上下文）。
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "sqs.ai.rag", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RagService {

    private final AiProperties aiProperties;

    /**
     * 检索与问题相关的资料片段，拼装为提示词上下文；无命中时返回空串。
     */
    public String search(String query) {
        if (!StringUtils.hasText(query)) {
            return "";
        }
        log.debug("RAG 检索触发, topK={}, query={}（当前为占位实现，无向量库）",
                aiProperties.getRag().getTopK(), query);
        return "";
    }
}
