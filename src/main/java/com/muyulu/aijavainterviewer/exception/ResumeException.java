package com.muyulu.aijavainterviewer.exception;

import static com.muyulu.aijavainterviewer.exception.BusinessException.ErrorCode;

/**
 * 简历相关异常
 */
public class ResumeException extends BusinessException {

    public ResumeException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public ResumeException(ErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage);
    }

    public ResumeException(int code, String message) {
        super(code, message);
    }

    /**
     * 简历不存在
     */
    public static ResumeException notFound() {
        return new ResumeException(ErrorCode.RESUME_NOT_FOUND);
    }

    /**
     * 简历上传失败
     */
    public static ResumeException uploadFailed(String reason) {
        return new ResumeException(ErrorCode.RESUME_UPLOAD_FAILED, "简历上传失败: " + reason);
    }

    /**
     * 简历解析失败
     */
    public static ResumeException parseFailed(String reason) {
        return new ResumeException(ErrorCode.RESUME_PARSE_FAILED, "简历解析失败: " + reason);
    }
}
