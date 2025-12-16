package com.muyulu.aijavainterviewer.filter;

import com.muyulu.aijavainterviewer.common.util.JwtUtil;
import com.muyulu.aijavainterviewer.service.impl.UserServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private JwtUtil jwtUtil;
    
    @Lazy
    @Resource
    private UserServiceImpl userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        log.info("JWT过滤器处理请求: {}", requestURI);
        
        // 跳过 Swagger 相关路径
        if (shouldNotFilter(requestURI)) {
            log.info("跳过JWT验证的路径: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        // 从请求头中提取 Token
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token)) {
            log.info("检测到 Token: {}", token.substring(0, Math.min(20, token.length())) + "...");
            
            // 检查 Token 是否在黑名单中
            if (userService.isTokenBlacklisted(token)) {
                log.warn("Token 已被加入黑名单（用户已退出登录）");
                filterChain.doFilter(request, response);
                return;
            }
            
            if (jwtUtil.validateToken(token)) {
                // Token 有效,将用户信息存入请求属性
                try {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);
                    
                    // 将用户信息存入请求属性,供后续业务层使用
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                    log.info("Token验证成功,用户ID: {}, 用户名: {}", userId, username);
                } catch (Exception e) {
                    log.error("解析 JWT Token 失败", e);
                }
            } else {
                log.warn("Token验证失败");
            }
        } else {
            log.info("未检测到 Authorization 头或 Token");
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
    
    /**
     * 判断是否应该跳过JWT验证
     */
    private boolean shouldNotFilter(String requestURI) {
        // Swagger 相关路径
        return requestURI.startsWith("/api/v3/api-docs") ||
               requestURI.startsWith("/api/swagger-ui") ||
               requestURI.equals("/api/swagger-ui.html") ||
               requestURI.equals("/api/doc.html") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.equals("/swagger-ui.html") ||
               requestURI.equals("/doc.html") ||
               requestURI.startsWith("/swagger-resources") ||
               requestURI.startsWith("/webjars/");
    }
}
