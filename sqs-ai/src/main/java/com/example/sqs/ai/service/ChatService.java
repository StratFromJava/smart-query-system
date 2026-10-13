package com.example.sqs.ai.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.sqs.ai.model.ModelRegistry;
import com.example.sqs.ai.properties.AiProperties;
import com.example.sqs.ai.rag.RagService;
import com.example.sqs.common.enums.FeatureType;
import com.example.sqs.core.enums.MessageRole;
import com.example.sqs.core.service.ChatMessageService;
import com.example.sqs.core.service.ChatSessionService;
import com.example.sqs.core.vo.ChatReplyVO;
import com.example.sqs.core.vo.ChatRequest;
import com.example.sqs.core.vo.SessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

/**
 * 对话编排：会话解析 → 历史上下文 → 可选增强（RAG）→ 模型调用 → 落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    /** 流式结束哨兵。 */
    public static final String DONE = "[DONE]";
    /** 流式错误前缀。 */
    public static final String ERROR_PREFIX = "[ERROR] ";

    private static final String DEFAULT_TITLE = "新对话";
    private static final int TITLE_MAX_LENGTH = 30;

    private final ChatClient chatClient;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ConversationHistoryLoader historyLoader;
    private final ModelRegistry modelRegistry;
    private final AiProperties aiProperties;
    private final ObjectProvider<RagService> ragServiceProvider;

    /**
     * 流式对话。返回文本增量流，末尾追加 {@link #DONE}，异常时追加 {@link #ERROR_PREFIX}。
     */
    public Flux<String> stream(Long userId, ChatRequest request) {
        return Flux.defer(() -> {
            String model = modelRegistry.resolve(request.getModel());
            String content = request.getContent().trim();
            Long sessionId = resolveSession(userId, request, model);

            chatMessageService.save(sessionId, userId, MessageRole.USER, content, model, null);
            chatSessionService.updateModel(userId, sessionId, model);

            List<Message> history = historyLoader.load(userId, sessionId, aiProperties.getHistoryLimit());
            String systemPrompt = buildSystemPrompt(request, content);
            log.info("发起对话, userId={}, sessionId={}, model={}, historySize={}", userId, sessionId, model, history.size());

            StringBuilder answer = new StringBuilder();
            Flux<String> tokenFlux = chatClient.prompt()
                    .system(systemPrompt)
                    .messages(history)
                    .options(buildOptions(model))
                    .stream()
                    .chatResponse()
                    .map(this::extractText)
                    .filter(StringUtils::hasLength)
                    .doOnNext(answer::append);

            return tokenFlux
                    .concatWith(Flux.defer(() -> {
                        if (!answer.isEmpty()) {
                            chatMessageService.save(sessionId, userId, MessageRole.ASSISTANT,
                                    answer.toString(), model, null);
                            applyAutoTitle(userId, sessionId);
                        }
                        return Flux.just(DONE);
                    }))
                    .onErrorResume(error -> Flux.just(ERROR_PREFIX + safeMessage(error)));
        });
    }

    /**
     * 非流式对话（预留），一次性返回完整内容。
     */
    public ChatReplyVO chat(Long userId, ChatRequest request) {
        String model = modelRegistry.resolve(request.getModel());
        String content = request.getContent().trim();
        Long sessionId = resolveSession(userId, request, model);

        chatMessageService.save(sessionId, userId, MessageRole.USER, content, model, null);
        chatSessionService.updateModel(userId, sessionId, model);

        List<Message> history = historyLoader.load(userId, sessionId, aiProperties.getHistoryLimit());
        String systemPrompt = buildSystemPrompt(request, content);

        ChatResponse response = chatClient.prompt()
                .system(systemPrompt)
                .messages(history)
                .options(buildOptions(model))
                .call()
                .chatResponse();

        String answer = extractText(response);
        if (!StringUtils.hasText(answer)) {
            answer = "抱歉，模型未返回有效内容，请稍后重试。";
        }
        chatMessageService.save(sessionId, userId, MessageRole.ASSISTANT, answer, model, null);
        applyAutoTitle(userId, sessionId);

        ChatReplyVO reply = new ChatReplyVO();
        reply.setSessionId(sessionId);
        reply.setContent(answer);
        reply.setModel(model);
        return reply;
    }

    private DashScopeChatOptions buildOptions(String model) {
        return DashScopeChatOptions.builder()
                .withModel(model)
                .withTemperature(aiProperties.getTemperature())
                .withIncrementalOutput(true)
                .build();
    }

    private Long resolveSession(Long userId, ChatRequest request, String model) {
        Long sessionId = request.getSessionId();
        if (sessionId != null) {
            chatSessionService.checkOwner(userId, sessionId);
            return sessionId;
        }
        return chatSessionService.create(userId, buildTitle(request.getContent()), model).getId();
    }

    private String buildSystemPrompt(ChatRequest request, String question) {
        StringBuilder prompt = new StringBuilder(aiProperties.getSystemPrompt());
        RagService ragService = ragServiceProvider.getIfAvailable();
        if (ragService != null && hasFeature(request, FeatureType.RAG)) {
            String context = ragService.search(question);
            if (StringUtils.hasText(context)) {
                prompt.append("\n\n请优先参考以下资料作答，资料不足时再用自身知识补充：\n")
                        .append(context);
            }
        }
        return prompt.toString();
    }

    private boolean hasFeature(ChatRequest request, FeatureType type) {
        Set<String> features = request.getFeatures();
        return features != null && features.contains(type.getCode());
    }

    /**
     * 首条提问时把默认标题替换为提问摘要。
     */
    private void applyAutoTitle(Long userId, Long sessionId) {
        SessionVO session = chatSessionService.get(userId, sessionId);
        if (session == null || !DEFAULT_TITLE.equals(session.getTitle())) {
            return;
        }
        String firstQuestion = chatMessageService.firstUserContent(userId, sessionId);
        if (StringUtils.hasText(firstQuestion)) {
            chatSessionService.rename(userId, sessionId, buildTitle(firstQuestion));
        }
    }

    private String buildTitle(String content) {
        if (!StringUtils.hasText(content)) {
            return DEFAULT_TITLE;
        }
        String singleLine = content.replaceAll("\\s+", " ").trim();
        return singleLine.length() <= TITLE_MAX_LENGTH
                ? singleLine
                : singleLine.substring(0, TITLE_MAX_LENGTH) + "…";
    }

    private String extractText(ChatResponse response) {
        if (response == null) {
            return "";
        }
        Generation generation = response.getResult();
        if (generation == null) {
            return "";
        }
        AssistantMessage message = generation.getOutput();
        return message == null || message.getText() == null ? "" : message.getText();
    }

    private String safeMessage(Throwable error) {
        log.error("对话失败", error);
        String message = error.getMessage();
        return StringUtils.hasText(message) ? message : "AI 服务调用失败，请稍后重试";
    }
}
