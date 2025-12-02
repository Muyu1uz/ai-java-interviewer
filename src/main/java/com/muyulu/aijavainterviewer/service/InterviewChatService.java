package com.muyulu.aijavainterviewer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyulu.aijavainterviewer.model.entity.InterviewChat;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

public interface InterviewChatService extends IService<InterviewChat> {

    /**
     * 开始面试聊天
     * @return
     */
    public Flux<String> startInterviewChat(HttpServletRequest request);

    /**
     * 继续面试聊天
     * @return
     */
    public Flux<String> continueInterviewChat(HttpServletRequest request, String userInput);
}
