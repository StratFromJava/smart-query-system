package com.example.sqs.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * smart-query-system 启动类。
 */
@MapperScan("com.example.sqs.core.mapper")
@SpringBootApplication(scanBasePackages = "com.example.sqs")
public class SmartQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartQueryApplication.class, args);
    }
}
