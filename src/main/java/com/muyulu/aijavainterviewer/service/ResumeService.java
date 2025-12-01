package com.muyulu.aijavainterviewer.service;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyulu.aijavainterviewer.model.entity.Resume;
import com.muyulu.aijavainterviewer.model.vo.ResumeVo;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService extends IService<Resume> {

    /**
     * 将上传的简历文件转换为文本内容
     * @param multipartFile
     * @return
     */
    String file2Content(MultipartFile multipartFile);

    /**
     * 分析简历内容并返回Resume对象
     * @param resumeContent 简历内容
     * @return 分析后的Resume对象
     */
    ResumeVo getAnalyzedResume(String resumeContent) throws GraphRunnerException;

    /**
     * 根据resumeId获取简历
     * @param resumeId
     * @return
     */
    Resume getByResumeId(String resumeId);

    /**
     * 根据resumeId更新简历
     * @param resume
     */
    void updateByResumeId(Resume resume);
}
