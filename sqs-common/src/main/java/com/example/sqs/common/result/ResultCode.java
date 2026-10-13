package com.example.sqs.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应码。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已失效"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "资源不存在"),
    USERNAME_EXISTS(1001, "用户名已被注册"),
    PASSWORD_ERROR(1002, "用户名或密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    AI_SERVICE_ERROR(2001, "AI 服务调用失败"),
    AI_MODEL_NOT_SUPPORTED(2002, "暂不支持该模型"),
    FEATURE_NOT_READY(2003, "该功能正在建设中"),
    SYSTEM_ERROR(5000, "系统内部错误");

    private final int code;
    private final String msg;
}
