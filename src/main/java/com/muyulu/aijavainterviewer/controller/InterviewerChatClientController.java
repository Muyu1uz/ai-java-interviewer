package com.muyulu.aijavainterviewer.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.muyulu.aijavainterviewer.constant.SystemConstant;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

public class InterviewerChatClientController {

    private final ChatClient dashscopeChatClient;

    public InterviewerChatClientController(ChatModel dashscopeChatModel){
        this.dashscopeChatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
    }

    /**
     * 流式调用
     * @param response
     * @return
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        return dashscopeChatClient.prompt(SystemConstant.SYSTEM_PROMPT).stream().content();
    }


}
