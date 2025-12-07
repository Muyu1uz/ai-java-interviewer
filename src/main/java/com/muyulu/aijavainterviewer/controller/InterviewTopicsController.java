package com.muyulu.aijavainterviewer.controller;

import com.muyulu.aijavainterviewer.common.annotation.RequireLogin;
import com.muyulu.aijavainterviewer.assistant.InterViewAssistant;
import com.muyulu.aijavainterviewer.common.Result;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 面试知识点管理接口
 */
@RestController
@RequestMapping("/interview-topics")
public class InterviewTopicsController {

    @Resource
    private InterViewAssistant interViewAssistant;
    
    @Resource
    private UserService userService;

    /**
     * 获取当前用户的已提问技术点列表
     */
    @GetMapping("/list")
    @RequireLogin
    public Result<Map<String, Object>> getAskedTopics(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String chatId = loginUser.getResumeId();
        
        Set<String> topics = interViewAssistant.getAskedTopics(chatId);
        long count = interViewAssistant.getAskedTopicsCount(chatId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("chatId", chatId);
        result.put("topics", topics);
        result.put("count", count);
        result.put("username", loginUser.getUsername());
        
        return Result.success("获取成功", result);
    }

    /**
     * 清除当前用户的已提问技术点（重新开始面试）
     */
    @DeleteMapping("/clear")
    @RequireLogin
    public Result<String> clearAskedTopics(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String chatId = loginUser.getResumeId();
        
        interViewAssistant.clearAskedTopics(chatId);
        
        return Result.success("已清除技术点记录，可以重新开始面试");
    }

    /**
     * 添加已提问技术点（管理功能）
     */
    @PostMapping("/add")
    @RequireLogin
    public Result<String> addAskedTopics(
            HttpServletRequest request,
            @RequestParam("topics") String topicsStr) {
        
        User loginUser = userService.getLoginUser(request);
        String chatId = loginUser.getResumeId();
        
        // 解析技术点
        Set<String> topics = Set.of(topicsStr.split("[,，、]"));
        
        interViewAssistant.addAskedTopics(chatId, topics);
        
        return Result.success("添加成功，共添加 " + topics.size() + " 个技术点");
    }

    /**
     * 获取技术点统计信息
     */
    @GetMapping("/stats")
    @RequireLogin
    public Result<Map<String, Object>> getTopicsStats(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String chatId = loginUser.getResumeId();
        
        Set<String> topics = interViewAssistant.getAskedTopics(chatId);
        long count = interViewAssistant.getAskedTopicsCount(chatId);
        
        // 统计各类技术的数量
        Map<String, Integer> categoryCount = new HashMap<>();
        categoryCount.put("Spring系列", 0);
        categoryCount.put("数据库", 0);
        categoryCount.put("中间件", 0);
        categoryCount.put("JVM/并发", 0);
        categoryCount.put("分布式", 0);
        categoryCount.put("其他", 0);
        
        for (String topic : topics) {
            if (topic.toLowerCase().contains("spring")) {
                categoryCount.put("Spring系列", categoryCount.get("Spring系列") + 1);
            } else if (topic.toLowerCase().matches(".*(mysql|redis|数据库|索引|事务|mvcc).*")) {
                categoryCount.put("数据库", categoryCount.get("数据库") + 1);
            } else if (topic.toLowerCase().matches(".*(kafka|rabbitmq|mq|nginx).*")) {
                categoryCount.put("中间件", categoryCount.get("中间件") + 1);
            } else if (topic.toLowerCase().matches(".*(jvm|juc|线程|锁|synchronized|volatile).*")) {
                categoryCount.put("JVM/并发", categoryCount.get("JVM/并发") + 1);
            } else if (topic.toLowerCase().matches(".*(分布式|微服务|限流|熔断|docker|kubernetes).*")) {
                categoryCount.put("分布式", categoryCount.get("分布式") + 1);
            } else {
                categoryCount.put("其他", categoryCount.get("其他") + 1);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", count);
        result.put("categoryCount", categoryCount);
        result.put("topics", topics);
        
        return Result.success("统计成功", result);
    }
}
