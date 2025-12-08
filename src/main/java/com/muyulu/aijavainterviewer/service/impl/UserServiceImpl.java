package com.muyulu.aijavainterviewer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyulu.aijavainterviewer.common.exception.UserException;
import com.muyulu.aijavainterviewer.mapper.UserMapper;
import com.muyulu.aijavainterviewer.model.dto.UserDto;
import com.muyulu.aijavainterviewer.model.dto.UserLoginDto;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.model.vo.UserLoginVo;
import com.muyulu.aijavainterviewer.service.UserService;
import com.muyulu.aijavainterviewer.common.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService  {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    public User register(UserDto request) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username());
        if (userMapper.selectOne(query) != null) {
            throw UserException.alreadyExists();
        }
        User user = new User();
        user.setUsername(request.username());
        user.setUserAccount(request.userAccount());
        user.setPassword(passwordEncoder.encode(request.password()));
        userMapper.insert(user);
        return user;
    }

    public UserLoginVo login(UserLoginDto userDto, HttpServletRequest request) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userDto.userAccount());

        User user = userMapper.selectOne(query);
        if (user == null) {
            throw UserException.notFound();
        }
        if (!passwordEncoder.matches(userDto.password(), user.getPassword())) {
            throw UserException.passwordError();
        }
        
        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 构造返回对象
        UserLoginVo loginVo = new UserLoginVo();
        loginVo.setId(user.getId());
        loginVo.setUsername(user.getUsername());
        loginVo.setUserAccount(user.getUserAccount());
        loginVo.setToken(token);

        log.info("用户登录成功，用户ID: {}", user.getId());
        return loginVo;
    }

    @Override
    public void logout(HttpServletRequest request) {
        // 获取 Token
        String token = extractToken(request);
        if (token == null) {
            throw UserException.notLogin();
        }
        
        // 获取当前用户信息
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw UserException.notLogin();
        }
        
        Long userId = (Long) userIdObj;
        
        // 将 Token 加入黑名单（Redis）
        // 过期时间设置为 Token 的剩余有效时间
        Long expiration = jwtUtil.getExpirationFromToken(token);
        if (expiration != null && expiration > 0) {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(
                blacklistKey, 
                String.valueOf(userId), 
                expiration, 
                TimeUnit.MILLISECONDS
            );
        }
        
        log.info("用户退出登录，用户ID: {}", userId);
    }
    
    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 检查 Token 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request) {
        // 从请求属性中获取用户ID(由 JWT 过滤器设置)
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw UserException.notLogin();
        }
        
        Long userId = (Long) userIdObj;
        User user = this.getById(userId);
        if (user == null) {
            throw UserException.notFound();
        }
        
        return user;
    }
}
