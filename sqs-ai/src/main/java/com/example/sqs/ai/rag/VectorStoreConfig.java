package com.example.sqs.ai.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量库接入点（预留能力）。
 *
 * <p>默认使用 Spring AI 内存实现 {@link SimpleVectorStore}；
 * 生产环境可替换为 AnalyticDB / Milvus / PGVector，只需提供新的 {@link VectorStore} Bean 即可。
 */
@Configuration
@ConditionalOnProperty(prefix = "sqs.ai.rag", name = "enabled", havingValue = "true")
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
