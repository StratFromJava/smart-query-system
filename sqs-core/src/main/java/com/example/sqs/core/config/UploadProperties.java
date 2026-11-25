package com.example.sqs.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件上传配置，前缀 {@code sqs.upload}。
 */
@Data
@Component
@ConfigurationProperties(prefix = "sqs.upload")
public class UploadProperties {

    /** 上传文件存储目录（静态资源 /uploads/** 映射到此目录）。 */
    private String dir = "./data/uploads";
}
