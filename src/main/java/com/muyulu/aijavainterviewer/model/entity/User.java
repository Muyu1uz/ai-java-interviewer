package com.muyulu.aijavainterviewer.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.UUID;

@Data
@TableName("users")
public class User {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("useraccount")
    private String userAccount;

    @TableField("password")
    private String password;

    @TableField("resume_id")
    private String resumeId;
}
