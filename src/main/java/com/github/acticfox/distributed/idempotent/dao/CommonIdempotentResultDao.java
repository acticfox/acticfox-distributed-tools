/*
 * Copyright 2018 github.com All right reserved. This software is the
 * confidential and proprietary information of github.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with github.com .
 */
package com.github.acticfox.distributed.idempotent.dao;

import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.acticfox.distributed.cache.CacheEngine;
import com.github.acticfox.distributed.idempotent.utils.IdempotentConstants;
import com.github.acticfox.distributed.idempotent.utils.IdempotentKeyManager;
import com.github.acticfox.distributed.idempotent.utils.IdempotentResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类TairIdempotentResultStore.java的实现描述：TODO 类实现描述
 *
 * @author fanyong.kfy Dec 18, 2018 12:03:51 PM
 */
public class CommonIdempotentResultDao implements IdempotentResultDao {

    private static Logger log = LoggerFactory.getLogger(CommonIdempotentResultDao.class);

    private CacheEngine cacheEngine;

    private int resultExpireDays = IdempotentConstants.DEFAULT_EXPIRE_DATE;

    private static int CUSTOM_EXPIRED_TIME_THRESHOLD = 0;

    public int getResultExpireDays() {
        return resultExpireDays;
    }

    public void setResultExpireDays(int resultExpireDays) {
        this.resultExpireDays = resultExpireDays;
    }
    // static {
    // ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    // }

    public CacheEngine getCacheEngine() {
        return cacheEngine;
    }

    public void setCacheEngine(CacheEngine cacheEngine) {
        this.cacheEngine = cacheEngine;
    }

    @Override
    public <P, R> void saveResult(IdempotentResult<P, R> result, int expiredTime, TimeUnit unit) {
        String key = IdempotentKeyManager.createIdempotentKey(result.getRequest().getTxId());
        String jsonResult = JSON.toJSONString(result, SerializerFeature.WriteClassName);
        int tempExpiredTime = this.resultExpireDays;
        TimeUnit tempExpiredTimeUnit = TimeUnit.DAYS;
        if (expiredTime > CUSTOM_EXPIRED_TIME_THRESHOLD) {
            tempExpiredTime = expiredTime;
            tempExpiredTimeUnit = unit;
        }
        boolean flag = cacheEngine.put(key, jsonResult, tempExpiredTime, tempExpiredTimeUnit);
        if (!flag) {
            log.info("Tair save IdempotentResult failed txId:{},result:{}", result.getRequest().getTxId(),
                JSON.toJSONString(result));
        }
    }

    @Override
    public <P, R> IdempotentResult<P, R> fetchResult(String txId) {
        String key = IdempotentKeyManager.createIdempotentKey(txId);
        String jsonResult = cacheEngine.get(key);
        IdempotentResult<P, R> result = JSON.parseObject(jsonResult, IdempotentResult.class, Feature.IgnoreNotMatch);

        return result;
    }

}
