package com.muyulu.aijavainterviewer.interceptor;

import com.muyulu.aijavainterviewer.common.annotation.RequireLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录认证拦截器
 * 拦截带有 @RequireLogin 注解的方法,验证是否已登录
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("登录拦截器处理请求: {}", requestURI);
        
        // 如果不是处理方法,直接放行
        if (!(handler instanceof HandlerMethod)) {
            log.info("非处理方法,放行");
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireLogin requireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);
        
        // 如果方法上没有 @RequireLogin 注解,直接放行
        if (requireLogin == null) {
            log.info("方法无 @RequireLogin 注解,放行");
            return true;
        }
        
        log.info("方法需要登录认证");
        
        // 检查请求属性中是否有用户ID(由 JWT 过滤器设置)
        Object userId = request.getAttribute("userId");
        log.info("从请求属性获取 userId: {}", userId);
        
        if (userId == null) {
            log.warn("用户未登录或登录已过期");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }
        
        log.info("用户认证通过,userId: {}", userId);
        return true;
    }
}
