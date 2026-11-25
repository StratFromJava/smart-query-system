package com.example.sqs.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库分块实体（RAG）。
 *
 * <p>embedding 为向量化结果的 JSON 浮点数组文本，由 AI 层写入与检索。
 */
@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private Long userId;

    /** 块在文档内的序号，从 0 开始。 */
    private Integer seq;

    /** 分块文本。 */
    private String content;

    /** 向量（JSON 浮点数组）。 */
    private String embedding;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
