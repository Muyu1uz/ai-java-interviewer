package com.muyulu.aijavainterviewer.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyulu.aijavainterviewer.model.dto.UserDto;
import com.muyulu.aijavainterviewer.model.dto.UserLoginDto;
import com.muyulu.aijavainterviewer.model.entity.User;
import com.muyulu.aijavainterviewer.model.vo.UserLoginVo;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param request
     * @return
     */
    public User register(UserDto request);

    /**
     * 用户登录
     * @param userDto
     * @param request
     * @return
     */
    public UserLoginVo login(UserLoginDto userDto, HttpServletRequest request);

    /**
     * 用户退出登录
     * @param request
     */
    void logout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request);
}
