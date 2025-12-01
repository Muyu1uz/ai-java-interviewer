package com.muyulu.aijavainterviewer.controller;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.muyulu.aijavainterviewer.model.dto.ResumeAnalyzeRequest;
import com.muyulu.aijavainterviewer.model.entity.Resume;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.service.ResumeService;
import com.muyulu.aijavainterviewer.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/resumes")
public class ResumeController {

    @Resource
    private ResumeService resumeService;
    @Resource
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Resume> create(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws GraphRunnerException {
        //转换简历文件为文本内容
        String content = resumeService.file2Content(file);
        //分析简历
        ResumeAnalyzeRequest resumeAnalyzeRequest = new ResumeAnalyzeRequest(content);
        Resume resume = resumeService.getAnalyzedResume(resumeAnalyzeRequest.resumeContent());
        //获取当前用户
        User currentUser = userService.getLoginUser(request);
        resume.setResumeId(file.getOriginalFilename() + "_" + currentUser.getId());
        resumeService.save(resume);
        return ResponseEntity.ok(resume);
    }
}
