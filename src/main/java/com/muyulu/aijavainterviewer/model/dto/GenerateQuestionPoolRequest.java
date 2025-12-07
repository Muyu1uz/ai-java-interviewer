package com.muyulu.aijavainterviewer.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 生成问题池请求
 */
@Data
public class GenerateQuestionPoolRequest {
    
    /**
     * 上传的简历文件
     */
    private MultipartFile resumeFile;
    
    /**
     * 期望生成的问题数量
     */
    private Integer questionCount = 20;
    
    /**
     * 难度分布配置
     * 基础题比例 (0.0-1.0)
     */
    private Double basicRatio = 0.3;
    
    /**
     * 进阶题比例 (0.0-1.0)
     */
    private Double advancedRatio = 0.5;
    
    /**
     * 高级题比例 (0.0-1.0)
     */
    private Double expertRatio = 0.2;
}
