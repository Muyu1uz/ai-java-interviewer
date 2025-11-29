package com.muyulu.aijavainterviewer.model.entity;

import com.muyulu.aijavainterviewer.model.enums.DifficultyLevel;
import com.muyulu.aijavainterviewer.model.enums.QuestionDimension;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "interview_questions")
@Data
/**
 * 面试问题池表
 */
public class InterviewQuestion {
    /** 主键，自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的简历分析记录
     * - 多个面试问题属于同一份简历分析
     */
    @ManyToOne
    @JoinColumn(name = "resume_id")
    private ResumeAnalysis resumeAnalysis;
    
    /**
     * 题目维度/类别（例如：算法、设计、行为、系统设计等），使用枚举 `QuestionDimension`
     */
    @Enumerated(EnumType.STRING)
    private QuestionDimension dimension;
    
    /**
     * 问题内容，使用 TEXT 存储，可能包含较长的描述或示例
     */
    @Column(columnDefinition = "TEXT")
    private String question;
    
    /**
     * 难度等级，使用枚举 `DifficultyLevel`（例如：EASY/MEDIUM/HARD）
     */
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;
    
    /**
     * 与该问题相关的关键词或触发词（逗号分隔或其它约定），用于匹配简历中的技能/经历
     */
    private String relatedKeywords;

    /**
     * 标记该问题是否已经被提问（默认 false）
     */
    private Boolean isAsked = false;
}