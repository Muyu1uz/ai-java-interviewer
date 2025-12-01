package com.muyulu.aijavainterviewer.factories;

import com.muyulu.aijavainterviewer.assistant.InterViewAssistant;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j. Slf4j;
import org. springframework.ai.chat.model.ChatModel;
import org. springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent. Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util. concurrent.TimeUnit;

@Slf4j
@Service
public class InterViewAssistantFactory {

    private final ChatModel chatModel;
    private final ConcurrentHashMap<String, InterViewAssistant> assistantCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> userLastActiveTime = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);

    private static final long USER_INACTIVE_TIMEOUT = 30 * 60 * 1000; // 30分钟

    public InterViewAssistantFactory(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        this.chatModel = chatModel;
        startCleanupTask();
    }

    /**
     * 为用户获取或创建 Assistant 实例
     */
    public InterViewAssistant getAssistantForUser(String userId) {
        userLastActiveTime.put(userId, System.currentTimeMillis());
        
        return assistantCache.computeIfAbsent(userId, k -> {
            log.info("为用户 {} 创建新的 InterViewAssistant 实例", userId);
            return new InterViewAssistant(chatModel);
        });
    }

    /**
     * 开始面试
     */
    public void startInterview(String userId, String chatId) {
        InterViewAssistant assistant = getAssistantForUser(userId);
        assistant.startInterview(chatId);
    }

    /**
     * 进行对话
     */
    public String chat(String userId, String chatId, String userInput) {
        InterViewAssistant assistant = getAssistantForUser(userId);
        return assistant.chat(chatId, userInput);
    }

    /**
     * 移除用户的 Assistant
     */
    public void removeAssistantForUser(String userId) {
        InterViewAssistant assistant = assistantCache.remove(userId);
        if (assistant != null) {
            userLastActiveTime.remove(userId);
            log.info("移除用户 {} 的 InterViewAssistant 实例", userId);
        }
    }

    /**
     * 启动定时清理任务
     */
    private void startCleanupTask() {
        cleanupScheduler.scheduleAtFixedRate(this::cleanupInactiveUsers, 
                5, 5, TimeUnit.MINUTES);
    }

    /**
     * 清理不活跃用户
     */
    private void cleanupInactiveUsers() {
        long currentTime = System.currentTimeMillis();
        
        userLastActiveTime.entrySet(). removeIf(entry -> {
            String userId = entry.getKey();
            long lastActive = entry.getValue();
            
            if (currentTime - lastActive > USER_INACTIVE_TIMEOUT) {
                removeAssistantForUser(userId);
                return true;
            }
            return false;
        });
        
        log.debug("清理任务完成，当前活跃用户数: {}", assistantCache.size());
    }

}