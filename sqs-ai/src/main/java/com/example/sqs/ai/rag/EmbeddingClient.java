package com.example.sqs.ai.rag;

import com.example.sqs.ai.properties.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DashScope 向量化客户端（OpenAI 兼容 embeddings 接口）。
 *
 * <p>模型默认 text-embedding-v4；分批调用以满足单次请求条数限制。
 */
@Slf4j
@Component
public class EmbeddingClient {

    private final RestClient restClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmbeddingClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.restClient = RestClient.builder()
                .baseUrl(aiProperties.getEmbedding().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + aiProperties.getApiKey())
                .build();
    }

    /**
     * 向量化单条文本。
     */
    public float[] embed(String text) {
        return embedAll(List.of(text)).get(0);
    }

    /**
     * 分批向量化文本列表，返回与输入顺序一致、长度相同的向量列表。
     */
    public List<float[]> embedAll(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        List<float[]> vectors = new ArrayList<>(texts.size());
        int batchSize = Math.max(1, aiProperties.getEmbedding().getBatchSize());
        for (int from = 0; from < texts.size(); from += batchSize) {
            List<String> batch = texts.subList(from, Math.min(from + batchSize, texts.size()));
            vectors.addAll(embedBatch(batch));
        }
        return vectors;
    }

    private List<float[]> embedBatch(List<String> batch) {
        Map<String, Object> body = Map.of(
                "model", aiProperties.getEmbedding().getModel(),
                "input", batch,
                "dimensions", aiProperties.getEmbedding().getDimensions(),
                "encoding_format", "float");

        String response = restClient.post()
                .uri("/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        if (!StringUtils.hasText(response)) {
            throw new IllegalStateException("Embedding 接口返回为空");
        }
        try {
            JsonNode data = objectMapper.readTree(response).path("data");
            if (!data.isArray() || data.size() != batch.size()) {
                throw new IllegalStateException("Embedding 接口返回条数与请求不一致");
            }
            List<float[]> vectors = new ArrayList<>(data.size());
            for (JsonNode item : data) {
                JsonNode embedding = item.path("embedding");
                if (!embedding.isArray()) {
                    throw new IllegalStateException("Embedding 接口返回缺少向量字段");
                }
                float[] vector = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    vector[i] = (float) embedding.get(i).asDouble();
                }
                vectors.add(vector);
            }
            return vectors;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Embedding 响应解析失败", e);
        }
    }
}
