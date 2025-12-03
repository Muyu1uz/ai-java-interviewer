package com.muyulu.aijavainterviewer.exception;

import static com.muyulu.aijavainterviewer.exception.BusinessException.ErrorCode;

/**
 * 用户相关异常
 */
public class UserException extends BusinessException {

    public UserException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public UserException(ErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage);
    }

    public UserException(int code, String message) {
        super(code, message);
    }

    /**
     * 用户不存在
     */
    public static UserException notFound() {
        return new UserException(ErrorCode.USER_NOT_FOUND);
    }

    /**
     * 用户已存在
     */
    public static UserException alreadyExists() {
        return new UserException(ErrorCode.USER_ALREADY_EXISTS);
    }

    /**
     * 用户未登录
     */
    public static UserException notLogin() {
        return new UserException(ErrorCode.USER_NOT_LOGIN);
    }

    /**
     * 密码错误
     */
    public static UserException passwordError() {
        return new UserException(ErrorCode.PASSWORD_ERROR);
    }

    /**
     * Token无效
     */
    public static UserException tokenInvalid() {
        return new UserException(ErrorCode.TOKEN_INVALID);
    }
}
