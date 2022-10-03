/*
 * Copyright 2019 zhichubao.com All right reserved. This software is the
 * confidential and proprietary information of zhichubao.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with zhichubao.com .
 */
package com.github.acticfox.distributed.limit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 类SmartRateLimit.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Jun 14, 2019 2:33:34 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RateLimit {

    /**
     * 限流KEY
     */
    String limitKey();

    /**
     * 允许访问的次数，默认值MAX_VALUE
     */
    long limitCount() default Long.MAX_VALUE;

    /**
     * 时间段
     */
    long timeRange();

    /**
     * 阻塞时间段
     */
    long blockDuration();

    /**
     * 时间单位，默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
