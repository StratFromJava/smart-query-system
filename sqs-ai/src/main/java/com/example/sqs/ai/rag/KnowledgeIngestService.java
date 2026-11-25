package com.example.sqs.ai.rag;

import com.example.sqs.ai.properties.AiProperties;
import com.example.sqs.common.exception.BizException;
import com.example.sqs.common.result.ResultCode;
import com.example.sqs.core.entity.KnowledgeChunk;
import com.example.sqs.core.entity.KnowledgeDocument;
import com.example.sqs.core.mapper.KnowledgeChunkMapper;
import com.example.sqs.core.service.KnowledgeDocumentService;
import com.example.sqs.core.vo.KnowledgeDocumentVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * 知识库文档入库：提取文本 → 分块 → 向量化 → 落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIngestService {

    private static final Set<String> SUPPORTED_TYPES = Set.of("txt", "md", "pdf");

    private final KnowledgeDocumentService documentService;
    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingClient embeddingClient;
    private final DocumentTextExtractor textExtractor;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    /**
     * 解析并入库一份文档。
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeDocumentVO ingest(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "请选择要上传的文档");
        }
        String fileName = file.getOriginalFilename();
        String type = fileType(fileName);
        if (type == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "仅支持 txt / md / pdf 文档");
        }
        String text = textExtractor.extract(file, type);
        List<String> chunks = TextChunker.chunk(text,
                aiProperties.getRag().getChunkSize(), aiProperties.getRag().getOverlap());
        if (chunks.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "文档内容为空或无法解析出文本");
        }

        KnowledgeDocument document = documentService.create(userId, title(fileName), fileName,
                type, file.getSize(), 0, "PROCESSING");
        try {
            List<float[]> vectors = embeddingClient.embedAll(chunks);
            saveChunks(userId, document.getId(), chunks, vectors);
            documentService.finish(userId, document.getId(), chunks.size(), "READY");
            log.info("知识库文档入库完成, userId={}, documentId={}, chunks={}",
                    userId, document.getId(), chunks.size());
        } catch (Exception e) {
            log.error("知识库文档入库失败, userId={}, fileName={}", userId, fileName, e);
            documentService.finish(userId, document.getId(), 0, "FAILED");
            throw new BizException(ResultCode.AI_SERVICE_ERROR, "文档向量化失败，请检查 API Key 或稍后重试");
        }
        return toVO(document, chunks.size(), "READY");
    }

    private void saveChunks(Long userId, Long documentId, List<String> chunks, List<float[]> vectors)
            throws Exception {
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocumentId(documentId);
            chunk.setUserId(userId);
            chunk.setSeq(i);
            chunk.setContent(chunks.get(i));
            chunk.setEmbedding(objectMapper.writeValueAsString(vectors.get(i)));
            chunkMapper.insert(chunk);
        }
    }

    private KnowledgeDocumentVO toVO(KnowledgeDocument document, int chunkCount, String status) {
        KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
        vo.setId(document.getId());
        vo.setTitle(document.getTitle());
        vo.setFileName(document.getFileName());
        vo.setFileType(document.getFileType());
        vo.setFileSize(document.getFileSize());
        vo.setChunkCount(chunkCount);
        vo.setStatus(status);
        return vo;
    }

    private String fileType(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        String ext = fileName.substring(dot + 1).toLowerCase();
        return SUPPORTED_TYPES.contains(ext) ? ext : null;
    }

    private String title(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        return base.isBlank() ? "未命名文档" : base;
    }
}
