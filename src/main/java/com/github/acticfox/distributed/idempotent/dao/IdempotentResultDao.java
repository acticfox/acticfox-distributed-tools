/*
 * Copyright 2018 github.com All right reserved. This software is the
 * confidential and proprietary information of github.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with github.com .
 */
package com.github.acticfox.distributed.idempotent.dao;

import java.util.concurrent.TimeUnit;

import com.github.acticfox.distributed.idempotent.utils.IdempotentResult;

/**
 * 类IdempotentResultStore.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Dec 18, 2018 12:00:40 PM
 */
public interface IdempotentResultDao {

    /**
     * 幂等结果存储
     * 
     * @param result
     * @param expiredTime
     * @param unit
     */
    <P, R> void saveResult(IdempotentResult<P, R> result, int expiredTime, TimeUnit unit);

    /**
     * 幂等结果查询
     * 
     * @param txId
     * @return
     */
    <P, R> IdempotentResult<P, R> fetchResult(String txId);

}
