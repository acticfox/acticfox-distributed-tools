/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.distributed.idempotent;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import com.github.acticfox.common.api.idempotence.Idempotent;
import com.github.acticfox.distributed.common.IdempotentTemplate;
import com.github.acticfox.distributed.idempotent.utils.IdempotentRequest;
import com.github.acticfox.distributed.idempotent.utils.TxIdUtils;
import com.google.common.base.Throwables;

/**
 * 类ServiceIdempotentAspect.java的实现描述：
 *
 * <pre>
 * 服务幂等处理拦截器
 * </pre>
 *
 * @author fanyong.kfy Dec 17, 2018 7:24:19 PM
 */
@Aspect
@Order(5000)
public class ServiceIdempotentAspect {

    private static Logger log = LoggerFactory.getLogger(ServiceIdempotentAspect.class);

    private IdempotentTemplate<Object[], Object> idempotentTemplate;

    public IdempotentTemplate<Object[], Object> getIdempotentTemplate() {
        return idempotentTemplate;
    }

    public void setIdempotentTemplate(IdempotentTemplate<Object[], Object> idempotentTemplate) {
        this.idempotentTemplate = idempotentTemplate;
    }

    @Pointcut("@annotation(com.zhichubao.common.api.idempotence.Idempotent)")
    public void IdempotentPointcut() {}

    @Around("IdempotentPointcut()&&@annotation(idempotent)")
    public Object providerBlock(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature)signature;
        // 获取到具体执行方法
        Method targetMethod = methodSignature.getMethod();
        Object[] args = pjp.getArgs();
        // 获取幂等txId
        String txId = TxIdUtils.createTxId(idempotent, targetMethod, args);
        if (StringUtils.isBlank(txId)) {
            throw new IllegalArgumentException(targetMethod.getName() + " txId is blank");
        }
        StringBuilder methodFullName = new StringBuilder();
        methodFullName.append(targetMethod.getDeclaringClass().getName()).append(".").append(targetMethod.getName());
        for (Class<?> pt : targetMethod.getParameterTypes()) {
            methodFullName.append("_").append(pt.getName());
        }
        IdempotentRequest<Object[]> request = new IdempotentRequest<Object[]>();
        request.setTxId(methodFullName.append("_").append(txId).toString());
        request.setParams(args);

        return idempotentTemplate.execute(request, () -> {
            try {
                return pjp.proceed(args);
            } catch (Throwable throwable) {
                log.error("idempotentTemplate execute failed", throwable);
                Throwables.propagate(throwable);
                return null;
            }
        }, (idempotentResult) -> {
            idempotentResult.setFirstLevelExpireDate(idempotent.firstLevelExpireDate());
            idempotentResult.setSecondLevelExpireDate(idempotent.secondLevelExpireDate());
        }, null);

    }

}
