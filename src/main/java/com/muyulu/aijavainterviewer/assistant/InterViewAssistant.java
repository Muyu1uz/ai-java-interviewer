package com.muyulu.aijavainterviewer.assistant;

import com.muyulu.aijavainterviewer.constant.SystemConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static com.muyulu.aijavainterviewer.constant.ChatMemoryConstant.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static com.muyulu.aijavainterviewer.constant.ChatMemoryConstant.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Component
public class InterViewAssistant {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public InterViewAssistant(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
        // 初始化基于消息窗口的对话记忆，最多保留30条消息
        chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(30)
                .build();

        // 构建ChatClient，添加系统提示和日志记录顾问
        chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SystemConstant.SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * 开始面试设定前置消息
     * @param chatId
     * @return
     */
    public void startInterview(String chatId) {
        // 手动添加系统消息到记忆中
        SystemMessage systemMsg = new SystemMessage(
                "面试开始。请根据候选人的技术背景提出合适的Java相关问题。"
        );
        chatMemory.add(chatId, systemMsg);

        // 发送初始问题
        String initialQuestion = "你好！欢迎参加我们的技术面试。首先请简单介绍一下你的Java开发经验。";

        // 将AI的回复也添加到记忆中
        AssistantMessage aiResponse = new AssistantMessage(initialQuestion);
        chatMemory.add(chatId, aiResponse);
    }

    /**
     * 进行聊天交互
     * @param chatId
     * @param userInput
     * @return
     */
    public String chat(String chatId, String userInput) {
        // 手动添加用户消息到记忆
        UserMessage userMessage = new UserMessage(userInput);
        chatMemory.add(chatId, userMessage);

        log.info("开始聊天，chatId: {}, userInput: {}", chatId, userInput);
        // 发送请求并获取回复
        String aiResponse = chatClient.prompt()
                .user(userInput)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        . param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 30))
                .call()
                .content();

        // 将AI回复添加到记忆
        AssistantMessage assistantMessage = new AssistantMessage(aiResponse);
        chatMemory.add(chatId, assistantMessage);
        log.info("聊天回复，chatId: {}, aiResponse: {}", chatId, aiResponse);

        return aiResponse;
    }

    /**
     * 流式聊天交互
     * @param chatId
     * @param userInput
     * @return
     */
    public Flux<String> chatStream(String chatId, String userInput) {
        // 手动添加用户消息到记忆
        UserMessage userMessage = new UserMessage(userInput);
        chatMemory.add(chatId, userMessage);

        log.info("开始流式聊天，chatId: {}, userInput: {}", chatId, userInput);
        // 发送流式请求
        Flux<String> responseStream = chatClient.prompt()
                .user(userInput)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 30))
                . stream()  // 关键：使用 stream() 而不是 call()
                .content();

        // 收集完整回复并添加到记忆
        StringBuilder fullResponse = new StringBuilder();

        return responseStream
                .doOnNext(chunk -> {
                    // 每个流式块都会触发这里
                    fullResponse.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式完成后，将完整回复添加到记忆
                    AssistantMessage assistantMessage = new AssistantMessage(fullResponse.toString());
                    chatMemory. add(chatId, assistantMessage);
                })
                . doOnError(error -> {
                    // 错误处理
                    log.error("流式聊天出错，chatId: {}", chatId, error);
                });
    }
}
