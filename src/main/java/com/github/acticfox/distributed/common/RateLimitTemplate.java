/*
 * Copyright 2019 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.distributed.common;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;

import com.github.acticfox.common.api.enums.ErrCodeEnum;
import com.github.acticfox.common.api.util.AssertUtils;
import com.github.acticfox.distributed.cache.CacheEngine;

/**
 * 类RateLimitTemplate.java的实现描述：
 * 
 * <pre>
 * 限流处理模板
 * </pre>
 * 
 * @author fanyong.kfy Jun 14, 2019 2:56:09 PM
 */
public class RateLimitTemplate {

    private CacheEngine cacheEngine;

    public CacheEngine getCacheEngine() {
        return cacheEngine;
    }

    public void setCacheEngine(CacheEngine cacheEngine) {
        this.cacheEngine = cacheEngine;
    }

    public <T> T execute(String limitKey, Supplier<T> resultSupplier, long rateThreshold, long limitTime, TimeUnit unit,
        ErrCodeEnum errCodeEnum) {

        return execute(limitKey, resultSupplier, rateThreshold, limitTime, -1, unit, errCodeEnum);
    }

    /**
     * @param limitKey 限流KEY
     * @param resultSupplier 回调方法
     * @param rateThreshold 限流阈值
     * @param limitTime 限制时间段
     * @param blockDuration 阻塞时间段
     * @param unit 时间单位
     * @param errCodeEnum 指定限流错误码
     * @return
     */
    public <T> T execute(String limitKey, Supplier<T> resultSupplier, long rateThreshold, long limitTime,
        long blockDuration, TimeUnit unit, ErrCodeEnum errCodeEnum) {
        boolean blocked = tryAcquire(limitKey, rateThreshold, limitTime, blockDuration, unit);
        if (errCodeEnum != null) {
            AssertUtils.assertTrue(blocked, errCodeEnum);
        } else {
            AssertUtils.assertTrue(blocked, ExceptionEnumType.ACQUIRE_LOCK_FAIL);
        }

        return resultSupplier.get();
    }

    public void execute(String limitKey, long rateThreshold, long limitTime, TimeUnit unit, ErrCodeEnum errCodeEnum) {
        execute(limitKey, rateThreshold, limitTime, -1, unit, errCodeEnum);
    }

    public void execute(String limitKey, long rateThreshold, long limitTime, long blockDuration, TimeUnit unit,
        ErrCodeEnum errCodeEnum) {
        boolean blocked = tryAcquire(limitKey, rateThreshold, limitTime, blockDuration, unit);
        if (errCodeEnum != null) {
            AssertUtils.assertTrue(blocked, errCodeEnum);
        } else {
            AssertUtils.assertTrue(blocked, ExceptionEnumType.ACQUIRE_LOCK_FAIL);
        }
    }

    public void execute(String limitKey, long rateThreshold, long limitTime, TimeUnit unit) {
        execute(limitKey, rateThreshold, limitTime, -1, unit);
    }

    public void execute(String limitKey, long rateThreshold, long limitTime, long blockDuration, TimeUnit unit) {
        boolean blocked = tryAcquire(limitKey, rateThreshold, limitTime, blockDuration, unit);
        AssertUtils.assertTrue(blocked, ExceptionEnumType.ACQUIRE_LOCK_FAIL);
    }

    public boolean tryAcquire(String limitKey, long rateThreshold, long limitTime, TimeUnit unit) {
        return tryAcquire(limitKey, rateThreshold, limitTime, -1, unit);
    }

    public boolean tryAcquire(String limitKey, long rateThreshold, long limitTime, long blockDuration, TimeUnit unit) {
        limitKey = genLimitKey(limitKey, unit);
        String blockKey = genBlockKey(limitKey, unit);
        String blockValue = cacheEngine.get(blockKey);
        if (StringUtils.isNotBlank(blockValue)) {
            return false;
        }

        boolean blocked = cacheEngine.rateLimit(limitKey, (int)rateThreshold, (int)limitTime, unit);
        if (blocked) {
            if (blockDuration > 0) {
                cacheEngine.put(blockKey, blockKey, (int)blockDuration, unit);
            }

            return false;
        }

        return true;
    }

    protected String genBlockKey(String key, TimeUnit unit) {
        return String.format("block_%s_%s", key, unit.name());
    }

    protected String genLimitKey(String key, TimeUnit unit) {
        return String.format("rate_%s_%s", key, unit.name());
    }

}
