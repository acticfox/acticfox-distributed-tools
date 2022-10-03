/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the confidential and proprietary information of
 * zhichubao.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with zhichubao.com .
 */
package com.github.acticfox.distributed.idempotent.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;

import com.github.acticfox.common.api.idempotence.Idempotent;
import com.github.acticfox.common.api.idempotence.IdempotentTxId;
import com.github.acticfox.common.api.idempotence.IdempotentTxIdGetter;

/**
 * 类TxIdUtils.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Dec 19, 2018 2:58:02 PM
 */
public class TxIdUtils {

    public static String createTxId(Idempotent idempotent, Method targetMethod, Object[] args) throws Exception {
        String txId = "";
        String idempotentKey = idempotent.spelKey();
        if (StringUtils.isNotBlank(idempotentKey)) {
            txId = IdempotentExpressionEvaluator.eval(idempotentKey, targetMethod, args);
        }
        if (StringUtils.isNotBlank(txId)) {
            return txId;
        }
        // 首先获取参数上是否有注解@IdempotentTxId
        Annotation[][] parameterAnnotations = targetMethod.getParameterAnnotations();
        for (int n = 0; n < parameterAnnotations.length; n++) {
            boolean idempotentTxIdFound = false;
            for (Annotation annotation : parameterAnnotations[n]) {
                if (annotation instanceof IdempotentTxId) {
                    Object tempArg = args[n];
                    txId = tempArg == null ? "" : String.valueOf(tempArg);
                    idempotentTxIdFound = true;
                    break;
                }
            }
            if (idempotentTxIdFound) {
                break;
            }
        }
        if (StringUtils.isNotBlank(txId)) {
            return txId;
        }

        // 如果参数没有注解@IdempotentTxId，获取参数对象方法是否有注解@IdempotentTxIdGetter
        IdempotentTxIdGetter idempotentTxIdGetter = null;
        Method idempotentTxIdGetterMethod = null;
        Object argObject = null;
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            Field[] fields = arg.getClass().getDeclaredFields();
            for (Field field : fields) {
                IdempotentTxId idempotentTxId = field.getAnnotation(IdempotentTxId.class);
                field.setAccessible(true);
                if (idempotentTxId != null && field.get(arg) != null) {
                    txId = field.get(arg).toString();
                    break;
                }
            }
            if (StringUtils.isNotBlank(txId)) {
                break;
            }
            Method[] methods = arg.getClass().getDeclaredMethods();
            for (Method m : methods) {
                idempotentTxIdGetter = m.getAnnotation(IdempotentTxIdGetter.class);
                if (idempotentTxIdGetter != null) {
                    idempotentTxIdGetterMethod = m;
                    break;
                }
            }
            if (idempotentTxIdGetter != null) {
                argObject = arg;
                break;
            }
        }
        if (idempotentTxIdGetter != null && idempotentTxIdGetterMethod != null) {
            Object getterValue = idempotentTxIdGetterMethod.invoke(argObject);
            txId = getterValue == null ? "" : String.valueOf(getterValue);
        }

        return txId;

    }

}
