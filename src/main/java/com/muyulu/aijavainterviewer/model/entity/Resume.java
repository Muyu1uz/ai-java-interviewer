package com.muyulu.aijavainterviewer.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("resumes")
public class Resume {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("resume_id")
    private String resumeId;

    @TableField("professional_knowledge")
    private String professionalKnowledge;

    @TableField("project_experience")
    private String projectExperience;

    @TableField("internship_experience")
    private String internshipExperience;

    @TableField("create_time")
    private String createTime;

    @TableField("update_time")
    private String updateTime;
}
