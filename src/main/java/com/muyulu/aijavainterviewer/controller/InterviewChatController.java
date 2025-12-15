package com.muyulu.aijavainterviewer.controller;

import com.muyulu.aijavainterviewer.common.annotation.RateLimit;
import com.muyulu.aijavainterviewer.common.annotation.RequireLogin;
import com.muyulu.aijavainterviewer.model.vo.QuestionPoolVO;
import com.muyulu.aijavainterviewer.service.InterviewChatService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/interview-chat")
public class InterviewChatController {

    @Resource
    private InterviewChatService interviewChatService;

    @PostMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequireLogin
    @RateLimit(
        name = "interview_start",
        capacity = 10,
        rate = 2,
        limitType = RateLimit.LimitType.USER,
        message = "面试开始过于频繁，请稍后再试"
    )
    public Flux<String> startInterviewChat(HttpServletRequest request) {
        return interviewChatService.startInterviewChat(request);
    }

    @PostMapping(value = "/continue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequireLogin
    @RateLimit(
        name = "interview_continue",
        capacity = 20,
        rate = 5,
        limitType = RateLimit.LimitType.USER,
        message = "回复过于频繁，请稍后再试"
    )
    public Flux<String> continueInterviewChat(HttpServletRequest request,
                                              @RequestParam("userInput") String userInput) {
        return interviewChatService.continueInterviewChat(request, userInput);
    }
    
    /**
     * 一键生成面试问题池
     * 
     * @param request HTTP请求
     * @param resumeFile 简历文件 (PDF/图片)
     * @param questionCount 期望生成的问题数量 (默认20)
     * @return 问题池展示VO
     */
    @PostMapping("/generate-pool")
    @RequireLogin
    @RateLimit(
        name = "generate_question_pool",
        capacity = 5,
        rate = 1,
        limitType = RateLimit.LimitType.USER,
        message = "问题池生成过于频繁，请稍后再试"
    )
    public QuestionPoolVO generateQuestionPool(
            HttpServletRequest request,
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam(value = "questionCount", defaultValue = "20") Integer questionCount) {
        
        return interviewChatService.generateQuestionPool(request, resumeFile, questionCount);
    }

    @PostMapping("/preload-pool")
    @RequireLogin
    public QuestionPoolVO preloadQuestionPool(HttpServletRequest request) {
        return interviewChatService.preloadQuestionPool(request);
    }
}
