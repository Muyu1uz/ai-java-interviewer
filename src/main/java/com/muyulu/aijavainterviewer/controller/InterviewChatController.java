package com.muyulu.aijavainterviewer.controller;

import com.muyulu.aijavainterviewer.annotation.RequireLogin;
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
    public Flux<String> startInterviewChat(HttpServletRequest request) {
        return interviewChatService.startInterviewChat(request);
    }

    @PostMapping(value = "/continue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequireLogin
    public Flux<String> continueInterviewChat(HttpServletRequest request,
                                              @RequestParam("userInput") String userInput) {
        return interviewChatService.continueInterviewChat(request, userInput);
    }
}
