package com.example.sqs.ai.config;

import com.example.sqs.ai.properties.AiProperties;
import com.example.sqs.ai.tool.WebSearchTool;
import io.agentscope.core.tool.Toolkit;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgentScope（DashScope / 通义千问）相关 Bean。
 *
 * <p>模型实例由 {@link AgentFactory} 按请求构建，
 * api-key 通过 sqs.ai.api-key 注入（环境变量 AI_DASHSCOPE_API_KEY）。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AgentConfig {

    /**
     * 全局工具集。开启 sqs.ai.web-search.enabled=true 时注册联网搜索工具，
     * 供所有 Agent 实例共享。
     */
    @Bean
    public Toolkit toolkit(ObjectProvider<WebSearchTool> webSearchToolProvider) {
        Toolkit toolkit = new Toolkit();
        WebSearchTool webSearchTool = webSearchToolProvider.getIfAvailable();
        if (webSearchTool != null) {
            toolkit.registerTool(webSearchTool);
        }
        return toolkit;
    }
}
