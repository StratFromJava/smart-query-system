package com.example.sqs.ai.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.sqs.ai.properties.AiProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Alibaba（DashScope / 通义千问）相关 Bean。
 *
 * <p>DashScopeChatModel 由 spring-ai-alibaba-starter-dashscope 自动装配，
 * api-key 通过 spring.ai.dashscope.api-key 注入（见 application.yml）。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class DashScopeConfig {

    /**
     * 全局 ChatClient。默认系统提示词与默认模型由配置驱动，
     * 单次请求可通过 options 覆盖模型，实现多模型切换。
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, AiProperties aiProperties) {
        return builder
                .defaultSystem(aiProperties.getSystemPrompt())
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel(aiProperties.getDefaultModel())
                        .withTemperature(aiProperties.getTemperature())
                        .build())
                .build();
    }
}
