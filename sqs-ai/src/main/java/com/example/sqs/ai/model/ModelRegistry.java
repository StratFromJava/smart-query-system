package com.example.sqs.ai.model;

import com.example.sqs.ai.properties.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型注册表：提供可选模型列表与模型名合法性校验。
 */
@Component
@RequiredArgsConstructor
public class ModelRegistry {

    private final AiProperties aiProperties;

    /**
     * 返回模型定义，按配置顺序保持有序。
     */
    public Map<String, AiProperties.ModelDef> models() {
        Map<String, AiProperties.ModelDef> map = new LinkedHashMap<>();
        for (AiProperties.ModelDef def : safeModels()) {
            map.put(def.getCode(), def);
        }
        return map;
    }

    public List<AiProperties.ModelDef> list() {
        return safeModels();
    }

    /**
     * 解析实际使用的模型：请求模型为空或非法时回退到默认模型。
     */
    public String resolve(String requested) {
        Map<String, AiProperties.ModelDef> models = models();
        if (requested != null && models.containsKey(requested)) {
            return requested;
        }
        String fallback = aiProperties.getDefaultModel();
        return models.containsKey(fallback) ? fallback
                : (models.isEmpty() ? fallback : models.keySet().iterator().next());
    }

    public boolean isMultimodal(String model) {
        AiProperties.ModelDef def = models().get(model);
        return def != null && def.isMultimodal();
    }

    private List<AiProperties.ModelDef> safeModels() {
        List<AiProperties.ModelDef> models = aiProperties.getModels();
        return models == null ? List.of() : models;
    }
}
