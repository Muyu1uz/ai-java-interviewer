package com.muyulu.aijavainterviewer.assistant;

import com.muyulu.aijavainterviewer.constant.SystemConstant;
import com.muyulu.aijavainterviewer.service.RagService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.muyulu.aijavainterviewer.constant.ChatMemoryConstant.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static com.muyulu.aijavainterviewer.constant.ChatMemoryConstant.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@Component
public class InterViewAssistant {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    @Resource
    private RagService ragService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    // Redis Key 前缀: interview:asked_topics:{chatId}
    private static final String ASKED_TOPICS_KEY_PREFIX = "interview:asked_topics:";
    // 已提问技术点的过期时间: 24小时
    private static final long ASKED_TOPICS_EXPIRE_HOURS = 24;

    public InterViewAssistant(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
        // 初始化基于消息窗口的对话记忆，增加到50条消息以保留更多问题历史
        chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(50)
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
     * 进行聊天交互 (RAG增强版)
     * @param chatId
     * @param userInput
     * @return
     */
    public String chat(String chatId, String userInput) {
        // 手动添加用户消息到记忆
        UserMessage userMessage = new UserMessage(userInput);
        chatMemory.add(chatId, userMessage);

        log.info("开始聊天，chatId: {}, userInput: {}", chatId, userInput);
        
        // 发送请求并获取回复 (RAG增强在流式方法中统一处理)
        String aiResponse = chatClient.prompt()
                .user(userInput)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .call()
                .content();

        // 将AI回复添加到记忆
        AssistantMessage assistantMessage = new AssistantMessage(aiResponse);
        chatMemory.add(chatId, assistantMessage);
        log.info("聊天回复，chatId: {}, aiResponse: {}", chatId, aiResponse);

        return aiResponse;
    }

    /**
     * 流式聊天交互 (RAG增强版 - 核心方法)
     * @param chatId 对话ID
     * @param userInput 用户输入
     * @param resumeContent 简历内容 (用于RAG检索)
     * @return 流式响应
     */
    public Flux<String> chatStreamWithRag(String chatId, String userInput, String resumeContent) {
        // 手动添加用户消息到记忆
        UserMessage userMessage = new UserMessage(userInput);
        chatMemory.add(chatId, userMessage);

        log.info("开始RAG增强的流式聊天，chatId: {}", chatId);
        
        // 提取已提问的技术点
        Set<String> askedTopics = extractAskedTopics(chatId);
        String askedTopicsPrompt = buildAskedTopicsPrompt(askedTopics);
        
        // RAG增强: 检索相关知识
        String ragContext = "";
        try {
            if (ragService.isVectorStoreReady()) {
                ragContext = ragService.buildRagContext(resumeContent, 3);
                if (!ragContext.isEmpty()) {
                    log.info("✓ RAG检索成功，注入知识上下文");
                }
            }
        } catch (Exception e) {
            log.warn("RAG检索失败，使用默认流程: {}", e.getMessage());
        }
        
        // 构建增强后的输入(仅包含RAG上下文,不包含清单)
        String enhancedInput = ragContext.isEmpty() 
                ? userInput 
                : ragContext + "\n\n" + userInput;
        
        // 构建完整的系统指令(包含清单)
        String systemInstruction = askedTopicsPrompt.isEmpty() 
                ? SystemConstant.SYSTEM_PROMPT
                : SystemConstant.SYSTEM_PROMPT + "\n\n" + askedTopicsPrompt;
        
        // 发送流式请求(通过 system() 注入清单)
        Flux<String> responseStream = chatClient.prompt()
                .system(systemInstruction)
                .user(enhancedInput)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .stream()
                .content();

        // 收集完整回复并添加到记忆
        StringBuilder fullResponse = new StringBuilder();

        return responseStream
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
                })
                .doOnComplete(() -> {
                    AssistantMessage assistantMessage = new AssistantMessage(fullResponse.toString());
                    chatMemory.add(chatId, assistantMessage);
                    log.info("流式聊天完成，chatId: {}", chatId);
                })
                .doOnError(error -> {
                    log.error("流式聊天出错，chatId: {}", chatId, error);
                });
    }

    /**
     * 流式聊天交互 (保留原方法用于向后兼容)
     */
    public Flux<String> chatStream(String chatId, String userInput) {
        return chatStreamWithRag(chatId, userInput, "");
    }
    
    /**
     * 从 Redis 和对话历史中提取已提问的技术点
     * 1. 先从 Redis 读取已缓存的技术点
     * 2. 再从对话历史中提取新的技术点
     * 3. 将新提取的技术点保存到 Redis
     */
    private Set<String> extractAskedTopics(String chatId) {
        String redisKey = ASKED_TOPICS_KEY_PREFIX + chatId;
        Set<String> topics = new HashSet<>();
        
        // 1. 从 Redis 中读取已提问的技术点
        try {
            Set<String> cachedTopics = redisTemplate.opsForSet().members(redisKey);
            if (cachedTopics != null && !cachedTopics.isEmpty()) {
                topics.addAll(cachedTopics);
                log.info("从 Redis 加载已提问技术点: {}", topics);
            }
        } catch (Exception e) {
            log.warn("从 Redis 读取已提问技术点失败: {}", e.getMessage());
        }
        
        // 2. 从对话历史中提取新的技术点
        List<Message> messages = chatMemory.get(chatId);
        
        // 定义技术关键词模式(面试官的问题)
        Pattern pattern = Pattern.compile(
            "(Spring\\s*Boot|Spring\\s*Cloud|Redis|MySQL|Kafka|RabbitMQ|" +
            "Redisson|布隆过滤器|缓存|数据库|JVM|JUC|线程池|锁|" +
            "分布式|微服务|限流|熔断|事务|索引|MQ|" +
            "HashMap|ConcurrentHashMap|ArrayList|AQS|" +
            "synchronized|volatile|ThreadLocal|" +
            "B\\+树|MVCC|死锁|慢查询|Docker|Kubernetes|Nginx|" +
            "集合|泛型|反射|注解|异常|IO|NIO|序列化)",
            Pattern.CASE_INSENSITIVE
        );
        
        Set<String> newTopics = new HashSet<>();
        for (Message message : messages) {
            // 只提取 AI 的问题(AssistantMessage)
            if (message instanceof AssistantMessage) {
                String content = message.getText();
                // 检查是否包含问号(判断是否为提问)
                if (content.contains("？") || content.contains("?")) {
                    // 使用正则匹配技术关键词
                    Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {
                        String topic = matcher.group();
                        if (!topics.contains(topic)) {
                            newTopics.add(topic);
                            topics.add(topic);
                        }
                    }
                }
            }
        }
        
        // 3. 将新提取的技术点保存到 Redis
        if (!newTopics.isEmpty()) {
            try {
                redisTemplate.opsForSet().add(redisKey, newTopics.toArray(new String[0]));
                // 设置过期时间 (24小时)
                redisTemplate.expire(redisKey, Duration.ofHours(ASKED_TOPICS_EXPIRE_HOURS));
                log.info("新增技术点已保存到 Redis: {}", newTopics);
            } catch (Exception e) {
                log.warn("保存技术点到 Redis 失败: {}", e.getMessage());
            }
        }
        
        log.info("当前已提问技术点总数: {}, 内容: {}", topics.size(), topics);
        return topics;
    }
    
    /**
     * 构建已提问技术点提示
     */
    private String buildAskedTopicsPrompt(Set<String> topics) {
        if (topics.isEmpty()) {
            return "";
        }
        
        return "## 已提问技术点清单（严禁重复）\n" +
               String.join("、", topics) +
               "\n\n**提问前必须检查上述清单，选择完全不同的技术点。**";
    }
    
    /**
     * 获取指定对话的所有已提问技术点（公共方法）
     * @param chatId 对话ID
     * @return 已提问技术点集合
     */
    public Set<String> getAskedTopics(String chatId) {
        String redisKey = ASKED_TOPICS_KEY_PREFIX + chatId;
        try {
            Set<String> topics = redisTemplate.opsForSet().members(redisKey);
            return topics != null ? topics : new HashSet<>();
        } catch (Exception e) {
            log.error("获取已提问技术点失败: {}", e.getMessage());
            return new HashSet<>();
        }
    }
    
    /**
     * 清除指定对话的已提问技术点（重新开始面试时使用）
     * @param chatId 对话ID
     */
    public void clearAskedTopics(String chatId) {
        String redisKey = ASKED_TOPICS_KEY_PREFIX + chatId;
        try {
            redisTemplate.delete(redisKey);
            log.info("已清除对话 {} 的技术点记录", chatId);
        } catch (Exception e) {
            log.error("清除技术点记录失败: {}", e.getMessage());
        }
    }
    
    /**
     * 手动添加已提问的技术点（可用于初始化或补充）
     * @param chatId 对话ID
     * @param topics 技术点集合
     */
    public void addAskedTopics(String chatId, Set<String> topics) {
        if (topics == null || topics.isEmpty()) {
            return;
        }
        String redisKey = ASKED_TOPICS_KEY_PREFIX + chatId;
        try {
            redisTemplate.opsForSet().add(redisKey, topics.toArray(new String[0]));
            redisTemplate.expire(redisKey, Duration.ofHours(ASKED_TOPICS_EXPIRE_HOURS));
            log.info("已添加技术点到对话 {}: {}", chatId, topics);
        } catch (Exception e) {
            log.error("添加技术点失败: {}", e.getMessage());
        }
    }
    
    /**
     * 获取已提问技术点的数量
     * @param chatId 对话ID
     * @return 技术点数量
     */
    public long getAskedTopicsCount(String chatId) {
        String redisKey = ASKED_TOPICS_KEY_PREFIX + chatId;
        try {
            Long count = redisTemplate.opsForSet().size(redisKey);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取技术点数量失败: {}", e.getMessage());
            return 0;
        }
    }
}



