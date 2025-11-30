package com.muyulu.aijavainterviewer.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyulu.aijavainterviewer.model.dto.UserDto;
import com.muyulu.aijavainterviewer.model.entity.User;
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
     * @param request
     * @return
     */
    public User login(UserDto request);
}
