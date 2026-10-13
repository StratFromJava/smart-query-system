package com.example.sqs.ai.rag;

import com.example.sqs.ai.properties.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 知识库检索（预留能力）。开启 sqs.ai.rag.enabled=true 后生效。
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "sqs.ai.rag", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;
    private final AiProperties aiProperties;

    /**
     * 检索与问题相关的资料片段，拼装为提示词上下文；无命中时返回空串。
     */
    public String search(String query) {
        if (!StringUtils.hasText(query)) {
            return "";
        }
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(aiProperties.getRag().getTopK())
                .build());
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            context.append("[资料").append(i + 1).append("] ")
                    .append(documents.get(i).getText())
                    .append('\n');
        }
        log.debug("RAG 命中 {} 条资料", documents.size());
        return context.toString();
    }
}
