package com.taobao.common;

/*
 @auther:Jimi
 @description: 统一响应类
 */

import lombok.Data;

@Data
public class R<T> {
    // 状态码 200 成功, 500 失败
    private int code;
    // 提示信息
    private String msg;
    // 数据
    private T data;

    // 成功
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> success(String msg, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    // 失败
    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }

    public static <T> R<T> error(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }
}
