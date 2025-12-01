package com.muyulu.aijavainterviewer.controller;

import com.muyulu.aijavainterviewer.model.dto.UserDto;
import com.muyulu.aijavainterviewer.model.dto.UserLoginDto;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.service.UserService;
import com.muyulu.aijavainterviewer.service.impl.UserServiceImpl;
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
    public User register(@Valid @RequestBody UserDto request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public User login(@Valid @RequestBody UserLoginDto userDto, HttpServletRequest request) {
        return userService.login(userDto, request);
    }
}
