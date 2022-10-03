/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.distributed.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;

import com.github.acticfox.common.api.enums.ErrCodeEnum;
import com.github.acticfox.common.api.util.AssertUtils;
import com.github.acticfox.distributed.lock.DistributedReentrantLock;

/**
 * 类DistributedLockTemplate.java的实现描述：
 * 
 * <pre>
 * 抽象分布式锁处理模板，避免应用中出现重复代码
 * </pre>
 * 
 * @author fanyong.kfy Dec 13, 2018 11:04:47 AM
 */
public class DistributedLockTemplate {

    /**
     * 分布式锁处理模板执行器
     * 
     * @param lockKey 分布式锁key
     * @param resultSupplier 分布式锁处理回调
     * @param waitTime 锁等待时间
     * @param unit 时间单位
     * @param errCodeEnum 指定特殊错误码返回
     * @return
     */
    public static <T> T execute(String lockKey, Supplier<T> resultSupplier, long waitTime, TimeUnit unit,
        ErrCodeEnum errCodeEnum) {
        AssertUtils.assertTrue(StringUtils.isNotBlank(lockKey), ExceptionEnumType.PARAMETER_ILLEGALL);
        boolean locked = false;
        Lock lock = DistributedReentrantLock.newLock(lockKey);
        try {
            locked = waitTime > 0 ? lock.tryLock(waitTime, unit) : lock.tryLock();
        } catch (InterruptedException e) {
            throw new RuntimeException(String.format("lock error,lockResource:%s", lockKey), e);
        }
        if (errCodeEnum != null) {
            AssertUtils.assertTrue(locked, errCodeEnum);
        } else {
            AssertUtils.assertTrue(locked, ExceptionEnumType.ACQUIRE_LOCK_FAIL);
        }
        try {
            return resultSupplier.get();
        } finally {
            lock.unlock();
        }
    }

    public static <T> T execute(String lockKey, Supplier<T> resultSupplier, long waitTime, TimeUnit unit) {
        return execute(lockKey, resultSupplier, waitTime, unit, null);
    }

    public static <T> T execute(String lockKey, Supplier<T> resultSupplier, ErrCodeEnum errCodeEnum) {
        return execute(lockKey, resultSupplier, -1, null, errCodeEnum);
    }

    public static <T> T execute(String lockKey, Supplier<T> resultSupplier) {
        return execute(lockKey, resultSupplier, -1, null, null);
    }

}
