/*
 * Copyright 2019 github.com All right reserved. This software is the
 * confidential and proprietary information of github.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with github.com .
 */
package com.github.acticfox.distributed.limit;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import com.github.acticfox.distributed.common.ExpressionEvaluator;
import com.github.acticfox.distributed.common.RateLimitTemplate;
import com.google.common.base.Throwables;

/**
 * 类RateLimitAspect.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Jun 14, 2019 3:18:32 PM
 */
@Aspect
@Order(1000)
public class RateLimitAspect {

    private static Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    private RateLimitTemplate rateLimitTemplate;

    public RateLimitTemplate getRateLimitTemplate() {
        return rateLimitTemplate;
    }

    public void setRateLimitTemplate(RateLimitTemplate rateLimitTemplate) {
        this.rateLimitTemplate = rateLimitTemplate;
    }
    
    @Pointcut("@annotation(com.github.common.api.idempotence.Idempotent)")
	public void rateLimitPointcut() {
	}
    
    @Around("rateLimitPointcut()&&@annotation(rateLimit)")
    public Object limitBlock(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
    	Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Class<?> aClass = joinPoint.getTarget().getClass();
        String key = evaluator.getValue(joinPoint.getTarget(), joinPoint.getArgs(), aClass, method,
            rateLimit.limitKey());

        String limitKey = aClass.getSimpleName() + "#" + method.getName() + "_" + key;
        rateLimitTemplate.execute(limitKey, rateLimit.limitCount(), rateLimit.timeRange(), rateLimit.blockDuration(),
            rateLimit.timeUnit());
        try {
			return joinPoint.proceed(args);
		} catch (Throwable throwable) {
			log.error("rateLimitTemplate execute failed", throwable);
			Throwables.propagate(throwable);
			return null;
		}
    }

}
