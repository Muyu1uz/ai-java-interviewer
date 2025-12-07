package com.muyulu.aijavainterviewer.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * 使用 Redisson RRateLimiter 实现令牌桶限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流器名称（Redis Key）
     * 默认为：方法名
     */
    String name() default "";

    /**
     * 令牌桶容量（最大令牌数）
     */
    long capacity() default 10;

    /**
     * 速率：每秒生成的令牌数
     */
    long rate() default 5;

    /**
     * 限流维度类型
     */
    LimitType limitType() default LimitType.GLOBAL;

    /**
     * 限流失败提示信息
     */
    String message() default "访问过于频繁，请稍后再试";

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 全局限流（所有用户共享）
         */
        GLOBAL,

        /**
         * 用户级别限流（每个用户独立）
         */
        USER,

        /**
         * IP 级别限流（每个 IP 独立）
         */
        IP
    }
}
