package com.muyulu.aijavainterviewer.controller;

import com.muyulu.aijavainterviewer.common.annotation.RequireLogin;
import com.muyulu.aijavainterviewer.model.dto.UserDto;
import com.muyulu.aijavainterviewer.model.dto.UserLoginDto;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.common.Result;
import com.muyulu.aijavainterviewer.model.vo.UserLoginVo;
import com.muyulu.aijavainterviewer.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody UserDto request) {
        User user = userService.register(request);
        return Result.success("注册成功", user);
    }

    @PostMapping("/login")
    public Result<UserLoginVo> login(@Valid @RequestBody UserLoginDto userDto, HttpServletRequest request) {
        UserLoginVo loginVo = userService.login(userDto, request);
        return Result.success("登录成功", loginVo);
    }
    
    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    @RequireLogin
    public Result<Void> logout(HttpServletRequest request) {
        userService.logout(request);
        return Result.success("退出登录成功", null);
    }
}
