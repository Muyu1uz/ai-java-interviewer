package com.muyulu.aijavainterviewer.common.initializer;

import com.muyulu.aijavainterviewer.common.component.DocumentKeywordExtractor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档向量库初始化器
 * 在应用启动时将 resources/document 下的所有 MD 文档加载到向量库
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentVectorStoreInitializer {

    private final VectorStore vectorStore;
    private final DocumentKeywordExtractor keywordExtractor;

    @Value("${rag.document.auto-load:true}")
    private boolean autoLoad;
    
    @Value("${rag.document.force-reload:false}")
    private boolean forceReload;

    @Value("${rag.document.chunk-size:800}")
    private int chunkSize;

    @Value("${rag.document.chunk-overlap:200}")
    private int chunkOverlap;

    @Value("${rag.document.ai-keywords:false}")
    private boolean useAiKeywords;

    @PostConstruct
    public void initVectorStore() {
        if (!autoLoad) {
            log.info("文档自动加载已禁用,跳过向量库初始化");
            return;
        }

        try {
            // 检查向量库是否已有数据
            if (!forceReload && isVectorStoreInitialized()) {
                log.info("========== 向量库已初始化,跳过文档加载 ==========");
                log.info("提示: 如需重新加载,请设置 rag.document.force-reload=true");
                return;
            }
            
            log.info("========== 开始加载文档到向量库 ==========");
            if (forceReload) {
                log.info("强制重新加载模式已启用");
            }
            
            // 1. 扫描所有 MD 文档
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:document/**/*.md");
            
            log.info("找到 {} 个 Markdown 文档", resources.length);
            
            if (resources.length == 0) {
                log.warn("未找到任何 Markdown 文档,跳过向量库初始化");
                return;
            }
            
            // 2. 配置文本分割器 (作为后备方案,用于分割超长段落)
            TokenTextSplitter textSplitter = new TokenTextSplitter(chunkSize, chunkOverlap, 5, 10000, true);
            
            // 3. 批量加载文档
            List<Document> allDocuments = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (Resource resource : resources) {
                try {
                    String filename = resource.getFilename();
                    log.debug("处理文档: {}", filename);
                    
                    // 4. 使用 MarkdownDocumentReader 读取
                    MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                            .withHorizontalRuleCreateDocument(true)
                            .withIncludeCodeBlock(true)
                            .withIncludeBlockquote(true)
                            .build();
                    
                    MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                    List<Document> documents = reader.get();
                    
                    // 5. 文本分割 (优先使用标题语义分割, 如果片段过长则使用Token分割)
                    List<Document> splitDocuments = splitDocumentsByHeaders(documents);
                    
                    // 检查分割后的片段是否过大,如果是则再次进行Token分割
                    List<Document> finalDocuments = new ArrayList<>();
                    for (Document doc : splitDocuments) {
                        if (doc.getFormattedContent().length() > chunkSize * 4) { // 粗略估算,字符数 > chunk * 4
                            finalDocuments.addAll(textSplitter.apply(List.of(doc)));
                        } else {
                            finalDocuments.add(doc);
                        }
                    }
                    splitDocuments = finalDocuments;
                    
                    // 6. 添加元数据
                    for (Document doc : splitDocuments) {
                        doc.getMetadata().put("source", filename);
                        doc.getMetadata().put("type", "interview_knowledge");
                        doc.getMetadata().put("category", extractCategory(filename));
                        doc.getMetadata().put("load_time", System.currentTimeMillis());
                        
                        // 使用 AI 提取关键词 (如果开启)
                        if (useAiKeywords) {
                            String keywords = keywordExtractor.extractKeywords(doc.getFormattedContent());
                            if (org.springframework.util.StringUtils.hasText(keywords)) {
                                doc.getMetadata().put("keywords", keywords);
                                log.debug("  + 关键词: {}", keywords);
                            }
                        }
                    }
                    
                    allDocuments.addAll(splitDocuments);
                    successCount++;
                    
                    log.info("✓ 成功加载: {} (分割成 {} 个片段)", filename, splitDocuments.size());
                    
                } catch (Exception e) {
                    log.error("✗ 加载文档失败: {}", resource.getFilename(), e);
                    failCount++;
                }
            }
            
            // 7. 分批写入向量库 (DashScope Embedding API 限制每次最多25个文本)
            if (!allDocuments.isEmpty()) {
                log.info("开始写入 {} 个文档片段到向量库...", allDocuments.size());
                long startTime = System.currentTimeMillis();
                
                int batchSize = 10; // 减小批次大小,提高稳定性
                int totalBatches = (int) Math.ceil((double) allDocuments.size() / batchSize);
                int processedCount = 0;
                int failedBatches = 0;
                
                for (int i = 0; i < allDocuments.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, allDocuments.size());
                    List<Document> batch = allDocuments.subList(i, endIndex);
                    int currentBatch = (i / batchSize) + 1;
                    
                    // 重试机制
                    boolean success = false;
                    int retryCount = 0;
                    int maxRetries = 3;
                    
                    while (!success && retryCount < maxRetries) {
                        try {
                            vectorStore.add(batch);
                            processedCount += batch.size();
                            log.info("✓ 批次 {}/{} 完成, 已处理 {}/{} 个片段", 
                                    currentBatch, totalBatches, processedCount, allDocuments.size());
                            success = true;
                        } catch (Exception e) {
                            retryCount++;
                            if (retryCount < maxRetries) {
                                log.warn("✗ 批次 {}/{} 失败,第 {} 次重试... (错误: {})", 
                                        currentBatch, totalBatches, retryCount, e.getMessage());
                                try {
                                    Thread.sleep(2000 * retryCount); // 递增延迟
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                }
                            } else {
                                log.error("✗ 批次 {}/{} 最终失败 (片段 {} - {}), 跳过该批次", 
                                        currentBatch, totalBatches, i, endIndex);
                                failedBatches++;
                            }
                        }
                    }
                }
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("========== 文档加载完成 ==========");
                log.info("✅ 成功文档: {}, 失败文档: {}", successCount, failCount);
                log.info("✅ 总片段数: {}, 已写入: {}, 失败批次: {}, 耗时: {} ms", 
                        allDocuments.size(), processedCount, failedBatches, duration);
            } else {
                log.warn("没有可加载的文档内容");
            }
            
        } catch (Exception e) {
            log.error("向量库初始化失败", e);
            // 不抛出异常,允许应用继续启动
        }
    }
    
    /**
     * 检查向量库是否已初始化(是否已有数据)
     */
    private boolean isVectorStoreInitialized() {
        try {
            List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query("Java")
                    .topK(1)
                    .build()
            );
            boolean initialized = !results.isEmpty();
            if (initialized) {
                log.info("向量库已包含数据,文档总数 >= 1");
            }
            return initialized;
        } catch (Exception e) {
            log.warn("无法检查向量库状态,将继续加载: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 根据 Markdown 标题进行语义分割
     * 保持 H1/H2/H3 下的内容作为一个完整的 Document
     */
    private List<Document> splitDocumentsByHeaders(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        
        for (Document doc : documents) {
            String content = doc.getFormattedContent();
            String[] lines = content.split("\n");
            
            StringBuilder currentChunk = new StringBuilder();
            String currentHeader = ""; // 记录当前最近的标题
            boolean inCodeBlock = false;
            
            for (String line : lines) {
                // 检查是否进入/离开代码块
                if (line.trim().startsWith("```")) {
                    inCodeBlock = !inCodeBlock;
                }
                
                // 检查是否是标题 (且不在代码块中)
                // 匹配 #, ##, ### 等开头
                boolean isHeader = !inCodeBlock && line.trim().matches("^#{1,6}\\s+.*");
                
                if (isHeader) {
                    // 如果之前有内容, 保存之前的块
                    if (currentChunk.length() > 0) {
                        Document newDoc = new Document(currentChunk.toString(), new java.util.HashMap<>(doc.getMetadata()));
                        // 添加当前块所属的标题上下文(上一个标题)
                        newDoc.getMetadata().put("section_title", currentHeader.isEmpty() ? "Introduction" : currentHeader);
                        result.add(newDoc);
                        currentChunk = new StringBuilder();
                    }
                    // 更新当前标题(去掉#号和空格)
                    currentHeader = line.trim().replaceAll("^#+\\s+", "");
                }
                
                currentChunk.append(line).append("\n");
            }
            
            // 添加最后一个块
            if (currentChunk.length() > 0) {
                Document newDoc = new Document(currentChunk.toString(), new java.util.HashMap<>(doc.getMetadata()));
                newDoc.getMetadata().put("section_title", currentHeader.isEmpty() ? "Introduction" : currentHeader);
                result.add(newDoc);
            }
        }
        
        return result;
    }

    /**
     * 从文件名提取分类
     */
    private String extractCategory(String filename) {
        if (filename == null) return "unknown";
        
        String lower = filename.toLowerCase();
        
        // Java Core
        if (lower.contains("java") || lower.contains("jvm") || lower.contains("jdk") || 
            lower.contains("gc") || lower.contains("classloader") || lower.contains("reflection") ||
            lower.contains("spi") || lower.contains("generics") || lower.contains("unsafe")) {
            return "Java Core";
        }
        
        // Concurrency
        if (lower.contains("concurrent") || lower.contains("thread") || lower.contains("lock") ||
            lower.contains("atomic") || lower.contains("aqs") || lower.contains("cas") ||
            lower.contains("future") || lower.contains("blockingqueue")) {
            return "Concurrency";
        }
        
        // Spring Ecosystem
        if (lower.contains("spring") || lower.contains("bean") || lower.contains("aop") || 
            lower.contains("ioc") || lower.contains("transaction") || lower.contains("mvc")) {
            return "Spring";
        }
        
        // Database & Storage
        if (lower.contains("mysql") || lower.contains("sql") || lower.contains("innodb") || 
            lower.contains("transaction") || lower.contains("index") || lower.contains("log") ||
            lower.contains("mybatis")) {
            return "Database";
        }
        
        // Redis / Cache
        if (lower.contains("redis") || lower.contains("cache") || lower.contains("bloom")) {
            return "Redis";
        }
        
        // Network
        if (lower.contains("http") || lower.contains("tcp") || lower.contains("udp") || 
            lower.contains("network") || lower.contains("ip") || lower.contains("socket") ||
            lower.contains("dns") || lower.contains("cdn") || lower.contains("arp")) {
            return "Network";
        }
        
        // Data Structures & Algorithms
        if (lower.contains("algorithm") || lower.contains("structure") || lower.contains("tree") || 
            lower.contains("list") || lower.contains("map") || lower.contains("queue") || 
            lower.contains("stack") || lower.contains("sort") || lower.contains("heap") ||
            lower.contains("offer") || lower.contains("leetcode")) {
            return "Algorithm";
        }
        
        // System Design / Architecture
        if (lower.contains("design") || lower.contains("distributed") || lower.contains("microservice") ||
            lower.contains("load-balancing") || lower.contains("circuit-breaker") || lower.contains("limit") ||
            lower.contains("idempotency") || lower.contains("high-availability")) {
            return "System Design";
        }

        // Tools / OS / Others
        if (lower.contains("linux") || lower.contains("shell") || lower.contains("os") || 
            lower.contains("operating") || lower.contains("docker") || lower.contains("k8s") ||
            lower.contains("git") || lower.contains("maven")) {
            return "DevOps";
        }
        
        return "General";
    }
}
