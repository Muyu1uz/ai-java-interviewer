package com.muyulu.aijavainterviewer.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问题展示 VO
 * 用于前端展示的简化问题对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVO {
    
    /**
     * 问题内容 (主要展示)
     */
    private String content;
    
    /**
     * 技术分类标签 (如: java, spring, redis)
     */
    private String category;
    
    /**
     * 难度级别标签 (如: 基础, 进阶, 高级)
     */
    private String level;
    
    /**
     * 关键词标签 (逗号分隔，用于展示)
     */
    private String keywords;
}
