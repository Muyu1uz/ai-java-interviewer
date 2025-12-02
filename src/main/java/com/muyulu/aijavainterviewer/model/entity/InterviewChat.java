package com.muyulu.aijavainterviewer.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@TableName("interview_chat")
public class InterviewChat {

    @TableId(value = "chat_id", type = IdType.ASSIGN_ID)
    private Long chatId;

    @TableField("user_id")
    private Long userId;

    @TableField("resume_id")
    private String resumeId;

    @TableField("status")
    private String status;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;
}