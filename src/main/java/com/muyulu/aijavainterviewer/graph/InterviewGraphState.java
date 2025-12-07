package com.muyulu.aijavainterviewer.graph;

import com.muyulu.aijavainterviewer.model.entity.Question;
import com.muyulu.aijavainterviewer.model.entity.QuestionPool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 面试问题池生成 Graph 的状态管理类
 * 在 Graph 各个节点之间传递和更新状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewGraphState {
    
    /**
     * 输入：上传的简历文件
     */
    private MultipartFile resumeFile;
    
    /**
     * 输入：期望生成的问题数量
     */
    private Integer questionCount = 20;
    
    /**
     * 难度分布
     */
    private Double basicRatio = 0.3;
    private Double advancedRatio = 0.5;
    private Double expertRatio = 0.2;
    
    /**
     * Node 1 输出：简历文本内容
     */
    private String resumeContent;
    
    /**
     * Node 2 输出：提取的技术关键词
     */
    private String techKeywords;
    
    /**
     * Node 3 输出：RAG 检索的知识上下文
     */
    private List<String> ragContext = new ArrayList<>();
    
    /**
     * Node 4 输出：生成的问题列表
     */
    private List<Question> questions = new ArrayList<>();
    
    /**
     * Node 5 输出：质量控制后的问题
     */
    private List<Question> filteredQuestions = new ArrayList<>();
    
    /**
     * Node 6 输出：最终的问题池
     */
    private QuestionPool finalPool;
    
    /**
     * 用户ID (用于关联)
     */
    private Long userId;
    
    /**
     * 简历ID (用于关联)
     */
    private String resumeId;
}
