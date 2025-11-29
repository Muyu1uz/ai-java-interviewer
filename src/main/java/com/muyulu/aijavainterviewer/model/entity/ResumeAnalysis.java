package com.muyulu.aijavainterviewer.model.entity;

import com.muyulu.aijavainterviewer.model.enums.AnalysisStatus;
import com.muyulu.aijavainterviewer.model.enums.FileType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resume_analysis")
@Data
/**
 * 简历解析/分析记录表
 * 存储用户上传的简历文件信息及其解析状态
 */
public class ResumeAnalysis {
    /** 主键，自增 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 简历唯一标识（基于文件hash或用户+时间）
    @Column(unique = true, nullable = false)
    private String resumeId;
    
    /** 用户在系统中的 ID（可为空） */
    private String userId;
    /** 上传的文件名（原始文件名） */
    private String fileName;
    
    /** 文件类型，使用枚举 `FileType`（例如：PDF、DOCX、TXT） */
    @Enumerated(EnumType.STRING)
    private FileType fileType;
    
    /** 文件大小（字节） */
    private Long fileSize;
    /** 文件上传时间 */
    private LocalDateTime uploadTime;
    /** 简历解析/分析时间（开始或完成时间，按项目约定） */
    private LocalDateTime analysisTime;
    
    /**
     * 解析状态，使用枚举 `AnalysisStatus`（例如：PENDING, PROCESSING, DONE, FAILED）
     */
    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;
    
    /**
     * 解析后生成的结构化信息（一对一关联）
     */
    @OneToOne(mappedBy = "resumeAnalysis", cascade = CascadeType.ALL)
    private ResumeStructuredInfo structuredInfo;
    
    /**
     * 根据简历生成的面试问题列表（与 `InterviewQuestion.resumeAnalysis` 反向关联）
     */
    @OneToMany(mappedBy = "resumeAnalysis", cascade = CascadeType.ALL)
    private List<InterviewQuestion> questions = new ArrayList<>();

}