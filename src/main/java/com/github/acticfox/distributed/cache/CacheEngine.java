/*
 * Copyright 2019 github.com All right reserved. This software is the
 * confidential and proprietary information of github.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with github.com .
 */
package com.github.acticfox.distributed.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 类CacheEngine.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Jun 3, 2019 10:07:46 AM
 */
public interface CacheEngine {

    public String get(String key);

    /**
     * 获取指定的key对应的对象,异常也会返回null
     * 
     * @param key
     * @param clazz
     * @return
     */
    public <T> T get(String key, Class<T> clz);

    /**
     * 存储tair缓存数据,忽略过期时间
     * 
     * @param key
     * @param value
     * @return
     */
    public <T extends Serializable> boolean put(String key, T value);

    /**
     * 存储tair缓存数据
     * 
     * @param key
     * @param value
     * @param expiredTime
     * @param unit
     * @return
     */
    public <T extends Serializable> boolean put(String key, T value, int expiredTime, TimeUnit unit);

    /**
     * 基于key删除缓存数据
     * 
     * @param key
     * @return
     */
    public boolean invalid(String key);

    /**
     * 指定过期时间自增计数器，默认每次+1，非滑动窗口
     * 
     * @param key
     * @param expireTime
     * @param unit
     * @return
     */
    public long incrCount(String key, int expireTime, TimeUnit unit);

    /**
     * 指定过期时间自增计数器,单位时间内超过最大值rateThreshold返回true，否则返回false
     * 
     * @param key
     * @param rateThreshold
     * @param expireTime
     * @param unit
     * @return
     */
    public boolean rateLimit(final String key, final int rateThreshold, int expireTime, TimeUnit unit);

}
