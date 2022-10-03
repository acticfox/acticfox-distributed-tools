/*
 * Copyright 2018 github.com All right reserved. This software is the
 * confidential and proprietary information of github.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with github.com .
 */
package com.github.acticfox.distributed.idempotent.utils;

import java.lang.reflect.Method;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 类IdempotentExpressionEvaluator.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Dec 19, 2018 2:44:01 PM
 */
public class IdempotentExpressionEvaluator {

    private static ExpressionParser                          parser     = new SpelExpressionParser();

    private static LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

    public static String eval(String spelExpression, Method targetMethod, Object[] args) {
        String[] params = discoverer.getParameterNames(targetMethod);
        EvaluationContext context = new StandardEvaluationContext();
        for (int len = 0; len < params.length; len++) {
            context.setVariable(params[len], args[len]);
        }
        Expression expression = parser.parseExpression(spelExpression);
        return expression.getValue(context, String.class);
    }

}
