package com.example.sqs.web.config;

import com.example.sqs.common.exception.BizException;
import com.example.sqs.common.result.R;
import com.example.sqs.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * 全局异常处理，统一转换为 {@link R}。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? ResultCode.BAD_REQUEST.getMsg() : fieldError.getDefaultMessage();
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class})
    public R<Void> handleMissingPart(Exception e) {
        return R.fail(ResultCode.BAD_REQUEST.getCode(), "请求缺少必要的文件或参数");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(ResultCode.SYSTEM_ERROR);
    }
}
