package com.muyulu.aijavainterviewer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyulu.aijavainterviewer.common.Result;
import com.muyulu.aijavainterviewer.common.annotation.RequireLogin;
import com.muyulu.aijavainterviewer.model.dto.MistakeBookDto;
import com.muyulu.aijavainterviewer.model.entity.MistakeBook;
import com.muyulu.aijavainterviewer.service.MistakeBookService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


/**
 * 错题本控制器
 */
@RestController
@RequestMapping("/mistake-book")

public class MistakeBookController {

    @Resource
    private MistakeBookService mistakeBookService;

    /**
     * 添加错题
     */
    @PostMapping("/add")
    @RequireLogin
    public Result<Void> addQuestion(@RequestAttribute("userId") Long userId,
                                    @RequestParam String questionContent) {
        mistakeBookService.insertQuestion(userId, questionContent);
        return Result.success("添加错题成功", null);
    }

    /**
     * 删除错题
     */
    @DeleteMapping("/delete")
    @RequireLogin
    public Result<Void> deleteLatestQuestion(@Valid @RequestBody MistakeBookDto dto) {
        mistakeBookService.deleteQuestion(dto.getUserId(), dto.getMistakeBookId());
        return Result.success("删除成功", null);
    }

    /**
     * 获取用户的错题列表（分页）
     */
    @GetMapping("/list")
    @RequireLogin
    public Result<Page<MistakeBook>> listQuestions(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<MistakeBook> page = mistakeBookService.listQuestions(userId, pageNum, pageSize);
        return Result.success("获取成功", page);
    }

    /**
     * 为最新的错题添加用户答案
     */
    @PostMapping("/add-answer")
    @RequireLogin
    public Result<Void> addUserAnswer(@Valid @RequestBody MistakeBookDto dto, String userAnswer) {
        mistakeBookService.addUserAnswer(dto.getUserId(), userAnswer, dto.getMistakeBookId());
        return Result.success("添加答案成功", null);
    }
}
