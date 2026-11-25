package com.example.sqs.ai.config;

import com.example.sqs.ai.properties.AiProperties;
import io.agentscope.core.tool.Toolkit;
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
     * 全局工具集，供所有 Agent 实例共享（联网搜索走 DashScope 联网插件，
     * 无需工具；后续自定义工具在此注册）。
     */
    @Bean
    public Toolkit toolkit() {
        return new Toolkit();
    }
}
