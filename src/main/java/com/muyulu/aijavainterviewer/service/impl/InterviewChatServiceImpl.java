package com.muyulu.aijavainterviewer.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyulu.aijavainterviewer.assistant.InterViewAssistant;
import com.muyulu.aijavainterviewer.mapper.InterviewChatMapper;
import com.muyulu.aijavainterviewer.model.entity.InterviewChat;
import com.muyulu.aijavainterviewer.model.entity.Resume;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.model.enums.InterviewChatEnum;
import com.muyulu.aijavainterviewer.service.InterviewChatService;
import com.muyulu.aijavainterviewer.service.ResumeService;
import com.muyulu.aijavainterviewer.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@Slf4j
public class InterviewChatServiceImpl extends ServiceImpl<InterviewChatMapper, InterviewChat> implements InterviewChatService {

    @Resource
    private InterViewAssistant interViewAssistant;
    @Resource
    private UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private ResumeService resumeService;

    @Override
    public Flux<String> startInterviewChat(HttpServletRequest request) {
        //初始化面试聊天记录
        InterviewChat interviewChat = new InterviewChat();
        User loginUser = userService.getLoginUser(request);
        interviewChat.setUserId(loginUser.getId());
        interviewChat.setResumeId(loginUser.getResumeId());
        interviewChat.setStatus(InterviewChatEnum.INTERVIEWING.name());
        //查询当前用户下这个简历是否为第一次开始聊天
        log.info("检查用户 {} 的简历 {} 是否已有聊天记录", loginUser.getId(), loginUser.getResumeId());
        boolean exists = lambdaQuery().eq(InterviewChat::getResumeId, interviewChat.getResumeId()).exists();
        //如果不存在，则保存
        if (!exists) {
            save(interviewChat);
        }

        //从redis中获取简历内容
        log.info("从Redis中获取用户 {} 的简历 {}", loginUser.getId(), loginUser.getResumeId());
        String resumeContent = redisTemplate.opsForValue().get(loginUser.getResumeId());
        if(resumeContent == null){
            //从数据库中读取简历内容
            log.info("Redis中未找到用户 {} 的简历 {}，从数据库中读取", loginUser.getId(), loginUser.getResumeId());
            Resume resumeFromDB = resumeService.getByResumeId(loginUser.getResumeId());
            resumeContent = JSONUtil.toJsonStr(resumeFromDB);
        }
        String userInput = "面试挂你好，这就是我的简历内容：" +  resumeContent;
        return interViewAssistant.chatStream(loginUser.getResumeId(), userInput);
    }

    @Override
    public Flux<String> continueInterviewChat(HttpServletRequest request, String userInput) {
        User loginUser = userService.getLoginUser(request);
        log.info("用户 {} 继续面试聊天，输入内容：{}", loginUser.getId(), userInput);
        return interViewAssistant.chatStream(loginUser.getResumeId(), userInput);
    }
}
