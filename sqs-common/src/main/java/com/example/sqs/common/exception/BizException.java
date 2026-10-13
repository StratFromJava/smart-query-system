package com.example.sqs.common.exception;

import com.example.sqs.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：由全局异常处理器捕获并转换为统一响应。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String msg) {
        super(msg);
        this.code = resultCode.getCode();
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
