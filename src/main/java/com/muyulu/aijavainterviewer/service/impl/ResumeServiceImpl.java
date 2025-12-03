package com.muyulu.aijavainterviewer.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.BloomFilter;
import com.muyulu.aijavainterviewer.assistant.ResumeAgent;
import com.muyulu.aijavainterviewer.exception.ResumeException;
import com.muyulu.aijavainterviewer.mapper.ResumeMapper;
import com.muyulu.aijavainterviewer.model.entity.Resume;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.model.vo.ResumeVo;
import com.muyulu.aijavainterviewer.service.ResumeService;
import com.muyulu.aijavainterviewer.service.UserService;
import com.muyulu.aijavainterviewer.tool.FileToStringConverterTool;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@Slf4j
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    @Resource
    private ResumeAgent resumeAgent;
    @Resource
    private FileToStringConverterTool fileToStringConverterTool;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private BloomFilter<String> resumeBloomFilter;
    @Autowired
    private UserService userService;

    @Override
    public String file2Content(MultipartFile multipartFile) {
        log.info("开始转换简历文件: {}", multipartFile.getOriginalFilename());
        return processResumeIntelligently(multipartFile);
    }

    /**
     * 智能处理简历 - 自动选择最适合的提取方式
     */
    public String processResumeIntelligently(MultipartFile resumeFile) {
        log.info("智能处理简历文件: {}", resumeFile.getOriginalFilename());

        String filename = resumeFile.getOriginalFilename();
        if (filename == null) {
            throw ResumeException.uploadFailed("文件名不能为空");
        }

        String extension = getFileExtension(filename).toLowerCase();
        String extractionType;

        // 根据文件类型选择最适合的提取方式
        switch (extension) {
            case "pdf" -> {
                // PDF文件先尝试直接文本提取，如果内容太少再用OCR
                String textContent = extractWithType(resumeFile, "TEXT");
                if (textContent.trim().length() < 100) {
                    log.info("PDF文本提取内容较少，尝试使用OCR");
                    return extractWithType(resumeFile, "OCR");
                }
                return textContent;
            }
            case "png", "jpg", "jpeg", "gif", "bmp" -> {
                // 图片文件直接使用OCR
                return extractWithType(resumeFile, "OCR");
            }
            default -> throw ResumeException.uploadFailed("不支持的文件格式: " + extension);
        }
    }
    private String extractWithType(MultipartFile file, String extractionType) {
        FileToStringConverterTool.Request request = new FileToStringConverterTool.Request(
                null, file, extractionType
        );

        String content = fileToStringConverterTool.apply(request);

        if (content.startsWith("文件转换失败")) {
            throw ResumeException.parseFailed(content);
        }

        return content;
    }

    @Override
    public ResumeVo getAnalyzedResume(String resumeContent) throws GraphRunnerException {
        String resultJson = resumeAgent.analyzeResume(resumeContent);
        ResumeVo resumeVo = null;
        resumeVo = JSONUtil.toBean(resultJson, ResumeVo.class);
        return resumeVo;
    }

    @Override
    public Resume getByResumeId(String resumeId) {

        //先从缓存中获取
        Map<Object, Object> resumeMap = redisTemplate.opsForHash().entries(resumeId);
        if (resumeMap != null && !resumeMap.isEmpty()) {
            try {
                return objectMapper.convertValue(resumeMap, Resume.class);
            } catch (IllegalArgumentException ex) {
                log.error("Failed to convert cached resume {}", resumeId, ex);
            }
        }

        //通过布隆过滤器检查数据库中有没有这个数据
        if (!resumeBloomFilter.mightContain(resumeId)) {
            log.debug("Bloom filter miss for resumeId={}, short-circuit DB lookup", resumeId);
            return null;
        }

        //如果有，再从数据库中获取
        return this.lambdaQuery().eq(Resume::getResumeId, resumeId).one();
    }

    @Override
    public void updateByResumeId(Resume resume) {
        //删除缓存
        redisTemplate.delete(resume.getResumeId());
        //更新数据库
        this.lambdaUpdate().eq(Resume::getResumeId, resume.getResumeId()).update(resume);
        resumeBloomFilter.put(resume.getResumeId());
        //延时双删
        new Thread(() -> {
            try {
                Thread.sleep(500);
                redisTemplate.delete(resume.getResumeId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

    }

    @Override
    public void cacheResume(Resume resume) {
        if (resume == null || resume.getResumeId() == null) {
            log.warn("Resume为空");
            return;
        }
        String jsonStr = JSONUtil.toJsonStr(resume);
        redisTemplate.opsForValue().set(resume.getResumeId(), jsonStr);
    }

    @Override
    public boolean getResume(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        String resumeId = currentUser.getResumeId();
        if(resumeId == null){
            return false;
        }
        //通过缓存
        String resumeContent = redisTemplate.opsForValue().get(resumeId);
        if (resumeContent == null) {
            return false;
        }
        //通过布隆过滤器检查数据库中有没有这个数据
        if (!resumeBloomFilter.mightContain(resumeId)) {
            log.debug("Bloom filter miss for resumeId={}, short-circuit DB lookup", resumeId);
            return false;
        }
        Resume resume = this.lambdaQuery().eq(Resume::getResumeId, resumeId).one();
        return resume != null;
    }


    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ?  filename.substring(lastDotIndex + 1) : "";
    }

}
