package com.example.sqs.core.service;

import com.example.sqs.core.entity.KnowledgeDocument;
import com.example.sqs.core.vo.KnowledgeDocumentVO;

import java.util.List;

/**
 * 知识库文档服务：按 userId 隔离的文档 CRUD。
 */
public interface KnowledgeDocumentService {

    /**
     * 创建文档记录。
     */
    KnowledgeDocument create(Long userId, String title, String fileName, String fileType,
                             long fileSize, int chunkCount, String status);

    /**
     * 文档入库完成时更新分块数与状态。
     */
    void finish(Long userId, Long documentId, int chunkCount, String status);

    /**
     * 文档列表（按创建时间倒序）。
     */
    List<KnowledgeDocumentVO> list(Long userId);

    /**
     * 校验文档归属，不属于该用户时返回 null。
     */
    KnowledgeDocument get(Long userId, Long documentId);

    /**
     * 删除文档及其全部分块（逻辑删除）。
     */
    void delete(Long userId, Long documentId);
}
