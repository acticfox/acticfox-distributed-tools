/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.distributed.idempotent.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.acticfox.common.tools.thredpool.ThreadPoolUtil;
import com.github.acticfox.common.tools.thredpool.WaitingEnqueuePolicy;
import com.github.acticfox.distributed.idempotent.dao.IdempotentResultDao;
import com.github.acticfox.distributed.idempotent.utils.IdempotentResult;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 类IdempotentService.java的实现描述：TODO 类实现描述
 *
 * @author fanyong.kfy Dec 18, 2018 2:15:13 PM
 */
public class IdempotentService<P, R> {

    private static Logger log = LoggerFactory.getLogger(IdempotentService.class);

    private IdempotentResultDao firstLevelIdempotentResultDao;

    private IdempotentResultDao secondLevelIdempotentResultDao;

    private ThreadFactory threadFactory =
        new ThreadFactoryBuilder().setNameFormat("IdempotentServiceThreadPool-thread-%d").build();

    private ExecutorService executorService = new ThreadPoolExecutor(50, 50, 0L, TimeUnit.MILLISECONDS,
        new SynchronousQueue<Runnable>(true), threadFactory, new WaitingEnqueuePolicy());

    public IdempotentResultDao getFirstLevelIdempotentResultDao() {
        return firstLevelIdempotentResultDao;
    }

    public void setFirstLevelIdempotentResultDao(IdempotentResultDao firstLevelIdempotentResultDao) {
        this.firstLevelIdempotentResultDao = firstLevelIdempotentResultDao;
    }

    public IdempotentResultDao getSecondLevelIdempotentResultDao() {
        return secondLevelIdempotentResultDao;
    }

    public void setSecondLevelIdempotentResultDao(IdempotentResultDao secondLevelIdempotentResultDao) {
        this.secondLevelIdempotentResultDao = secondLevelIdempotentResultDao;
    }

    public void saveResult(IdempotentResult<P, R> result) {
        try {
            int firstLevelExpireDate = result.getFirstLevelExpireDate();
            firstLevelIdempotentResultDao.saveResult(result, firstLevelExpireDate, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("save firstLevel idempotentResult failed txId:{},result:{}", result.getRequest().getTxId(),
                JSON.toJSONString(result), e);
        }
        if (secondLevelIdempotentResultDao != null) {
            executorService.execute(() -> {
                try {
                    int secondLevelExpireDate = result.getSecondLevelExpireDate();
                    secondLevelIdempotentResultDao.saveResult(result, secondLevelExpireDate, TimeUnit.DAYS);
                } catch (Exception e) {
                    log.error("save secondLevel idempotentResult failed txId:{},result:{}",
                        result.getRequest().getTxId(), JSON.toJSONString(result), e);
                }
            });
        }
    }

    public IdempotentResult<P, R> fetchResult(String txId) {
        List<Callable<IdempotentResult<P, R>>> callableList = new ArrayList<>();
        callableList.add(() -> {
            try {
                return firstLevelIdempotentResultDao.fetchResult(txId);
            } catch (Exception ex) {
                log.error("fetchResult  firstLevel idempotentResult  failed txId:{}", txId, ex);
            }
            return null;
        });
        if (secondLevelIdempotentResultDao != null) {
            callableList.add(() -> {
                try {
                    return secondLevelIdempotentResultDao.fetchResult(txId);
                } catch (Exception ex) {
                    log.error("fetchResult  secondLevel idempotentResult  failed txId:{}", txId, ex);
                }
                return null;
            });
        }
        return ThreadPoolUtil.fetchFirstNonnullResult(callableList, executorService);
    }

}
