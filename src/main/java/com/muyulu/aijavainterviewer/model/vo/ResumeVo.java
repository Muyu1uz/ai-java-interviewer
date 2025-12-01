package com.muyulu.aijavainterviewer.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResumeVo {
    @TableField("professional_knowledge")
    private String professionalKnowledge;

    @TableField("project_experience")
    private String projectExperience;

    @TableField("internship_experience")
    private String internshipExperience;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
