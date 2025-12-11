package com.muyulu.aijavainterviewer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyulu.aijavainterviewer.model.entity.MistakeBook;

import java.util.List;

public interface MistakeBookService {

    /**
     * 插入错题
     * @param userId 用户ID
     * @param questionContent 问题内容
     */
    void insertQuestion(Long userId, String questionContent);

    /**
     * 删除错题（删除用户最新的一条错题）
     * @param userId 用户ID
     */
    void deleteQuestion(Long userId, Long mistakeBookId);

    /**
     * 列出错题(分页)
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<MistakeBook> listQuestions(Long userId, int pageNum, int pageSize);

    /**
     * 添加用户答案（给用户最新的错题添加答案）
     * @param userId 用户ID
     * @param userAnswer 用户答案
     */
    void addUserAnswer(Long userId, String userAnswer, Long mistakeBookId);
}
