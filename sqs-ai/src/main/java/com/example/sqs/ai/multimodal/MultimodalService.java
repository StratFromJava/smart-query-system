package com.example.sqs.ai.multimodal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 多模态输入（预留能力）。
 *
 * <p>接入方式：构造带 Media 的 UserMessage，例如
 * {@code new UserMessage(text, List.of(new Media(MimeTypeUtils.IMAGE_PNG, url)))}，
 * 并使用支持视觉的模型（如 qwen-vl-max）。当前仅做占位与校验。
 */
@Slf4j
@Service
public class MultimodalService {

    /**
     * 校验附件是否可用于多模态输入。
     *
     * @param attachmentUrls 附件可访问地址
     * @return 是否通过校验
     */
    public boolean supports(List<String> attachmentUrls) {
        return attachmentUrls != null && attachmentUrls.stream().allMatch(StringUtils::hasText);
    }

    /**
     * 预留：构造多模态消息内容。
     */
    public String describe(List<String> attachmentUrls) {
        log.info("多模态输入接入点被调用, attachments={}", attachmentUrls == null ? 0 : attachmentUrls.size());
        return "文件与图片理解能力正在建设中。";
    }
}
