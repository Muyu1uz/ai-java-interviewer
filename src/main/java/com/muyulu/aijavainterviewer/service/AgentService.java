package com.muyulu.aijavainterviewer.service;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

@Service
public class AgentService {
    @Resource
    private ReactAgent reactAgent;

    public AssistantMessage streamOutput(String query, String threadId) throws GraphRunnerException {
        String resolvedThreadId = (threadId == null || threadId.isBlank())
                ? java.util.UUID.randomUUID().toString()
                : threadId;
        RunnableConfig config = RunnableConfig.builder()
                .threadId(resolvedThreadId)
                .build();
        return reactAgent.call(query, config);
    }
}
