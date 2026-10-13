package com.example.sqs.web.controller;

import com.example.sqs.ai.service.ChatService;
import com.example.sqs.common.result.R;
import com.example.sqs.core.vo.ChatReplyVO;
import com.example.sqs.core.vo.ChatRequest;
import com.example.sqs.security.SecurityUtils;
import com.example.sqs.web.dto.StreamEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 对话接口。流式接口不做统一响应包装，直接下发 SSE 帧。
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityUtils.currentUserId();
        return chatService.stream(userId, request)
                .map(this::toEvent)
                .map(this::toJson)
                .onErrorResume(error -> Flux.just(toJson(StreamEvent.error(error.getMessage()))));
    }

    @PostMapping
    public R<ChatReplyVO> chat(@Valid @RequestBody ChatRequest request) {
        return R.ok(chatService.chat(SecurityUtils.currentUserId(), request));
    }

    private StreamEvent toEvent(String chunk) {
        if (ChatService.DONE.equals(chunk)) {
            return StreamEvent.done();
        }
        if (chunk != null && chunk.startsWith(ChatService.ERROR_PREFIX)) {
            return StreamEvent.error(chunk.substring(ChatService.ERROR_PREFIX.length()));
        }
        return StreamEvent.delta(chunk);
    }

    private String toJson(StreamEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.warn("SSE 事件序列化失败", e);
            return "{\"type\":\"error\",\"content\":\"响应序列化失败\"}";
        }
    }
}
