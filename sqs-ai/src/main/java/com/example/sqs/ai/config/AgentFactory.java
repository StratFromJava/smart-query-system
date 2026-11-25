package com.example.sqs.ai.config;

import com.example.sqs.ai.properties.AiProperties;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.dashscope.DashScopeChatModel;
import io.agentscope.extensions.model.dashscope.formatter.DashScopeChatFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Agent 工厂：按请求构建 AgentScope ReActAgent。
 *
 * <p>Agent 本身无状态，会话历史由 ChatService 从数据库加载后随请求传入；
 * 模型（DashScope）、系统提示词、温度均由配置驱动，实现多模型切换。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentFactory {

    private final AiProperties aiProperties;
    private final Toolkit toolkit;

    /**
     * 构建指定模型的 Agent 实例（请求级，用后即弃）。
     *
     * @param modelCode    模型名（如 qwen-plus）
     * @param systemPrompt 本次请求使用的系统提示词（含可选 RAG 注入）
     * @param webSearch    是否启用 DashScope 联网搜索插件
     */
    public ReActAgent create(String modelCode, String systemPrompt, boolean webSearch) {
        GenerateOptions.Builder options = GenerateOptions.builder()
                .temperature(aiProperties.getTemperature());
        if (webSearch && aiProperties.getWebSearch().isEnabled()) {
            // DashScope 百炼联网搜索插件：参数合并进请求 parameters
            options.additionalBodyParam("enable_search", true);
        }

        DashScopeChatModel model = DashScopeChatModel.builder()
                .apiKey(apiKey())
                .modelName(modelCode)
                .stream(true)
                .formatter(new DashScopeChatFormatter())
                .defaultOptions(options.build())
                .build();

        return ReActAgent.builder()
                .name("smart_query_assistant")
                .sysPrompt(systemPrompt)
                .model(model)
                .toolkit(toolkit)
                .build();
    }

    private String apiKey() {
        String apiKey = aiProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException(
                    "DashScope API key must be set (sqs.ai.api-key / AI_DASHSCOPE_API_KEY)");
        }
        return apiKey;
    }
}
