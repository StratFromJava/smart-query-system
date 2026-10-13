package com.example.sqs.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 REST 响应封装。
 *
 * <p>注意：SSE 流式接口不应使用该包装，直接返回 Flux&lt;String&gt;。
 */
@Data
public class R<T> implements Serializable {

    private int code;
    private String msg;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMsg(ResultCode.SUCCESS.getMsg());
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMsg());
    }

    public static <T> R<T> fail(ResultCode resultCode, String msg) {
        return fail(resultCode.getCode(), msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
