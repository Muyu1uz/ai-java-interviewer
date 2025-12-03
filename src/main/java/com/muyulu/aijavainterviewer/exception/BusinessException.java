package com.muyulu.aijavainterviewer.exception;

import lombok.Getter;

/**
 * 业务异常基类
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 错误码枚举
     */
    public enum ErrorCode {
        // 通用错误 1xxxx
        SUCCESS(200, "操作成功"),
        SYSTEM_ERROR(10000, "系统错误"),
        PARAM_ERROR(10001, "参数错误"),
        
        // 用户相关 2xxxx
        USER_NOT_FOUND(20001, "用户不存在"),
        USER_ALREADY_EXISTS(20002, "用户已存在"),
        USER_NOT_LOGIN(20003, "用户未登录"),
        PASSWORD_ERROR(20004, "密码错误"),
        TOKEN_INVALID(20005, "Token无效或已过期"),
        
        // 简历相关 3xxxx
        RESUME_NOT_FOUND(30001, "简历不存在"),
        RESUME_UPLOAD_FAILED(30002, "简历上传失败"),
        RESUME_PARSE_FAILED(30003, "简历解析失败"),
        
        // 面试相关 4xxxx
        INTERVIEW_NOT_FOUND(40001, "面试记录不存在"),
        INTERVIEW_ALREADY_EXISTS(40002, "面试已存在"),
        INTERVIEW_NOT_STARTED(40003, "面试未开始");

        private final int code;
        private final String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
