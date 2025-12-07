package com.muyulu.aijavainterviewer.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问题池展示 VO
 * 用于前端展示的问题池对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPoolVO {
    
    /**
     * 问题池ID
     */
    private String poolId;
    
    /**
     * 问题总数
     */
    private Integer totalCount;
    
    /**
     * 所有问题列表 (扁平化，按顺序展示)
     */
    private List<QuestionVO> questions;
    
    /**
     * 按技术分类的问题数量统计
     * 用于前端展示分类标签云
     */
    private Map<String, Integer> categoryStats;
    
    /**
     * 按难度的问题数量统计
     * 用于前端展示难度分布
     */
    private Map<String, Integer> levelStats;
    
    /**
     * 面试建议
     */
    private List<String> suggestions;
    
    /**
     * 生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime generatedAt;
}
