package com.example.sqs.web.controller;

import com.example.sqs.common.result.R;
import com.example.sqs.core.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传接口（图片），静态访问走 /uploads/**。
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private static final Set<String> IMAGE_TYPES = Set.of("png", "jpg", "jpeg", "gif", "webp", "bmp");

    private final UploadProperties uploadProperties;

    @PostMapping
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.fail(400, "请选择要上传的图片");
        }
        String ext = ext(file.getOriginalFilename());
        if (ext == null) {
            return R.fail(400, "仅支持 png/jpg/jpeg/gif/webp/bmp 图片");
        }
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            // transferTo 对相对路径会解析到 Tomcat 临时目录，必须先转为绝对路径
            Path dir = Path.of(uploadProperties.getDir(), dateDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(name).toFile());
            log.info("图片上传成功, size={}, url=/uploads/{}/{}", file.getSize(), dateDir, name);
            return R.ok(Map.of("url", "/uploads/" + dateDir + "/" + name));
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return R.fail(5000, "图片上传失败，请稍后重试");
        }
    }

    private String ext(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        String e = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return IMAGE_TYPES.contains(e) ? e : null;
    }
}
