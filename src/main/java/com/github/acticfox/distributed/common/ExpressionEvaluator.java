/*
 * Copyright (c) 2019 zhichubao.com All right reserved. This software is the
 * confidential and proprietary information of zhichubao.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with zhichubao.com .
 *
 */

package com.github.acticfox.distributed.common;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

public class ExpressionEvaluator extends CachedExpressionEvaluator {

    private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>(64);

    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

    /**
     * Create the suitable {@link EvaluationContext} for the specified event handling
     * on the specified method.
     */
    public EvaluationContext createEvaluationContext(Object object, Class<?> targetClass, Method method, Object[] args) {

        Method targetMethod = getTargetMethod(targetClass, method);
        ExpressionRootObject root = new ExpressionRootObject(object, args);
        return new MethodBasedEvaluationContext(root, targetMethod, args, this.paramNameDiscoverer);
    }

    /**
     * Specify if the condition defined by the specified expression matches.
     */
    public String condition(String conditionExpression, AnnotatedElementKey elementKey, EvaluationContext evalContext) {
        return getExpression(this.conditionCache, elementKey, conditionExpression).getValue(evalContext, String.class);
    }

    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        Method targetMethod = this.targetMethodCache.get(methodKey);
        if (targetMethod == null) {
            targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            if (targetMethod == null) {
                targetMethod = method;
            }
            this.targetMethodCache.put(methodKey, targetMethod);
        }
        return targetMethod;
    }


    public String getValue(Object object, Object[] args, Class clazz, Method method,
                            String condition) {
        if (args == null) {
            return null;
        }

        EvaluationContext evaluationContext = this.createEvaluationContext(object, clazz, method, args);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
        return this.condition(condition, methodKey, evaluationContext);
    }

  static   class ExpressionRootObject {
        private final Object object;

        private final Object[] args;

        public ExpressionRootObject(Object object, Object[] args) {
            this.object = object;
            this.args = args;
        }

        public Object getObject() {
            return object;
        }

        public Object[] getArgs() {
            return args;
        }
    }
}

