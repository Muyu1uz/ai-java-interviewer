package com.muyulu.aijavainterviewer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 文档检索服务
 * 用于从向量库中检索与问题相关的知识文档
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;

    /**
     * 根据简历内容和查询文本检索相关文档
     * 
     * @param resumeContent 简历内容(用于提取技术栈关键词)
     * @param topK 返回最相关的 K 个结果
     * @return 相关文档内容列表
     */
    public List<String> searchRelevantKnowledge(String resumeContent, int topK) {
        log.debug("基于简历检索相关知识, topK: {}", topK);
        
        try {
            // 1. 从简历中提取关键技术点作为查询
            String query = extractTechKeywords(resumeContent);
            
            // 2. 构建搜索请求
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(0.6)  // 相似度阈值 0.6
                    .build();
            
            // 3. 执行向量检索
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            if (results.isEmpty()) {
                log.warn("未找到相关知识文档, query: {}", query);
                return List.of();
            }
            
            // 4. 提取文档内容
            List<String> contents = results.stream()
                    .map(doc -> {
                        String content = doc.getFormattedContent();
                        String source = (String) doc.getMetadata().get("source");
                        return String.format("[来源: %s]\n%s", source, content);
                    })
                    .collect(Collectors.toList());
            
            log.info("检索到 {} 个相关知识片段", contents.size());
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
     * @param topK 检索文档数量
     * @return 格式化的知识上下文
     */
    public String buildRagContext(String resumeContent, int topK) {
        List<String> knowledgeDocs = searchRelevantKnowledge(resumeContent, topK);
        
        if (knowledgeDocs.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("### 📚 面试知识库参考 ###\n\n");
        context.append("以下是与候选人简历相关的技术知识，可以基于这些内容提出针对性问题：\n\n");
        
        for (int i = 0; i < knowledgeDocs.size(); i++) {
            context.append("【知识点 ").append(i + 1).append("】\n");
            context.append(knowledgeDocs.get(i)).append("\n\n");
        }
        
        context.append("---\n");
        context.append("请基于以上知识库内容和候选人简历，提出深入、专业的面试问题。\n\n");
        
        return context.toString();
    }

    /**
     * 从简历内容提取技术关键词
     */
    private String extractTechKeywords(String resumeContent) {
        if (resumeContent == null || resumeContent.isEmpty()) {
            return "Java 后端开发";
        }
        
        // 简单提取：取前500字符作为上下文
        String summary = resumeContent.length() > 500 
                ? resumeContent.substring(0, 500) 
                : resumeContent;
        
        return summary;
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
