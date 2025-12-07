package com.muyulu.aijavainterviewer.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问题池实体
 * 包含按技术分类和难度分组的问题集合
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPool {
    
    /**
     * 问题池ID (可用于缓存键)
     */
    private String poolId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 简历ID
     */
    private String resumeId;
    
    /**
     * 简历摘要
     */
    private String resumeSummary;
    
    /**
     * 所有问题列表
     */
    private List<Question> allQuestions;
    
    /**
     * 按技术分类的问题
     * Key: 技术分类 (java, spring, redis, mysql)
     * Value: 该分类下的问题列表
     */
    private Map<String, List<Question>> byCategory;
    
    /**
     * 按难度分组的问题
     * Key: 难度级别 (BASIC, ADVANCED, EXPERT)
     * Value: 该难度的问题列表
     */
    private Map<Question.DifficultyLevel, List<Question>> byLevel;
    
    /**
     * 面试建议
     */
    private List<String> interviewSuggestions;
    
    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;
    
    /**
     * 问题总数
     */
    private Integer totalCount;
}
