package com.muyulu.aijavainterviewer.common.constant;

/**
 * Redis Key 常量接口，统一管理所有 Redis Key
 */
public interface RedisKeyConstant {
    /**
     * 错题本幂等性前缀
     */
    String MISTAKE_BOOK_IDEMPOTENT_PREFIX = "mistake_book:idempotent:";
    
    /**
     * 面试问题池缓存前缀
     */
    String QUESTION_POOL_PREFIX = "question_pool:";
    
    // 可继续补充其他 Redis Key
}
