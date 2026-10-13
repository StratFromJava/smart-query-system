package com.example.sqs.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 相关可配项，前缀 {@code sqs.ai}。
 */
@Data
@ConfigurationProperties(prefix = "sqs.ai")
public class AiProperties {

    /** 默认模型。 */
    private String defaultModel = "qwen-plus";

    /** 采样温度。 */
    private double temperature = 0.7;

    /** 组装上下文时携带的历史消息条数。 */
    private int historyLimit = 20;

    /** 系统提示词。 */
    private String systemPrompt = """
            你是 SmartQuery 智能助手，一个专业、简洁、友好的中文 AI 助手。
            回答请使用 Markdown 排版：长内容分条陈述，代码放入带语言标注的代码块。
            不确定时应明确说明不确定，不要编造事实。
            """;

    /** 可选模型清单，驱动前端下拉框。 */
    private List<ModelDef> models = new ArrayList<>(List.of(
            new ModelDef("qwen-plus", "通义千问 Plus"),
            new ModelDef("qwen-max", "通义千问 Max"),
            new ModelDef("qwen-turbo", "通义千问 Turbo")
    ));

    private Rag rag = new Rag();

    private WebSearch webSearch = new WebSearch();

    /** 单个模型定义。 */
    @Data
    public static class ModelDef {
        private String code;
        private String label;
        private boolean multimodal;

        public ModelDef() {
        }

        public ModelDef(String code, String label) {
            this(code, label, false);
        }

        public ModelDef(String code, String label, boolean multimodal) {
            this.code = code;
            this.label = label;
            this.multimodal = multimodal;
        }
    }

    /** 知识库（RAG）预留配置，默认关闭。 */
    @Data
    public static class Rag {
        private boolean enabled = false;
        private int topK = 4;
    }

    /** 联网搜索预留配置，默认关闭。 */
    @Data
    public static class WebSearch {
        private boolean enabled = false;
    }
}
