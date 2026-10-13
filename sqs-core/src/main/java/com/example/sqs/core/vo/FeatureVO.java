package com.example.sqs.core.vo;

import com.example.sqs.common.enums.FeatureType;
import lombok.Data;

/**
 * 能力视图对象，驱动前端功能开关显隐。
 */
@Data
public class FeatureVO {

    private String code;
    private String name;
    private String desc;
    private boolean implemented;

    public static FeatureVO from(FeatureType type) {
        FeatureVO vo = new FeatureVO();
        vo.setCode(type.getCode());
        vo.setName(type.getName());
        vo.setDesc(type.getDesc());
        vo.setImplemented(type.isImplemented());
        return vo;
    }
}
