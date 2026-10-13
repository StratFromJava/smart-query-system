package com.example.sqs.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 单条事件。JSON 序列化后作为 {@code data:} 内容下发，
 * 避免回答文本中的换行破坏 SSE 帧结构。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamEvent {

    /** delta：文本增量；done：流结束；error：出错。 */
    private String type;

    /** delta 为文本片段，error 为错误描述。 */
    private String content;

    public static StreamEvent delta(String content) {
        return new StreamEvent("delta", content);
    }

    public static StreamEvent done() {
        return new StreamEvent("done", null);
    }

    public static StreamEvent error(String message) {
        return new StreamEvent("error", message);
    }
}
