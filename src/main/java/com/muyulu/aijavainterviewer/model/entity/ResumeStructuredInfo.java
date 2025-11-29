package com.muyulu.aijavainterviewer.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Data
@Table(name = "resume_structured_info")
/**
 * 简历结构化信息表
 * 存储从简历中提取的结构化数据，如基本信息、教育背景、技能列表、工作经历等
 */
public class ResumeStructuredInfo {
    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;
    
    /** 对应的简历解析记录（双向一对一） */
    @OneToOne
    @JoinColumn(name = "resume_id")
    private ResumeAnalysis resumeAnalysis;
    
    /**
     * 基本信息（如姓名、联系方式、期望岗位、所在地等）
     * 存为 JSON 字段，Map 的 key 为字段名，value 为对应值
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> basicInfo;
    
    /**
     * 教育信息（如学校、学历、专业、起止时间等）
     * 存为 JSON 字段，Map 的结构可包含多个标准教育字段
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> educationInfo;
    
    /**
     * 技能列表，每项为一个 Map（例如：{"name":"Java","level":"熟练"}）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> skills;
    
    /**
     * 工作经历列表，每项为一个 Map（例如：{"company":"X","title":"工程师","start":"yyyy-mm","end":"yyyy-mm","description":"..."}）
     */
    @JdbcTypeCode(SqlTypes. JSON)
    private List<Map<String, Object>> workExperience;
    
    /**
     * 项目经历列表，每项为一个 Map（如项目名、职责、关键技术、成就等）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> projectExperience;
    
    /**
     * 原始解析文本（可能包含模型返回的自然语言分析、建议或备注），使用 TEXT 存储
     */
    @Column(columnDefinition = "TEXT")
    private String rawAnalysisText;
    
    // getters and setters... 
}