package com.windloo.common.exception;
import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),
    LOGIN_FAILED(40001, "登录失败"),
    USER_EXISTS(40002, "用户已存在"),
    USER_NOT_FOUND(40003, "用户不存在"),
    PASSWORD_INVALID(40004, "密码不合法"),
    CODE_INVALID(40005, "验证码无效或已过期"),
    INIT_DONE(40009, "已初始化"),
    UNAUTHORIZED(40100, "未认证"),
    FORBIDDEN(40300, "无权限"),
    SERVER_ERROR(50000, "服务器内部错误");
    private final int code;
    private final String msg;
    ErrorCode(int code, String msg) { this.code = code; this.msg = msg; }
}