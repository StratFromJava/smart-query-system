package com.example.sqs.ai.multimodal;

import com.example.sqs.common.exception.BizException;
import com.example.sqs.common.result.ResultCode;
import com.example.sqs.core.config.UploadProperties;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.DataBlock;
import io.agentscope.core.message.Base64Source;
import io.agentscope.core.message.TextBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多模态支持：把消息中的图片（/uploads/** 相对地址）转为 AgentScope DataBlock。
 *
 * <p>图片以 Markdown 语法 {@code ![](url)} 拼在消息文本后持久化，
 * 构建模型上下文时解析为 TextBlock + DataBlock（base64）传给视觉模型。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultimodalService {

    private static final Pattern IMAGE_MD =
            Pattern.compile("!\\[[^\\]]*\\]\\((/uploads/[^)\\s]+)\\)");
    private static final Map<String, String> MEDIA_TYPES = Map.of(
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "gif", "image/gif",
            "webp", "image/webp",
            "bmp", "image/bmp");

    private final UploadProperties uploadProperties;

    /**
     * 把附件地址以 Markdown 图片语法拼接到消息文本后（用于持久化与前端渲染）。
     */
    public String appendImages(String content, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return content;
        }
        StringBuilder sb = new StringBuilder(content == null ? "" : content.strip());
        for (String url : imageUrls) {
            requireImage(url);
            sb.append("\n\n![image](").append(url).append(')');
        }
        return sb.toString();
    }

    /**
     * 从消息文本中解析图片地址。
     */
    public List<String> extractImageUrls(String content) {
        List<String> urls = new ArrayList<>();
        if (content == null) {
            return urls;
        }
        Matcher matcher = IMAGE_MD.matcher(content);
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }
        return urls;
    }

    /**
     * 去掉消息文本中的 Markdown 图片（构建纯文本上下文用）。
     */
    public String stripImages(String content) {
        if (content == null) {
            return "";
        }
        return IMAGE_MD.matcher(content).replaceAll("").strip();
    }

    /**
     * 构建用户消息内容块：文本块 + 图片块（本地文件读为 base64）。
     *
     * @param text        纯文本（已去掉图片 Markdown）
     * @param imageUrls   图片相对地址列表
     * @param withImage   是否携带图片（模型不支持视觉时不携带）
     */
    public List<ContentBlock> buildUserBlocks(String text, List<String> imageUrls, boolean withImage) {
        List<ContentBlock> blocks = new ArrayList<>();
        if (StringUtils.hasText(text)) {
            blocks.add(TextBlock.builder().text(text).build());
        }
        if (withImage && imageUrls != null) {
            for (String url : imageUrls) {
                blocks.add(toImageBlock(url));
            }
        }
        return blocks;
    }

    private DataBlock toImageBlock(String url) {
        requireImage(url);
        Path path = resolve(url);
        try {
            byte[] bytes = Files.readAllBytes(path);
            String mediaType = mediaType(url);
            log.debug("构建图片块, url={}, size={}B, mediaType={}", url, bytes.length, mediaType);
            return DataBlock.builder()
                    .source(Base64Source.builder()
                            .mediaType(mediaType)
                            .data(Base64.getEncoder().encodeToString(bytes))
                            .build())
                    .build();
        } catch (Exception e) {
            throw new BizException(ResultCode.BAD_REQUEST, "图片读取失败：" + url);
        }
    }

    private void requireImage(String url) {
        if (!StringUtils.hasText(url) || mediaType(url) == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "仅支持 png/jpg/jpeg/gif/webp/bmp 图片");
        }
    }

    private Path resolve(String url) {
        String relative = url.substring("/uploads/".length());
        if (relative.contains("..") || relative.contains(":")) {
            throw new BizException(ResultCode.BAD_REQUEST, "非法图片地址");
        }
        Path base = Path.of(uploadProperties.getDir()).toAbsolutePath().normalize();
        Path path = base.resolve(relative).normalize();
        if (!path.startsWith(base)) {
            throw new BizException(ResultCode.BAD_REQUEST, "非法图片地址");
        }
        return path;
    }

    private String mediaType(String url) {
        String lower = url.toLowerCase();
        int dot = lower.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        return MEDIA_TYPES.get(lower.substring(dot + 1));
    }
}
