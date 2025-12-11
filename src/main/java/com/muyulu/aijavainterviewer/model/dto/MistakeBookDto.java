package com.muyulu.aijavainterviewer.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错题本 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MistakeBookDto {
    
    /**
     * 错题本ID
     */
    private Long mistakeBookId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 问题内容
     */
    @NotNull(message = "问题内容不能为空")
    private String questionContent;
}
