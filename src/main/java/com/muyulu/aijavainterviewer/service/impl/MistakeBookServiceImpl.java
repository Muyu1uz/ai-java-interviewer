package com.muyulu.aijavainterviewer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyulu.aijavainterviewer.mapper.MistakeBookMapper;
import com.muyulu.aijavainterviewer.model.entity.MistakeBook;
import com.muyulu.aijavainterviewer.service.MistakeBookService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 错题本服务实现
 */
@Slf4j
@Service
public class MistakeBookServiceImpl implements MistakeBookService {

    @Resource
    private MistakeBookMapper mistakeBookMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String MISTAKE_BOOK_IDEMPOTENT_PREFIX = "mistake_book:idempotent:";
    private static final long IDEMPOTENT_EXPIRE_TIME = 5; // 5分钟过期

    @Override
    public void insertQuestion(Long userId, String questionContent) {
        // 生成幂等性key: userId + questionContent的MD5
        String contentMd5 = DigestUtils.md5DigestAsHex(questionContent.getBytes());
        String idempotencyKey = MISTAKE_BOOK_IDEMPOTENT_PREFIX + userId + ":" + contentMd5;

        // 尝试设置Redis key,如果已存在则返回false
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(idempotencyKey, "1", IDEMPOTENT_EXPIRE_TIME, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(success)) {
            log.warn("用户 {} 重复添加相同的错题,内容: {}", userId, questionContent);
            return;
        }
        
        MistakeBook mistakeBook = new MistakeBook();
        mistakeBook.setUserId(userId);
        mistakeBook.setQuestionContent(questionContent);
        mistakeBook.setCreatedAt(LocalDateTime.now());
        mistakeBook.setUpdatedAt(LocalDateTime.now());
        
        int rows = mistakeBookMapper.insert(mistakeBook);
        log.info("用户 {} 添加错题: {}, 影响行数: {}", userId, questionContent, rows);
    }

    @Override
    public void deleteQuestion(Long userId, Long mistakeBookId) {
        LambdaQueryWrapper<MistakeBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MistakeBook::getId, mistakeBookId);
        
        int rows = mistakeBookMapper.delete(queryWrapper);
        if (rows > 0) {
            log.info("用户 {} 删除错题 ID: {}", userId, mistakeBookId);
        } else {
            log.warn("用户 {} 尝试删除不存在的错题 ID: {}", userId, mistakeBookId);
        }
    }

    @Override
    public Page<MistakeBook> listQuestions(Long userId, int pageNum, int pageSize) {
        Page<MistakeBook> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MistakeBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MistakeBook::getUserId, userId)
                   .orderByDesc(MistakeBook::getCreatedAt);
        
        Page<MistakeBook> result = mistakeBookMapper.selectPage(page, queryWrapper);
        log.info("查询用户 {} 的错题列表,第 {} 页,每页 {} 条,共 {} 条", userId, pageNum, pageSize, result.getTotal());
        return result;
    }

    @Override
    public void addUserAnswer(Long userId, String userAnswer, Long mistakeBookId) {
        MistakeBook mistakeBook = mistakeBookMapper.selectById(mistakeBookId);
        
        if (mistakeBook == null || !mistakeBook.getUserId().equals(userId)) {
            log.warn("用户 {} 尝试为不存在的错题 ID: {} 添加答案", userId, mistakeBookId);
            return;
        }
        
        mistakeBook.setUserAnswer(userAnswer);
        mistakeBook.setUpdatedAt(LocalDateTime.now());
        mistakeBookMapper.updateById(mistakeBook);
        log.info("用户 {} 为错题 ID: {} 添加答案", userId, mistakeBookId);
    }
}
