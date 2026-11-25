package com.example.sqs.ai.service;

import com.example.sqs.ai.config.AgentFactory;
import com.example.sqs.ai.model.ModelRegistry;
import com.example.sqs.ai.multimodal.MultimodalService;
import com.example.sqs.ai.properties.AiProperties;
import com.example.sqs.ai.rag.RagService;
import com.example.sqs.common.enums.FeatureType;
import com.example.sqs.core.entity.ChatMessage;
import com.example.sqs.core.enums.MessageRole;
import com.example.sqs.core.service.ChatMessageService;
import com.example.sqs.core.service.ChatSessionService;
import com.example.sqs.core.vo.ChatReplyVO;
import com.example.sqs.core.vo.ChatRequest;
import com.example.sqs.core.vo.SessionVO;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.message.AssistantMessage;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 对话编排：会话解析 → 历史上下文 → 可选增强（RAG）→ AgentScope Agent 调用 → 落库。
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

    private final AgentFactory agentFactory;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;
    private final ModelRegistry modelRegistry;
    private final AiProperties aiProperties;
    private final MultimodalService multimodalService;
    private final ObjectProvider<RagService> ragServiceProvider;

    /**
     * 流式对话。返回文本增量流，末尾追加 {@link #DONE}，异常时追加 {@link #ERROR_PREFIX}。
     */
    public Flux<String> stream(Long userId, ChatRequest request) {
        return Flux.defer(() -> {
            String model = modelRegistry.resolve(request.getModel());
            String content = request.getContent().trim();
            Long sessionId = resolveSession(userId, request, model);
            boolean multimodal = modelRegistry.isMultimodal(model);

            String storedContent = multimodalService.appendImages(content, request.getAttachments());
            chatMessageService.save(sessionId, userId, MessageRole.USER, storedContent, model, null);
            chatSessionService.updateModel(userId, sessionId, model);

            List<Msg> context = buildContext(userId, sessionId, aiProperties.getHistoryLimit(), multimodal);
            String systemPrompt = buildSystemPrompt(userId, request, content);
            log.info("发起对话, userId={}, sessionId={}, model={}, historySize={}",
                    userId, sessionId, model, context.size());

            ReActAgent agent = agentFactory.create(model, systemPrompt,
                    hasFeature(request, FeatureType.WEB_SEARCH));
            StringBuilder answer = new StringBuilder();
            Flux<String> tokenFlux = agent.streamEvents(context)
                    .filter(TextBlockDeltaEvent.class::isInstance)
                    .map(event -> ((TextBlockDeltaEvent) event).getDelta())
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
        boolean multimodal = modelRegistry.isMultimodal(model);

        String storedContent = multimodalService.appendImages(content, request.getAttachments());
        chatMessageService.save(sessionId, userId, MessageRole.USER, storedContent, model, null);
        chatSessionService.updateModel(userId, sessionId, model);

        List<Msg> context = buildContext(userId, sessionId, aiProperties.getHistoryLimit(), multimodal);
        String systemPrompt = buildSystemPrompt(userId, request, content);

        ReActAgent agent = agentFactory.create(model, systemPrompt,
                hasFeature(request, FeatureType.WEB_SEARCH));
        Msg reply = agent.call(context, RuntimeContext.empty()).block();

        String answer = reply == null ? "" : reply.getTextContent();
        if (!StringUtils.hasText(answer)) {
            answer = "抱歉，模型未返回有效内容，请稍后重试。";
        }
        chatMessageService.save(sessionId, userId, MessageRole.ASSISTANT, answer, model, null);
        applyAutoTitle(userId, sessionId);

        ChatReplyVO vo = new ChatReplyVO();
        vo.setSessionId(sessionId);
        vo.setContent(answer);
        vo.setModel(model);
        return vo;
    }

    /**
     * 从数据库加载会话最近 N 条消息并转为 AgentScope 消息序列。
     *
     * <p>最近一条即本次刚落库的用户提问，随上下文一起传入模型；
     * 历史持久化在库而非进程内存，重启后上下文不丢失。
     * 用户消息中的图片（Markdown 语法）在多模态模型下解析为 DataBlock。
     */
    private List<Msg> buildContext(Long userId, Long sessionId, int limit, boolean multimodal) {
        List<ChatMessage> records = chatMessageService.recent(userId, sessionId, limit);
        List<Msg> messages = new ArrayList<>(records.size());
        for (ChatMessage record : records) {
            String recordContent = record.getContent();
            if (recordContent == null) {
                continue;
            }
            if (MessageRole.ASSISTANT.getValue().equals(record.getRole())) {
                messages.add(new AssistantMessage(recordContent));
                continue;
            }
            List<String> imageUrls = multimodalService.extractImageUrls(recordContent);
            String text = multimodalService.stripImages(recordContent);
            if (multimodal && !imageUrls.isEmpty()) {
                messages.add(new UserMessage(
                        multimodalService.buildUserBlocks(text, imageUrls, true)));
            } else {
                messages.add(new UserMessage(text));
            }
        }
        return messages;
    }

    private Long resolveSession(Long userId, ChatRequest request, String model) {
        Long sessionId = request.getSessionId();
        if (sessionId != null) {
            chatSessionService.checkOwner(userId, sessionId);
            return sessionId;
        }
        return chatSessionService.create(userId, buildTitle(request.getContent()), model).getId();
    }

    private String buildSystemPrompt(Long userId, ChatRequest request, String question) {
        StringBuilder prompt = new StringBuilder(aiProperties.getSystemPrompt());
        RagService ragService = ragServiceProvider.getIfAvailable();
        if (ragService != null && hasFeature(request, FeatureType.RAG)) {
            String context = ragService.search(userId, question);
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

    private String safeMessage(Throwable error) {
        log.error("对话失败", error);
        String message = error.getMessage();
        return StringUtils.hasText(message) ? message : "AI 服务调用失败，请稍后重试";
    }
}
