package com.muyulu.aijavainterviewer.exception;

import static com.muyulu.aijavainterviewer.exception.BusinessException.ErrorCode;

/**
 * 面试相关异常
 */
public class InterviewException extends BusinessException {

    public InterviewException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public InterviewException(ErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage);
    }

    public InterviewException(int code, String message) {
        super(code, message);
    }

    /**
     * 面试记录不存在
     */
    public static InterviewException notFound() {
        return new InterviewException(ErrorCode.INTERVIEW_NOT_FOUND);
    }

    /**
     * 面试已存在
     */
    public static InterviewException alreadyExists() {
        return new InterviewException(ErrorCode.INTERVIEW_ALREADY_EXISTS);
    }

    /**
     * 面试未开始
     */
    public static InterviewException notStarted() {
        return new InterviewException(ErrorCode.INTERVIEW_NOT_STARTED);
    }
}
