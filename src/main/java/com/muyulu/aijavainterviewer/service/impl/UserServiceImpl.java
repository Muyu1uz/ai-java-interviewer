package com.muyulu.aijavainterviewer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyulu.aijavainterviewer.mapper.UserMapper;
import com.muyulu.aijavainterviewer.model.dto.UserDto;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService  {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserMapper userMapper;

    public User register(UserDto request) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username());
        if (userMapper.selectOne(query) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setUserAccount(request.userAccount());
        user.setPassword(passwordEncoder.encode(request.password()));
        userMapper.insert(user);
        return user;
    }

    public User login(UserDto request) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username());
        User user = userMapper.selectOne(query);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!user.getUserAccount().equals(request.userAccount()) || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("账号/密码错误");
        }
        return user;
    }
}
