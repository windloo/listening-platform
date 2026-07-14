package com.windloo.common.api;
import lombok.Data;

@Data
public class JsonResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> JsonResponse<T> ok() { return ok(null); }
    public static <T> JsonResponse<T> ok(T data) {
        JsonResponse<T> r = new JsonResponse<>();
        r.code = 0; r.msg = "success"; r.data = data;
        return r;
    }
    public static <T> JsonResponse<T> fail(int code, String msg) {
        JsonResponse<T> r = new JsonResponse<>();
        r.code = code; r.msg = msg; r.data = null;
        return r;
    }
}