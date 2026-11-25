package com.example.sqs.ai.tool;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 联网搜索工具（预留能力）。
 *
 * <p>开启 sqs.ai.web-search.enabled=true 后，由 AgentConfig 注册进全局
 * {@link io.agentscope.core.tool.Toolkit}，供模型自主调用。
 * 当前返回固定文案，接入真实搜索服务时替换 {@link #search(String)} 实现即可。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "sqs.ai.web-search", name = "enabled", havingValue = "true")
public class WebSearchTool {

    @Tool(name = "web_search", description = "联网搜索实时信息，用于回答需要最新数据的问题")
    public String search(
            @ToolParam(name = "query", description = "搜索关键词") String query) {
        log.info("触发联网搜索工具, query={}", query);
        return "联网搜索能力正在建设中，暂未返回实时结果。";
    }
}
