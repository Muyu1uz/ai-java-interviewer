package com.muyulu.aijavainterviewer.controller;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.google.common.hash.BloomFilter;
import com.muyulu.aijavainterviewer.common.annotation.RateLimit;
import com.muyulu.aijavainterviewer.common.annotation.RequireLogin;
import com.muyulu.aijavainterviewer.model.dto.ResumeAnalyzeRequest;
import com.muyulu.aijavainterviewer.model.entity.Resume;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.common.Result;
import com.muyulu.aijavainterviewer.model.vo.ResumeVo;
import com.muyulu.aijavainterviewer.service.ResumeService;
import com.muyulu.aijavainterviewer.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Resource
    private ResumeService resumeService;
    @Resource
    private UserService userService;
    @Resource
    private BloomFilter<String> resumeBloomFilter;

    @PostMapping("/create")
    @Transactional
    @RequireLogin
    @RateLimit(
        name = "resume_upload",
        capacity = 5,
        rate = 1,
        limitType = RateLimit.LimitType.USER,
        message = "简历上传过于频繁，请稍后再试"
    )
    public Result<ResumeVo> create(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws GraphRunnerException {
        //转换简历文件为文本内容
        String content = resumeService.file2Content(file);
        //分析简历
        ResumeAnalyzeRequest resumeAnalyzeRequest = new ResumeAnalyzeRequest(content);
        ResumeVo resumeVo = resumeService.getAnalyzedResume(resumeAnalyzeRequest.resumeContent());
        Resume resume = new Resume();
        BeanUtils.copyProperties(resumeVo, resume);
        //获取当前用户
        User currentUser = userService.getLoginUser(request);
        String resumeId = file.getOriginalFilename() + "_" + currentUser.getId();
        //更新用户简历id
        currentUser.setResumeId(resumeId);
        userService.updateById(currentUser);
        resume.setResumeId(resumeId);
        Resume oldResume = resumeService.getByResumeId(resume.getResumeId());
        if(oldResume != null){
            resumeService.updateByResumeId(resume);
        }
        else{
            //存入缓存
            resumeService.cacheResume(resume);
            //更新布隆过滤器
            resumeBloomFilter.put(resume.getResumeId());
            //存入数据库
            resumeService.save(resume);
        }
        return Result.success("简历上传成功", resumeVo);
    }

    /**
     * 检查当前用户是否已经上传简历
     */
    @GetMapping("/check")
    @RequireLogin
    public Result<Boolean> checkResumeUploaded(HttpServletRequest request) {
        boolean hasResume = resumeService.getResume(request);
        return Result.success(hasResume);
    }

}
