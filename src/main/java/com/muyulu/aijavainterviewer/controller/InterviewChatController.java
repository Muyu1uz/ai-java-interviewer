package com.muyulu.aijavainterviewer.controller;

import com.muyulu.aijavainterviewer.common.annotation.RateLimit;
import com.muyulu.aijavainterviewer.common.annotation.RequireLogin;
import com.muyulu.aijavainterviewer.service.InterviewChatService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
}
