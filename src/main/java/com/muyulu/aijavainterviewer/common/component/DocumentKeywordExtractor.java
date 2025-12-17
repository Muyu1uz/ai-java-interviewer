package com.muyulu.aijavainterviewer.common.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 文档关键词提取器
 * 使用 AI 模型分析文档片段，提取核心技术关键词
 */
@Slf4j
@Component
public class DocumentKeywordExtractor {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            你是一个专业的Java技术文档分析助手。你的任务是分析给定的技术文档片段，并提取出最核心的 3-5 个技术关键词。
            
            要求：
            1. 关键词应具体、准确（例如："ConcurrentHashMap" 优于 "Map"，"AQS" 优于 "同步"）
            2. 仅返回关键词，用英文逗号分隔
            3. 不要包含任何解释性文字
            4. 如果片段太短或没有明显技术内容，返回 "General"
            5. 忽略通用词汇（如：代码、实现、方法、类）
            
            示例输入：
            "ConcurrentHashMap 在 JDK 1.8 中放弃了 Segment 分段锁，改用 Node + CAS + Synchronized 实现并发控制。同时将链表过长时的结构转换为红黑树。"
            
            示例输出：
            ConcurrentHashMap, CAS, Synchronized, 红黑树, Segment
            """;

    public DocumentKeywordExtractor(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    /**
     * 提取文档片段的关键词
     * @param content 文档片段内容
     * @return 逗号分隔的关键词字符串
     */
    public String extractKeywords(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        // 截取前 1000 个字符进行分析，避免 Token 消耗过大
        String input = content.length() > 1000 ? content.substring(0, 1000) : content;

        try {
            return chatClient.prompt()
                    .user(input)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("AI 关键词提取失败: {}", e.getMessage());
            return "";
        }
    }
}
