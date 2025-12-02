package com.muyulu.aijavainterviewer.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录响应 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVo {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户账号
     */
    private String userAccount;
    
    /**
     * JWT Token
     */
    private String token;
}
