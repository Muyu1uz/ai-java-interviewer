package com.muyulu.aijavainterviewer.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.muyulu.aijavainterviewer.constant.SystemConstant;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    @Resource
    private ChatModel dashScopeChatModel;

    @Bean
    public ReactAgent reactAgent() {
        return ReactAgent.builder()
                .name("java_interview_agent")
                .model(dashScopeChatModel)
                .instruction(SystemConstant.SYSTEM_PROMPT)
                .saver(new MemorySaver())
                .build();
    }
}
