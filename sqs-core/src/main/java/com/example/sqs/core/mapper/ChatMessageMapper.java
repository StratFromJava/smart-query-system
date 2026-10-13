package com.example.sqs.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sqs.core.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息 Mapper。
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 按会话取最近 N 条消息（正序返回，供模型上下文使用）。
     *
     * <p>自定义 SQL 不受逻辑删除插件自动过滤影响，故显式带上 deleted = 0。
     */
    @Select("""
            SELECT id, session_id, user_id, role, content, model, tokens, create_time
            FROM (
                SELECT id, session_id, user_id, role, content, model, tokens, create_time
                FROM chat_message
                WHERE session_id = #{sessionId}
                  AND user_id = #{userId}
                  AND deleted = 0
                ORDER BY id DESC
                LIMIT #{limit}
            ) t
            ORDER BY t.id ASC
            """)
    List<ChatMessage> selectRecent(@Param("sessionId") Long sessionId,
                                   @Param("userId") Long userId,
                                   @Param("limit") int limit);
}
