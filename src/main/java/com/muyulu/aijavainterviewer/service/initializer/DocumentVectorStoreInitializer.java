package com.muyulu.aijavainterviewer.service.initializer;

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

    @Value("${rag.document.auto-load:true}")
    private boolean autoLoad;
    
    @Value("${rag.document.force-reload:false}")
    private boolean forceReload;

    @Value("${rag.document.chunk-size:800}")
    private int chunkSize;

    @Value("${rag.document.chunk-overlap:200}")
    private int chunkOverlap;

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
            
            // 2. 配置文本分割器 (减小chunk大小,避免单个文档过长)
            TokenTextSplitter textSplitter = new TokenTextSplitter(400, 100, 5, 10000, true);
            
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
                    
                    // 5. 文本分割 (将长文档切分成小块)
                    List<Document> splitDocuments = textSplitter.apply(documents);
                    
                    // 6. 添加元数据
                    for (Document doc : splitDocuments) {
                        doc.getMetadata().put("source", filename);
                        doc.getMetadata().put("type", "interview_knowledge");
                        doc.getMetadata().put("category", extractCategory(filename));
                        doc.getMetadata().put("load_time", System.currentTimeMillis());
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
     * 从文件名提取分类
     */
    private String extractCategory(String filename) {
        if (filename == null) return "unknown";
        
        String lower = filename.toLowerCase();
        if (lower.contains("java") || lower.contains("jvm")) return "Java";
        if (lower.contains("spring")) return "Spring";
        if (lower.contains("database") || lower.contains("mysql") || lower.contains("redis")) return "Database";
        if (lower.contains("network")) return "Network";
        if (lower.contains("algorithm")) return "Algorithm";
        if (lower.contains("concurrent") || lower.contains("thread")) return "Concurrency";
        
        return "General";
    }
}
