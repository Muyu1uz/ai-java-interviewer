package com.muyulu.aijavainterviewer.service;

import cn.hutool.core.util.IdUtil;
import com.muyulu.aijavainterviewer.graph.InterviewGraphState;
import com.muyulu.aijavainterviewer.model.entity.Question;
import com.muyulu.aijavainterviewer.model.entity.QuestionPool;
import com.muyulu.aijavainterviewer.tool.FileToStringConverterTool;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试问题池生成服务
 * 使用 Spring AI Graph 编排多个节点生成问题池
 */
@Slf4j
@Service
public class InterviewGraphService {
    
    private final FileToStringConverterTool fileConverter;
    private final RagService ragService;
    private final ChatClient chatClient;
    
    public InterviewGraphService(FileToStringConverterTool fileConverter, 
                                RagService ragService,
                                @Qualifier("dashScopeChatModel") ChatModel chatModel) {
        this.fileConverter = fileConverter;
        this.ragService = ragService;
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    
    /**
     * 生成问题池 (手动编排 Graph 流程)
     */
    public QuestionPool generateQuestionPool(InterviewGraphState state) {
        log.info("========== 开始生成问题池 ==========");
        log.info("期望生成问题数: {}, 难度分布: 基础{}% / 进阶{}% / 高级{}%", 
                state.getQuestionCount(),
                (int)(state.getBasicRatio() * 100),
                (int)(state.getAdvancedRatio() * 100),
                (int)(state.getExpertRatio() * 100));
        try {
            // Node 1: 解析简历
            state = parseResume(state);
            
            // Node 2: 提取关键词
            state = extractKeywords(state);
            
            // Node 3: RAG 检索知识
            state = retrieveKnowledge(state);
            
            // Node 4: 生成问题
            state = generateQuestions(state);
            
            // Node 5: 质量控制
            state = qualityControl(state);
            
            // Node 6: 组织问题池
            state = organizePool(state);
            
            log.info("========== 问题池生成完成 ==========");
            log.info("最终问题数: {}", state.getFinalPool().getTotalCount());
            
            return state.getFinalPool();
            
        } catch (Exception e) {
            log.error("问题池生成失败", e);
            throw new RuntimeException("生成问题池失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * Node 1: 解析简历
     * 复用 FileToStringConverterTool
     */
    private InterviewGraphState parseResume(InterviewGraphState state) {
        log.info("Node 1: 解析简历文件");
        
        String resumeText = fileConverter.convertMultipartFileToString(
                state.getResumeFile(), 
                "TEXT"
        );
        
        state.setResumeContent(resumeText);
        log.info("✓ 简历解析完成, 长度: {} 字符", resumeText.length());
        
        return state;
    }
    
    /**
     * Node 2: 提取技术关键词
     * 复用 RagService.extractKeywords
     */
    private InterviewGraphState extractKeywords(InterviewGraphState state) {
        log.info("Node 2: 提取技术关键词");
        
        String keywords = ragService.extractKeywords(state.getResumeContent());
        state.setTechKeywords(keywords);
        
        log.info("✓ 关键词提取完成: {}", keywords);
        return state;
    }
    
    /**
     * Node 3: RAG 检索相关知识
     * 复用 RagService.searchRelevantKnowledge
     */
    private InterviewGraphState retrieveKnowledge(InterviewGraphState state) {
        log.info("Node 3: RAG 检索相关知识");
        
        List<String> ragContext = ragService.searchRelevantKnowledge(
                state.getResumeContent(), 
                5  // topK
        );
        
        state.setRagContext(ragContext);
        log.info("✓ 知识检索完成, 检索到 {} 个知识片段", ragContext.size());
        
        return state;
    }
    
    /**
     * Node 4: 分层生成问题
     * 调用 AI 生成基础/进阶/高级问题
     */
    private InterviewGraphState generateQuestions(InterviewGraphState state) {
        log.info("Node 4: 生成面试问题");
        
        List<Question> allQuestions = new ArrayList<>();
        
        int totalCount = state.getQuestionCount();
        int basicCount = (int) (totalCount * state.getBasicRatio());
        int advancedCount = (int) (totalCount * state.getAdvancedRatio());
        int expertCount = totalCount - basicCount - advancedCount;
        
        // 生成基础题
        log.info("生成基础题 {} 道...", basicCount);
        List<Question> basicQuestions = generateQuestionsByLevel(
                state, Question.DifficultyLevel.BASIC, basicCount
        );
        allQuestions.addAll(basicQuestions);
        
        // 生成进阶题
        log.info("生成进阶题 {} 道...", advancedCount);
        List<Question> advancedQuestions = generateQuestionsByLevel(
                state, Question.DifficultyLevel.ADVANCED, advancedCount
        );
        allQuestions.addAll(advancedQuestions);
        
        // 生成高级题
        log.info("生成高级题 {} 道...", expertCount);
        List<Question> expertQuestions = generateQuestionsByLevel(
                state, Question.DifficultyLevel.EXPERT, expertCount
        );
        allQuestions.addAll(expertQuestions);
        
        state.setQuestions(allQuestions);
        log.info("✓ 问题生成完成, 共 {} 道题", allQuestions.size());
        
        return state;
    }
    
    /**
     * 生成指定难度的问题
     */
    private List<Question> generateQuestionsByLevel(InterviewGraphState state, 
                                                    Question.DifficultyLevel level, 
                                                    int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        
        String prompt = buildQuestionPrompt(
                state.getResumeContent(),
                state.getTechKeywords(),
                state.getRagContext(),
                level,
                count
        );
        
        try {
            // 调用 AI 生成问题
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            // 解析 AI 返回的问题列表
            return parseQuestions(response, level);
            
        } catch (Exception e) {
            log.error("生成 {} 难度问题失败", level, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 构建问题生成 Prompt
     */
    private String buildQuestionPrompt(String resumeContent, 
                                      String keywords, 
                                      List<String> ragContext,
                                      Question.DifficultyLevel level,
                                      int count) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一位资深技术面试官。请根据候选人的简历和相关技术知识，生成 ")
              .append(count).append(" 道 ").append(level.getDescription())
              .append(" 难度的面试题。\n\n");
        
        prompt.append("【候选人简历摘要】\n")
              .append(resumeContent.substring(0, Math.min(500, resumeContent.length())))
              .append("\n\n");
        
        prompt.append("【技术关键词】\n")
              .append(keywords).append("\n\n");
        
        if (!ragContext.isEmpty()) {
            prompt.append("【参考知识库】\n");
            ragContext.stream()
                    .limit(2)  // 只取前2个知识片段
                    .forEach(doc -> prompt.append(doc).append("\n\n"));
        }
        
        prompt.append("【难度要求】\n");
        switch (level) {
            case BASIC:
                prompt.append("- 基础题：概念理解、API使用、基本原理\n");
                prompt.append("- 例如：什么是Spring AOP? HashMap和HashTable的区别?\n");
                break;
            case ADVANCED:
                prompt.append("- 进阶题：原理分析、场景应用、性能优化\n");
                prompt.append("- 例如：Redis如何解决缓存穿透? Spring Boot启动流程是怎样的?\n");
                break;
            case EXPERT:
                prompt.append("- 高级题：架构设计、分布式场景、高并发解决方案\n");
                prompt.append("- 例如：如何设计一个秒杀系统? 分布式事务的几种解决方案?\n");
                break;
        }
        
        prompt.append("\n【输出格式】\n");
        prompt.append("请严格按照以下JSON数组格式输出，每个问题包含:\n");
        prompt.append("```json\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"content\": \"问题内容\",\n");
        prompt.append("    \"category\": \"技术分类(java/spring/redis/mysql等)\",\n");
        prompt.append("    \"keywords\": \"关联技术点(逗号分隔)\"\n");
        prompt.append("  }\n");
        prompt.append("]\n");
        prompt.append("```\n\n");
        
        prompt.append("要求：\n");
        prompt.append("1. 问题必须紧扣候选人简历中的技术栈和项目经验\n");
        prompt.append("2. 结合知识库内容，提出有深度的问题\n");
        prompt.append("3. 问题具体明确，避免泛泛而谈\n");
        prompt.append("4. 只返回JSON数组，不要其他文字\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析 AI 返回的问题列表
     */
    private List<Question> parseQuestions(String response, Question.DifficultyLevel level) {
        List<Question> questions = new ArrayList<>();
        
        try {
            // 提取 JSON 部分
            String json = response;
            if (response.contains("```json")) {
                json = response.substring(response.indexOf("["), response.lastIndexOf("]") + 1);
            } else if (response.contains("```")) {
                json = response.substring(response.indexOf("["), response.lastIndexOf("]") + 1);
            }
            
            // 简单的 JSON 解析 (这里可以用 Jackson 或 Gson)
            json = json.trim();
            if (!json.startsWith("[")) {
                log.warn("AI 返回格式不正确，尝试修复");
                return Collections.emptyList();
            }
            
            // 手动解析 (简化版，生产环境建议用 JSON 库)
            String[] items = json.split("\\},\\s*\\{");
            for (String item : items) {
                try {
                    Question q = new Question();
                    q.setLevel(level);
                    q.setSource("AI_GENERATED");
                    
                    // 提取 content
                    String content = extractJsonValue(item, "content");
                    q.setContent(content);
                    
                    // 提取 category
                    String category = extractJsonValue(item, "category");
                    q.setCategory(category != null ? category.toLowerCase() : "general");
                    
                    // 提取 keywords
                    String keywords = extractJsonValue(item, "keywords");
                    q.setKeywords(keywords);
                    
                    if (content != null && !content.isEmpty()) {
                        questions.add(q);
                    }
                } catch (Exception e) {
                    log.warn("解析单个问题失败: {}", item, e);
                }
            }
            
        } catch (Exception e) {
            log.error("解析问题列表失败", e);
        }
        
        return questions;
    }
    
    /**
     * 简单的 JSON 值提取
     */
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
    
    /**
     * Node 5: 质量控制
     * 去重、评分、过滤低质量问题
     */
    private InterviewGraphState qualityControl(InterviewGraphState state) {
        log.info("Node 5: 质量控制");
        
        List<Question> questions = state.getQuestions();
        
        // 简单去重 (基于内容相似度)
        Set<String> seen = new HashSet<>();
        List<Question> filtered = questions.stream()
                .filter(q -> {
                    String normalized = q.getContent().toLowerCase().replaceAll("\\s+", "");
                    if (seen.contains(normalized)) {
                        log.debug("去重: {}", q.getContent());
                        return false;
                    }
                    seen.add(normalized);
                    return true;
                })
                .collect(Collectors.toList());
        
        state.setFilteredQuestions(filtered);
        log.info("✓ 质量控制完成, 去重后剩余 {} 道题", filtered.size());
        
        return state;
    }
    
    /**
     * Node 6: 组织问题池
     * 按技术分类和难度分组
     */
    private InterviewGraphState organizePool(InterviewGraphState state) {
        log.info("Node 6: 组织问题池");
        
        List<Question> questions = state.getFilteredQuestions();
        
        QuestionPool pool = new QuestionPool();
        pool.setPoolId(IdUtil.simpleUUID());
        pool.setUserId(state.getUserId());
        pool.setResumeId(state.getResumeId());
        pool.setAllQuestions(questions);
        pool.setTotalCount(questions.size());
        pool.setGeneratedAt(LocalDateTime.now());
        
        // 按技术分类分组
        Map<String, List<Question>> byCategory = questions.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getCategory() != null ? q.getCategory() : "general"
                ));
        pool.setByCategory(byCategory);
        
        // 按难度分组
        Map<Question.DifficultyLevel, List<Question>> byLevel = questions.stream()
                .collect(Collectors.groupingBy(Question::getLevel));
        pool.setByLevel(byLevel);
        
        // 生成面试建议
        List<String> suggestions = generateInterviewSuggestions(byCategory, byLevel);
        pool.setInterviewSuggestions(suggestions);
        
        state.setFinalPool(pool);
        
        log.info("✓ 问题池组织完成");
        log.info("  - 技术分类: {}", byCategory.keySet());
        log.info("  - 难度分布: 基础 {} / 进阶 {} / 高级 {}", 
                byLevel.getOrDefault(Question.DifficultyLevel.BASIC, Collections.emptyList()).size(),
                byLevel.getOrDefault(Question.DifficultyLevel.ADVANCED, Collections.emptyList()).size(),
                byLevel.getOrDefault(Question.DifficultyLevel.EXPERT, Collections.emptyList()).size());
        
        return state;
    }
    
    /**
     * 生成面试建议
     */
    private List<String> generateInterviewSuggestions(Map<String, List<Question>> byCategory,
                                                     Map<Question.DifficultyLevel, List<Question>> byLevel) {
        List<String> suggestions = new ArrayList<>();
        
        suggestions.add("建议面试时长: " + (byLevel.values().stream().mapToInt(List::size).sum() * 3) + " 分钟");
        
        // 按类别建议
        byCategory.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(3)
                .forEach(entry -> {
                    suggestions.add(String.format("重点考察 %s (%d 道题)", 
                            entry.getKey(), entry.getValue().size()));
                });
        
        // 难度建议
        suggestions.add("建议提问顺序: 基础题 → 进阶题 → 高级题");
        
        return suggestions;
    }
}
