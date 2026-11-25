package com.example.sqs.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sqs.core.entity.KnowledgeChunk;
import com.example.sqs.core.entity.KnowledgeDocument;
import com.example.sqs.core.mapper.KnowledgeChunkMapper;
import com.example.sqs.core.mapper.KnowledgeDocumentMapper;
import com.example.sqs.core.service.KnowledgeDocumentService;
import com.example.sqs.core.vo.KnowledgeDocumentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识库文档服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;

    @Override
    public KnowledgeDocument create(Long userId, String title, String fileName, String fileType,
                                    long fileSize, int chunkCount, String status) {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setUserId(userId);
        document.setTitle(title);
        document.setFileName(fileName);
        document.setFileType(fileType);
        document.setFileSize(fileSize);
        document.setChunkCount(chunkCount);
        document.setStatus(status);
        knowledgeDocumentMapper.insert(document);
        return document;
    }

    @Override
    public void finish(Long userId, Long documentId, int chunkCount, String status) {
        KnowledgeDocument document = get(userId, documentId);
        if (document == null) {
            return;
        }
        document.setChunkCount(chunkCount);
        document.setStatus(status);
        knowledgeDocumentMapper.updateById(document);
    }

    @Override
    public List<KnowledgeDocumentVO> list(Long userId) {
        List<KnowledgeDocument> documents = knowledgeDocumentMapper.selectList(
                new LambdaQueryWrapper<KnowledgeDocument>()
                        .eq(KnowledgeDocument::getUserId, userId)
                        .orderByDesc(KnowledgeDocument::getId));
        List<KnowledgeDocumentVO> list = new ArrayList<>(documents.size());
        for (KnowledgeDocument document : documents) {
            KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
            BeanUtils.copyProperties(document, vo);
            list.add(vo);
        }
        return list;
    }

    @Override
    public KnowledgeDocument get(Long userId, Long documentId) {
        return knowledgeDocumentMapper.selectOne(new LambdaQueryWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getId, documentId)
                .eq(KnowledgeDocument::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long documentId) {
        KnowledgeDocument document = get(userId, documentId);
        if (document == null) {
            return;
        }
        int rows = knowledgeChunkMapper.delete(new LambdaQueryWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getDocumentId, documentId)
                .eq(KnowledgeChunk::getUserId, userId));
        knowledgeDocumentMapper.deleteById(document.getId());
        log.info("删除知识库文档, userId={}, documentId={}, chunks={}", userId, documentId, rows);
    }
}
