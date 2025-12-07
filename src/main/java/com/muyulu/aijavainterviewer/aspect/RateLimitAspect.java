package com.muyulu.aijavainterviewer.aspect;

import com.muyulu.aijavainterviewer.common.annotation.RateLimit;
import com.muyulu.aijavainterviewer.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 限流切面
 * 基于 Redisson RRateLimiter 实现令牌桶限流
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RedissonClient redissonClient;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    @Around("@annotation(com.muyulu.aijavainterviewer.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        // 构建限流器 Key
        String key = buildRateLimitKey(rateLimit, method);
        
        // 获取或创建限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        
        // 初始化限流器配置（如果未初始化）
        if (!rateLimiter.isExists()) {
            // 令牌桶算法：capacity 为桶容量，rate 为每秒生成的令牌数
            rateLimiter.trySetRate(
                RateType.OVERALL,      // 全局限流模式
                rateLimit.rate(),      // 速率
                1,                     // 时间间隔
                RateIntervalUnit.SECONDS // 时间单位
            );
            log.info("初始化限流器: key={}, rate={}/s, capacity={}", 
                    key, rateLimit.rate(), rateLimit.capacity());
        }

        // 尝试获取令牌
        boolean acquired = rateLimiter.tryAcquire(1);
        
        if (!acquired) {
            log.warn("限流触发: key={}, message={}", key, rateLimit.message());
            throw new BusinessException(429, rateLimit.message());
        }

        log.debug("限流通过: key={}, 剩余令牌: {}", key, rateLimiter.availablePermits());
        
        // 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 构建限流器 Key
     */
    private String buildRateLimitKey(RateLimit rateLimit, Method method) {
        String name = rateLimit.name();
        if (name.isEmpty()) {
            name = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }

        String suffix = "";
        switch (rateLimit.limitType()) {
            case USER -> {
                // 用户级别限流：从请求属性中获取 userId
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    Object userId = request.getAttribute("userId");
                    suffix = userId != null ? ":user:" + userId : ":anonymous";
                }
            }
            case IP -> {
                // IP 级别限流：获取客户端 IP
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String ip = getClientIp(request);
                    suffix = ":ip:" + ip;
                }
            }
            case GLOBAL -> {
                // 全局限流：不添加后缀
                suffix = ":global";
            }
        }

        return RATE_LIMIT_KEY_PREFIX + name + suffix;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个 IP 的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
