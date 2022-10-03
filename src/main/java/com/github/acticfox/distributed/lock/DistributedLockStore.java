/*
 * Copyright 2019 zhichubao.com All right reserved. This software is the
 * confidential and proprietary information of zhichubao.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with zhichubao.com .
 */
package com.github.acticfox.distributed.lock;

/**
 * 类DistributedLockStore.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Jun 3, 2019 10:11:45 AM
 */
public interface DistributedLockStore {

    /**
     * 加锁
     *
     * @param resource 待加锁的目标资源
     * @param lockToken 唯一锁定标识
     * @param expireTimeInSecond 锁过期时间，单位:秒
     * @return
     */
    InvokeResult<Boolean> lock(String resource, String lockToken, int expireTimeInSecond);

    /**
     * 解锁
     *
     * @param resource 待加锁的目标资源
     * @param lockToken 唯一锁定标识
     * @return
     */
    InvokeResult<Boolean> unlock(String resource, String lockToken);

    /**
     * 更新锁过期时间
     *
     * @param resource
     * @param lockToken 唯一锁定标识
     * @param expireTimeInSecond 锁过期时间，单位:秒
     * @return
     */
    InvokeResult<Boolean> updateLockExpireTime(String resource, String lockToken, int expireTimeInSecond);

}
