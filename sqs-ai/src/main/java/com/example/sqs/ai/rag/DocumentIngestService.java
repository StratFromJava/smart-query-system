package com.example.sqs.ai.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 文档入库（预留能力）。
 *
 * <p>当前为最简实现：直接按传入文本切片入库。后续可接入
 * TikaDocumentReader / PagePdfDocumentReader 做真实文档解析与 TokenTextSplitter 切片。
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "sqs.ai.rag", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class DocumentIngestService {

    private final VectorStore vectorStore;

    /**
     * 将一段文本写入知识库。
     *
     * @param docId    文档标识，用于后续删除
     * @param content  文档正文
     * @return 实际写入的片段数量
     */
    public int ingest(String docId, String content) {
        if (!StringUtils.hasText(content)) {
            return 0;
        }
        List<Document> documents = List.of(new Document(content, java.util.Map.of("docId", docId)));
        vectorStore.add(documents);
        log.info("文档入库完成, docId={}", docId);
        return documents.size();
    }

    public void remove(String docId) {
        vectorStore.delete(List.of(docId));
    }
}
