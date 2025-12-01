package com.muyulu.aijavainterviewer.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyulu.aijavainterviewer.assistant.ResumeAgent;
import com.muyulu.aijavainterviewer.mapper.ResumeMapper;
import com.muyulu.aijavainterviewer.model.entity.Resume;
import com.muyulu.aijavainterviewer.service.ResumeService;
import com.muyulu.aijavainterviewer.tool.FileToStringConverterTool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    @Resource
    private ResumeAgent resumeAgent;
    @Resource
    private FileToStringConverterTool fileToStringConverterTool;

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
            throw new IllegalArgumentException("文件名不能为空");
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
            default -> throw new IllegalArgumentException("不支持的文件格式: " + extension);
        }
    }
    private String extractWithType(MultipartFile file, String extractionType) {
        FileToStringConverterTool.Request request = new FileToStringConverterTool.Request(
                null, file, extractionType
        );

        String content = fileToStringConverterTool.apply(request);

        if (content.startsWith("文件转换失败")) {
            throw new RuntimeException("文件提取失败: " + content);
        }

        return content;
    }

    @Override
    public Resume getAnalyzedResume(String resumeContent) throws GraphRunnerException {
        String resultJson = resumeAgent.analyzeResume(resumeContent);
        return JSONUtil.toBean(resultJson, Resume.class);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ?  filename.substring(lastDotIndex + 1) : "";
    }

}
