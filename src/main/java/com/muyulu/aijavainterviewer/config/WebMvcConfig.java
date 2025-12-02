package com.muyulu.aijavainterviewer.config;

import com.muyulu.aijavainterviewer.interceptor.LoginInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        "/api/user/login",      // 登录接口不拦截
                        "/api/user/register",   // 注册接口不拦截
                        "/swagger-ui/**",       // Swagger 文档不拦截
                        "/v3/api-docs/**",      // API 文档不拦截
                        "/doc.html",            // Knife4j 文档不拦截
                        "/swagger-ui.html"      // Swagger UI 不拦截
                );
    }
}
