package com.example.sqs.ai.rag;

import com.example.sqs.common.exception.BizException;
import com.example.sqs.common.result.ResultCode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 文档文本提取器：txt / md 直接读取，pdf 用 PDFBox 提取。
 */
@Component
public class DocumentTextExtractor {

    /**
     * 提取文档全文文本。
     *
     * @throws BizException 文档解析失败
     */
    public String extract(MultipartFile file, String type) {
        try {
            if ("pdf".equals(type)) {
                return extractPdf(file);
            }
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ResultCode.BAD_REQUEST, "文档解析失败，请确认文件未损坏");
        }
    }

    private String extractPdf(MultipartFile file) throws Exception {
        try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(pdf);
        }
    }
}
