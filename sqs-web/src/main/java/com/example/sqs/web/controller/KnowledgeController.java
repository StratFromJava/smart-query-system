package com.example.sqs.web.controller;

import com.example.sqs.ai.rag.KnowledgeIngestService;
import com.example.sqs.common.result.R;
import com.example.sqs.core.service.KnowledgeDocumentService;
import com.example.sqs.core.vo.KnowledgeDocumentVO;
import com.example.sqs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库接口（RAG）：文档上传入库、列表与删除，按登录用户隔离。
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeIngestService knowledgeIngestService;
    private final KnowledgeDocumentService knowledgeDocumentService;

    /**
     * 上传文档并入库（txt / md / pdf，自动分块向量化）。
     */
    @PostMapping("/documents")
    public R<KnowledgeDocumentVO> upload(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.currentUserId();
        return R.ok(knowledgeIngestService.ingest(userId, file));
    }

    /**
     * 文档列表（按创建时间倒序）。
     */
    @GetMapping("/documents")
    public R<List<KnowledgeDocumentVO>> list() {
        return R.ok(knowledgeDocumentService.list(SecurityUtils.currentUserId()));
    }

    /**
     * 删除文档及其分块。
     */
    @DeleteMapping("/documents/{id}")
    public R<Void> delete(@PathVariable Long id) {
        knowledgeDocumentService.delete(SecurityUtils.currentUserId(), id);
        return R.ok(null);
    }
}
