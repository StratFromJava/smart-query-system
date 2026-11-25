package com.example.sqs.ai.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sqs.ai.properties.AiProperties;
import com.example.sqs.core.entity.KnowledgeChunk;
import com.example.sqs.core.mapper.KnowledgeChunkMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

/**
 * 知识库检索：问题向量化后与用户知识库分块做余弦相似度，返回 topK 资料片段。
 *
 * <p>本地轻量实现：向量存 SQLite（JSON 文本），检索时载入内存计算。
 * 后续切换 Milvus / PGVector 时仅需替换本类实现。
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "sqs.ai.rag", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class RagService {

    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingClient embeddingClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    /**
     * 检索与问题相关的资料片段，拼装为提示词上下文；无命中时返回空串。
     */
    public String search(Long userId, String query) {
        if (!StringUtils.hasText(query)) {
            return "";
        }
        List<KnowledgeChunk> chunks = chunkMapper.selectList(
                new LambdaQueryWrapper<KnowledgeChunk>()
                        .eq(KnowledgeChunk::getUserId, userId)
                        .isNotNull(KnowledgeChunk::getEmbedding)
                        .last("LIMIT " + aiProperties.getRag().getScanLimit()));
        if (chunks.isEmpty()) {
            log.debug("知识库为空, userId={}", userId);
            return "";
        }

        float[] queryVector = embeddingClient.embed(query);
        List<Scored> scored = chunks.stream()
                .map(chunk -> new Scored(chunk, cosine(queryVector, parse(chunk.getEmbedding()))))
                .filter(s -> s.score() > 0)
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(aiProperties.getRag().getTopK())
                .toList();

        StringBuilder context = new StringBuilder();
        int index = 1;
        for (Scored s : scored) {
            context.append("[资料").append(index++).append("] ")
                    .append(s.chunk().getContent())
                    .append('\n');
        }
        log.debug("RAG 命中 {} 条资料, userId={}", scored.size(), userId);
        return context.toString();
    }

    private float[] parse(String embedding) {
        try {
            return objectMapper.readValue(embedding, float[].class);
        } catch (Exception e) {
            return new float[0];
        }
    }

    private double cosine(float[] a, float[] b) {
        if (a.length == 0 || a.length != b.length) {
            return 0;
        }
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record Scored(KnowledgeChunk chunk, double score) {
    }
}
