package com.example.sqs.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档实体（RAG）。
 */
@Data
@TableName("knowledge_document")
public class KnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 展示标题。 */
    private String title;

    /** 原始文件名。 */
    private String fileName;

    /** 文件类型：txt / md / pdf。 */
    private String fileType;

    /** 文件大小（字节）。 */
    private Long fileSize;

    /** 分块数量。 */
    private Integer chunkCount;

    /** 状态：READY / FAILED。 */
    private String status;

    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
