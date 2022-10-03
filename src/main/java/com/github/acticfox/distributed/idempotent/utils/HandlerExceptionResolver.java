/*
 * Copyright 2018 github.com All right reserved. This software is the confidential and proprietary information of
 * github.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with github.com .
 */
package com.github.acticfox.distributed.idempotent.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.github.acticfox.common.api.exception.BusinessException;
import com.github.acticfox.common.api.exception.RetriableException;
import com.github.acticfox.distributed.idempotent.utils.IdempotentResult.ResultStatus;

/**
 * 类RpcHandlerExceptionResolver.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Nov 29, 2018 3:14:46 PM
 */
public class HandlerExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(HandlerExceptionResolver.class);

    public static <P, R> void resolveException(IdempotentResult<P, R> executeResult, Throwable oriResp) {
        if (oriResp instanceof IllegalArgumentException) {
            executeResult.setResultStatus(ResultStatus.UNRECOVERABLE_EXCEPTION_THROWN);
            executeResult.setErrMsg(oriResp.getMessage());
            log.error("idempotent  IllegalArgumentException caught result:{}", JSON.toJSONString(executeResult));
        } else if (oriResp instanceof BusinessException) {
            BusinessException tempEx = (BusinessException)oriResp;
            executeResult.setResultStatus(ResultStatus.UNRECOVERABLE_EXCEPTION_THROWN);
            executeResult.setErrCode(tempEx.getErrCode());
            executeResult.setMedusaCode(tempEx.getMedusaCode());
            executeResult.setErrorArgs(tempEx.getErrorArgs());
            executeResult.setErrMsg(tempEx.getMessage());
            log.error("idempotent  BusinessException caught result:{}", JSON.toJSONString(executeResult));
        } else if (oriResp instanceof RetriableException) {
            RetriableException tempEx = (RetriableException)oriResp;
            executeResult.setResultStatus(ResultStatus.RECOVERABLE_EXCEPTION_THROWN);
            executeResult.setErrCode(tempEx.getErrCode());
            executeResult.setMedusaCode(tempEx.getMedusaCode());
            executeResult.setErrorArgs(tempEx.getErrorArgs());
            executeResult.setErrMsg(tempEx.getMessage());
            log.error("idempotent  RetriableException caught result:{}", JSON.toJSONString(executeResult));
        } else if (oriResp instanceof RuntimeException) {
            executeResult.setResultStatus(ResultStatus.RECOVERABLE_EXCEPTION_THROWN);
            executeResult.setErrMsg(oriResp.getMessage());
            log.error("idempotent  RuntimeException caught result:{}", JSON.toJSONString(executeResult));
        }

    }

}
