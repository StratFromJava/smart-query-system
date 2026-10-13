package com.example.sqs.core.service;

import com.example.sqs.core.vo.SessionVO;

import java.util.List;

/**
 * 会话领域服务。所有方法都强制带 userId，确保数据隔离。
 */
public interface ChatSessionService {

    SessionVO create(Long userId, String title, String model);

    List<SessionVO> list(Long userId);

    List<SessionVO> search(Long userId, String keyword);

    /**
     * 查询单个会话。越权或不存在时返回 null（由上层转换为 404）。
     */
    SessionVO get(Long userId, Long sessionId);

    void rename(Long userId, Long sessionId, String title);

    void remove(Long userId, Long sessionId);

    void updateModel(Long userId, Long sessionId, String model);

    /**
     * 校验会话归属，越权时抛业务异常（404，避免会话 ID 枚举探测）。
     */
    void checkOwner(Long userId, Long sessionId);
}
