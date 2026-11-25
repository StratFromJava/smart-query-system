package com.example.sqs.core.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档视图对象。
 */
@Data
public class KnowledgeDocumentVO {

    private Long id;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer chunkCount;
    private String status;
    private LocalDateTime createTime;
}
