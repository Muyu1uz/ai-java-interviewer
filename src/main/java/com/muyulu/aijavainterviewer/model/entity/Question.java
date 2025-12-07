package com.muyulu.aijavainterviewer.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 面试问题实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    
    /**
     * 问题内容
     */
    private String content;
    
    /**
     * 技术分类 (如: java, spring, redis, mysql)
     */
    private String category;
    
    /**
     * 难度级别
     */
    private DifficultyLevel level;
    
    /**
     * 关联的技术关键词
     */
    private String keywords;
    
    /**
     * 问题来源 (RESUME: 简历项目, KNOWLEDGE_BASE: 知识库)
     */
    private String source;
    
    /**
     * 相关性评分 (0.0-1.0)
     */
    private Double relevanceScore;
    
    /**
     * 参考答案 (可选)
     */
    private String suggestedAnswer;
    
    /**
     * 难度级别枚举
     */
    public enum DifficultyLevel {
        BASIC("基础"),
        ADVANCED("进阶"),
        EXPERT("高级");
        
        private final String description;
        
        DifficultyLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
