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

    /** DashScope API Key，生产环境通过环境变量 AI_DASHSCOPE_API_KEY 注入。 */
    private String apiKey = "";

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

    private Embedding embedding = new Embedding();

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

    /** 知识库（RAG）配置。 */
    @Data
    public static class Rag {
        private boolean enabled = true;
        private int topK = 4;
        /** 文档分块目标长度（字符）。 */
        private int chunkSize = 600;
        /** 相邻分块重叠长度（字符）。 */
        private int overlap = 80;
        /** 检索时最多参与相似度计算的分块数。 */
        private int scanLimit = 2000;
    }

    /** 联网搜索（DashScope 百炼联网插件）配置。 */
    @Data
    public static class WebSearch {
        private boolean enabled = true;
    }

    /** 向量化（Embedding）配置。 */
    @Data
    public static class Embedding {
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String model = "text-embedding-v4";
        private int dimensions = 1024;
        /** 单次请求最多向量化文本条数。 */
        private int batchSize = 10;
    }
}
