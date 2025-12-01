package com.muyulu.aijavainterviewer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyulu.aijavainterviewer.mapper.UserMapper;
import com.muyulu.aijavainterviewer.model.dto.UserDto;
import com.muyulu.aijavainterviewer.model.dto.UserLoginDto;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.muyulu.aijavainterviewer.constant.UserConstant.USER_LOGIN_STATE;

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

    public User login(UserLoginDto userDto, HttpServletRequest request) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(User::getUserAccount, userDto.userAccount());

        User user = userMapper.selectOne(query);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!passwordEncoder.matches(userDto.password(), user.getPassword())) {
            throw new IllegalArgumentException("账号/密码错误");
        }
        //保存用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        return user;
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request) {
        //判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null || currentUser.getId() == null){
            throw new IllegalArgumentException("用户未登录");
        }
        return this.getById(currentUser.getId());
    }
}
