package com.example.sqs.web.controller;

import com.example.sqs.common.result.R;
import com.example.sqs.core.service.ChatMessageService;
import com.example.sqs.core.service.ChatSessionService;
import com.example.sqs.core.vo.MessageVO;
import com.example.sqs.core.vo.SessionVO;
import com.example.sqs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 会话接口。所有数据按当前登录用户隔离。
 */
@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    @GetMapping
    public R<List<SessionVO>> list(@RequestParam(required = false) String keyword) {
        Long userId = SecurityUtils.currentUserId();
        List<SessionVO> sessions = (keyword == null || keyword.isBlank())
                ? chatSessionService.list(userId)
                : chatSessionService.search(userId, keyword.trim());
        return R.ok(sessions);
    }

    @PostMapping
    public R<SessionVO> create(@RequestBody(required = false) Map<String, String> body) {
        Long userId = SecurityUtils.currentUserId();
        String title = body == null ? null : body.get("title");
        String model = body == null ? null : body.get("model");
        return R.ok(chatSessionService.create(userId, title, model));
    }

    @GetMapping("/{sessionId}/messages")
    public R<List<MessageVO>> messages(@PathVariable Long sessionId) {
        Long userId = SecurityUtils.currentUserId();
        chatSessionService.checkOwner(userId, sessionId);
        return R.ok(chatMessageService.listBySession(userId, sessionId));
    }

    @PatchMapping("/{sessionId}/title")
    public R<Void> rename(@PathVariable Long sessionId, @RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.currentUserId();
        chatSessionService.rename(userId, sessionId, body.get("title"));
        return R.ok();
    }

    @DeleteMapping("/{sessionId}")
    public R<Void> remove(@PathVariable Long sessionId) {
        Long userId = SecurityUtils.currentUserId();
        chatSessionService.remove(userId, sessionId);
        return R.ok();
    }
}
