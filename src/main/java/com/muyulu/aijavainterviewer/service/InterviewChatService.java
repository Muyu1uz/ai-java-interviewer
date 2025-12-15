package com.muyulu.aijavainterviewer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyulu.aijavainterviewer.model.entity.InterviewChat;
import com.muyulu.aijavainterviewer.model.vo.QuestionPoolVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

public interface InterviewChatService extends IService<InterviewChat> {

    /**
     * 开始面试聊天
     * @return
     */
    public Flux<String> startInterviewChat(HttpServletRequest request);

    /**
     * 继续面试聊天
     * @return
     */
    public Flux<String> continueInterviewChat(HttpServletRequest request, String userInput);
    
    /**
     * 一键生成面试问题池
     * @param request HTTP请求
     * @param resumeFile 简历文件
     * @param questionCount 期望生成的问题数量
     * @return 问题池展示VO
     */
    QuestionPoolVO generateQuestionPool(HttpServletRequest request, 
                                       MultipartFile resumeFile, 
                                       Integer questionCount);

    /**
     * 预加载面试问题池
     * @param request
     * @return
     */
    QuestionPoolVO preloadQuestionPool(HttpServletRequest request);
}

