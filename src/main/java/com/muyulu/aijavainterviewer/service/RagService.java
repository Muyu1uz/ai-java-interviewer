package com.muyulu.aijavainterviewer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * RAG 文档检索服务
 * 用于从向量库中检索与问题相关的知识文档
 */
@Slf4j
@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient keywordExtractionClient;

    @Value("${rag.keyword-extraction.use-ai:true}")
    private boolean useAiExtraction;
    
    @Value("${rag.keyword-extraction.max-input-length:1000}")
    private int maxInputLength;

    // 最大上下文长度限制(约 2000 tokens)
    private static final int MAX_CONTEXT_LENGTH = 8000;
    
    // AI 技术分析的系统提示词
    private static final String KEYWORD_EXTRACTION_PROMPT = """
            你是一个技术分析专家。请分析用户提供的简历或技术描述，提取关键的技术信息用于知识检索。
            
            要求：
            1. 提取技术栈、框架、工具（如：Java、Spring Boot、Redis、MySQL）
            2. 提取技术场景和解决方案（如：缓存穿透、布隆过滤器、分布式锁）
            3. 提取技术思路和模式（如：高并发、微服务架构、消息队列异步处理）
            4. 提取完整的问题-解决方案对（如：使用Redis分布式锁解决超卖问题）
            5. 保持自然语言描述，不要只列出单个技术名词
            6. 最多返回 150 字的技术摘要
            
            示例输入：
            "具有3年Java开发经验，熟练使用Spring Boot、MyBatis、Redis进行后端开发。
            在电商项目中使用布隆过滤器解决缓存穿透问题，使用Redis分布式锁处理秒杀超卖。
            负责设计高并发系统，使用Kafka消息队列实现异步解耦。"
            
            示例输出：
            "Java Spring Boot Redis 缓存穿透 布隆过滤器 分布式锁 秒杀超卖 高并发 Kafka消息队列 异步解耦 MyBatis"
            
            注意：
            - 包含完整的技术场景（如"缓存穿透 布隆过滤器"而非只有"布隆过滤器"）
            - 包含问题和解决方案（如"秒杀超卖 分布式锁"）
            - 包含架构模式（如"消息队列 异步解耦"）
            - 不要提取公司名、人名、项目名称、时间日期等非技术信息
            """;

    
    public RagService(VectorStore vectorStore, 
                     @Qualifier("dashScopeChatModel") ChatModel chatModel) {
        this.vectorStore = vectorStore;
        this.keywordExtractionClient = ChatClient.builder(chatModel)
                .defaultSystem(KEYWORD_EXTRACTION_PROMPT)
                .build();
    }
    
    /**
     * 根据简历内容和查询文本检索相关文档
     * 
     * @param resumeContent 简历内容(用于提取技术栈关键词)
     * @param topK 返回最相关的 K 个结果
     * @return 相关文档内容列表
     */
    public List<String> searchRelevantKnowledge(String resumeContent, int topK) {
        long startTime = System.currentTimeMillis();
        log.debug("基于简历检索相关知识, topK: {}", topK);
        
        try {
            // 1. 从简历中提取关键技术点作为查询
            String query = extractTechKeywords(resumeContent);
            log.debug("提取的查询关键词: {}", query);
            
            // 2. 构建搜索请求
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(0.65)  // 提高相似度阈值，过滤低质量结果
                    .build();
            
            // 3. 执行向量检索
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            if (results.isEmpty()) {
                log.warn("未找到相关知识文档, query: {}, threshold: 0.65", query);
                return List.of();
            }
            
            // 4. 提取文档内容并控制总长度
            int totalLength = 0;
            List<String> contents = new java.util.ArrayList<>();
            
            for (Document doc : results) {
                String content = doc.getFormattedContent();
                String source = (String) doc.getMetadata().get("source");
                String formatted = String.format("[来源: %s]\n%s", source, content);
                
                // 控制总上下文长度，避免超出模型限制
                if (totalLength + formatted.length() > MAX_CONTEXT_LENGTH) {
                    log.warn("RAG 上下文已达长度限制 {} 字符，停止添加更多文档", MAX_CONTEXT_LENGTH);
                    break;
                }
                
                contents.add(formatted);
                totalLength += formatted.length();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("✓ 检索到 {} 个相关知识片段，总长度 {} 字符，耗时 {} ms", 
                    contents.size(), totalLength, duration);
            return contents;
            
        } catch (Exception e) {
            log.error("知识检索失败", e);
            return List.of();
        }
    }

    /**
     * 根据具体问题检索相关文档
     */
    public List<String> searchByQuestion(String question, int topK) {
        log.debug("根据问题检索知识: {}", question);
        
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(question)
                    .topK(topK)
                    .similarityThreshold(0.65)
                    .build();
            
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            return results.stream()
                    .map(Document::getFormattedContent)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("问题检索失败", e);
            return List.of();
        }
    }

    /**
     * 构建 RAG 增强的提示词上下文
     * 
     * @param resumeContent 简历内容
     * @param topK 检索文档数量（建议 3-5）
     * @return 格式化的知识上下文
     */
    public String buildRagContext(String resumeContent, int topK) {
        // 限制 topK 最大值，避免上下文过长
        int limitedTopK = Math.min(topK, 5);
        List<String> knowledgeDocs = searchRelevantKnowledge(resumeContent, limitedTopK);
        
        if (knowledgeDocs.isEmpty()) {
            log.debug("RAG 检索未返回结果，使用默认上下文");
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("### 📚 面试知识库参考 ###\n\n");
        context.append("以下是与候选人简历相关的技术知识（已优选最相关的 ");
        context.append(knowledgeDocs.size()).append(" 个片段）：\n\n");
        
        for (int i = 0; i < knowledgeDocs.size(); i++) {
            context.append("【知识点 ").append(i + 1).append("】\n");
            context.append(knowledgeDocs.get(i)).append("\n\n");
        }
        
        context.append("---\n");
        context.append("请基于以上知识库内容和候选人简历，提出深入、专业的面试问题。\n\n");
        
        String result = context.toString();
        log.debug("构建的 RAG 上下文长度: {} 字符", result.length());
        return result;
    }

    /**
     * 从简历内容提取技术关键词 (公共方法，供外部调用)
     * 优化版本：优先使用 AI 提取，失败时降级到正则表达式
     */
    public String extractKeywords(String resumeContent) {
        return extractTechKeywords(resumeContent);
    }
    
    /**
     * 从简历内容提取技术关键词
     * 优化版本：优先使用 AI 提取，失败时降级到正则表达式
     */
    private String extractTechKeywords(String resumeContent) {
        if (resumeContent == null || resumeContent.isEmpty()) {
            return "Java 后端开发";
        }
        
        // 策略1: 使用 AI 提取关键词（智能、准确）- 可配置
        if (useAiExtraction) {
            try {
                String aiKeywords = extractKeywordsWithAI(resumeContent);
                if (aiKeywords != null && !aiKeywords.trim().isEmpty()) {
                    log.debug("✓ AI 提取技术关键词: {}", aiKeywords);
                    return aiKeywords.trim();
                }
            } catch (Exception e) {
                log.warn("AI 关键词提取失败，降级到正则表达式: {}", e.getMessage());
            }
        }
        
        // 策略2: 降级到正则表达式（快速、离线）
        String regexKeywords = extractKeywordsWithRegex(resumeContent);
        log.debug("✓ 正则提取技术关键词: {}", regexKeywords);
        return regexKeywords;
    }
    
    /**
     * 使用 AI 模型提取技术关键词
     * 优点：智能理解上下文，能识别新技术、框架别名等
     */
    private String extractKeywordsWithAI(String resumeContent) {
        // 限制输入长度，避免 token 过多
        String limitedContent = resumeContent.length() > maxInputLength 
                ? resumeContent.substring(0, maxInputLength) 
                : resumeContent;
        
        long startTime = System.currentTimeMillis();
        
        String keywords = keywordExtractionClient.prompt()
                .user(limitedContent)
                .call()
                .content();
        
        long duration = System.currentTimeMillis() - startTime;
        log.debug("AI 关键词提取耗时: {} ms, 输入长度: {} 字符", duration, limitedContent.length());
        
        return keywords;
    }
    
    /**
     * 使用正则表达式提取技术关键词（降级方案）
     * 优点：快速、离线、无额外成本
     * 注：尽量提取技术场景和解决方案相关的词汇
     */
    private String extractKeywordsWithRegex(String resumeContent) {
        // 技术栈模式
        Pattern techPattern = Pattern.compile(
            "(?i)(Java|Spring|SpringBoot|SpringCloud|MyBatis|MyBatisPlus|Redis|MySQL|PostgreSQL|" +
            "Kafka|RabbitMQ|RocketMQ|Docker|Kubernetes|K8s|Microservice|微服务|" +
            "Nginx|Linux|Git|Maven|Gradle|Tomcat|Jetty|Netty|Dubbo|Zookeeper|" +
            "Elasticsearch|ES|MongoDB|Oracle|SQL|NoSQL|" +
            "Vue|React|Angular|Node\\.js|Python|Go|Rust|C\\+\\+|" +
            "Hadoop|Spark|Flink|HBase|Hive|Presto|ClickHouse|TiDB|" +
            "Jenkins|GitLab|CI/CD|DevOps|Prometheus|Grafana|ELK|Kibana|" +
            "Sentinel|Hystrix|Feign|Gateway|Nacos|Apollo|Seata|XXL-Job)",
            Pattern.CASE_INSENSITIVE
        );
        
        // 技术场景和解决方案模式（新增）
        Pattern scenarioPattern = Pattern.compile(
            "(?i)(缓存穿透|缓存击穿|缓存雪崩|布隆过滤器|" +
            "分布式锁|超卖问题|秒杀|限流|熔断|降级|" +
            "高并发|高可用|负载均衡|读写分离|分库分表|" +
            "消息队列|异步处理|削峰填谷|最终一致性|" +
            "分布式事务|两阶段提交|三阶段提交|TCC|Saga|" +
            "服务注册|服务发现|配置中心|链路追踪|" +
            "设计模式|单例|工厂|策略|观察者|责任链|" +
            "数据结构|算法|排序|查找|树|图|哈希|" +
            "多线程|并发|线程池|锁|同步|异步|死锁|" +
            "JVM|GC|垃圾回收|内存泄漏|性能优化|调优)",
            Pattern.CASE_INSENSITIVE
        );
        
        java.util.Set<String> seen = new java.util.HashSet<>();
        StringBuilder keywords = new StringBuilder();
        int count = 0;
        
        // 1. 先提取技术场景（优先级更高）
        Matcher scenarioMatcher = scenarioPattern.matcher(resumeContent);
        while (scenarioMatcher.find() && count < 15) {
            String keyword = scenarioMatcher.group(1);
            String normalizedKeyword = keyword.toLowerCase();
            
            if (!seen.contains(normalizedKeyword)) {
                if (keywords.length() > 0) {
                    keywords.append(" ");
                }
                keywords.append(keyword);
                seen.add(normalizedKeyword);
                count++;
            }
        }
        
        // 2. 再提取技术栈
        Matcher techMatcher = techPattern.matcher(resumeContent);
        while (techMatcher.find() && count < 20) {
            String keyword = techMatcher.group(1);
            String normalizedKeyword = keyword.toLowerCase();
            
            if (!seen.contains(normalizedKeyword)) {
                if (keywords.length() > 0) {
                    keywords.append(" ");
                }
                keywords.append(keyword);
                seen.add(normalizedKeyword);
                count++;
            }
        }
        
        // 如果没有匹配到关键词，取前 300 字符
        if (keywords.length() == 0) {
            return resumeContent.length() > 300 
                ? resumeContent.substring(0, 300) 
                : resumeContent;
        }
        
        return keywords.toString();
    }

    /**
     * 检查向量库是否已初始化
     */
    public boolean isVectorStoreReady() {
        try {
            List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query("test")
                    .topK(1)
                    .build()
            );
            return !results.isEmpty();
        } catch (Exception e) {
            log.warn("向量库未就绪: {}", e.getMessage());
            return false;
        }
    }
}
