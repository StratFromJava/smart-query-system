package com.example.sqs.web.controller;

import com.example.sqs.ai.model.ModelRegistry;
import com.example.sqs.ai.properties.AiProperties;
import com.example.sqs.common.enums.FeatureType;
import com.example.sqs.common.result.R;
import com.example.sqs.core.vo.FeatureVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 元数据接口：驱动前端模型下拉与功能开关显隐。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetaController {

    private final ModelRegistry modelRegistry;
    private final AiProperties aiProperties;

    @GetMapping("/models")
    public R<List<AiProperties.ModelDef>> models() {
        return R.ok(new ArrayList<>(modelRegistry.list()));
    }

    @GetMapping("/models/default")
    public R<String> defaultModel() {
        return R.ok(aiProperties.getDefaultModel());
    }

    @GetMapping("/features")
    public R<List<FeatureVO>> features() {
        List<FeatureVO> list = new ArrayList<>();
        for (FeatureType type : FeatureType.values()) {
            list.add(FeatureVO.from(type));
        }
        return R.ok(list);
    }
}
