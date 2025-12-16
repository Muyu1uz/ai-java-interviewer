package com.muyulu.aijavainterviewer.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyulu.aijavainterviewer.assistant.InterViewAssistant;
import com.muyulu.aijavainterviewer.common.constant.RedisKeyConstant;
import com.muyulu.aijavainterviewer.graph.InterviewGraphState;
import com.muyulu.aijavainterviewer.mapper.InterviewChatMapper;
import com.muyulu.aijavainterviewer.model.entity.*;
import com.muyulu.aijavainterviewer.model.vo.QuestionPoolVO;
import com.muyulu.aijavainterviewer.model.vo.QuestionVO;
import com.muyulu.aijavainterviewer.model.enums.InterviewChatEnum;
import com.muyulu.aijavainterviewer.service.InterviewChatService;
import com.muyulu.aijavainterviewer.service.InterviewGraphService;
import com.muyulu.aijavainterviewer.service.ResumeService;
import com.muyulu.aijavainterviewer.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    @Resource
    private InterviewGraphService interviewGraphService;

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
        log.info("========== 开始面试流程 ==========");
        log.info("步骤1: 获取简历内容");
        String resumeContent = redisTemplate.opsForValue().get(loginUser.getResumeId()); // 简历缓存key后续可统一
        if(resumeContent == null){
            //从数据库中读取简历内容
            log.info("Redis中未找到简历，从数据库读取");
            Resume resumeFromDB = resumeService.getByResumeId(loginUser.getResumeId());
            resumeContent = JSONUtil.toJsonStr(resumeFromDB);
        }
        
        log.info("步骤2: 使用RAG增强的AI面试官进行对话");
        String userInput = "面试官你好，这是我的简历内容：" + resumeContent;
        return interViewAssistant.chatStreamWithRag(
            loginUser.getResumeId(), 
            userInput,
            resumeContent
        );
    }

    @Override
    public Flux<String> continueInterviewChat(HttpServletRequest request, String userInput) {
        User loginUser = userService.getLoginUser(request);
        log.info("用户 {} 继续面试，输入: {}", loginUser.getId(), userInput);
        
        // 获取简历内容
        String resumeContent = redisTemplate.opsForValue().get(loginUser.getResumeId()); // 简历缓存key后续可统一
        if(resumeContent == null){
            Resume resumeFromDB = resumeService.getByResumeId(loginUser.getResumeId());
            resumeContent = JSONUtil.toJsonStr(resumeFromDB);
        }
        
        // 使用RAG增强的对话
        return interViewAssistant.chatStreamWithRag(
            loginUser.getResumeId(), 
            userInput,
            resumeContent
        );
    }
    
    @Override
    public QuestionPoolVO generateQuestionPool(HttpServletRequest request,
                                              MultipartFile resumeFile,
                                              Integer questionCount) {
        User loginUser = userService.getLoginUser(request);
        log.info("========== 用户 {} 开始生成问题池 ==========", loginUser.getId());
        
        // 构建 Graph State
        InterviewGraphState state = new InterviewGraphState();
        state.setResumeFile(resumeFile);
        state.setQuestionCount(questionCount != null ? questionCount : 20);
        state.setUserId(loginUser.getId());
        state.setResumeId(loginUser.getResumeId());
        
        // 调用 Graph 服务生成问题池
        QuestionPool questionPool = interviewGraphService.generateQuestionPool(state);
        
        log.info("========== 问题池生成完成, 共 {} 道题 ==========", questionPool.getTotalCount());
        
        // 转换为 VO
        QuestionPoolVO questionPoolVO = convertToVO(questionPool);
        redisTemplate.opsForValue()
                .set(RedisKeyConstant.QUESTION_POOL_PREFIX + loginUser.getId() + ":" + loginUser.getResumeId(),
                        JSONUtil.toJsonStr(questionPoolVO), 1, TimeUnit.HOURS);
        log.info("问题池已缓存至 Redis，Key: {}", RedisKeyConstant.QUESTION_POOL_PREFIX + loginUser.getId() + ":" + loginUser.getResumeId());
        return questionPoolVO;
    }

    @Override
    public QuestionPoolVO preloadQuestionPool(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String key = RedisKeyConstant.QUESTION_POOL_PREFIX + loginUser.getId() + ":" + loginUser.getResumeId();
        String poolJson = redisTemplate.opsForValue().get(key);
        if (poolJson != null) {
            log.info("预加载问题池成功，Redis Key: {}, 内容: {}", key, poolJson);
            return JSONUtil.toBean(poolJson, QuestionPoolVO.class);
        }
        log.info("预加载问题池失败");
        return null;
    }

    /**
     * 将 QuestionPool 转换为前端展示的 VO
     */
    private QuestionPoolVO convertToVO(QuestionPool pool) {
        // 转换问题列表
        List<QuestionVO> questionVOs = pool.getAllQuestions().stream()
                .map(q -> QuestionVO.builder()
                        .content(q.getContent())
                        .category(q.getCategory())
                        .level(q.getLevel() != null ? q.getLevel().getDescription() : "未知")
                        .keywords(q.getKeywords())
                        .build())
                .toList();
        
        // 统计分类数量
        Map<String, Integer> categoryStats = pool.getByCategory().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()
                ));
        
        // 统计难度数量
        Map<String, Integer> levelStats = pool.getByLevel().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getKey().getDescription(),
                        e -> e.getValue().size()
                ));
        
        return QuestionPoolVO.builder()
                .poolId(pool.getPoolId())
                .totalCount(pool.getTotalCount())
                .questions(questionVOs)
                .categoryStats(categoryStats)
                .levelStats(levelStats)
                .suggestions(pool.getInterviewSuggestions())
                .generatedAt(pool.getGeneratedAt())
                .build();
    }
}
