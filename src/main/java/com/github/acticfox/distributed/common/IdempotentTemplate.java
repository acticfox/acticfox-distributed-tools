/*
 * Copyright 2018 github.com All right reserved. This software is the confidential and proprietary information of
 * github.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with github.com .
 */
package com.github.acticfox.distributed.common;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.acticfox.common.api.exception.BusinessException;
import com.github.acticfox.common.api.result.ResultDTO;
import com.github.acticfox.distributed.idempotent.service.IdempotentService;
import com.github.acticfox.distributed.idempotent.utils.HandlerExceptionResolver;
import com.github.acticfox.distributed.idempotent.utils.IdempotentKeyManager;
import com.github.acticfox.distributed.idempotent.utils.IdempotentRequest;
import com.github.acticfox.distributed.idempotent.utils.IdempotentResult;
import com.github.acticfox.distributed.idempotent.utils.IdempotentResult.ResultStatus;

/**
 * 类IdempotentExecuteTemplate.java的实现描述：TODO 类实现描述
 *
 * <pre>
 * 幂等处理执行器
 * </pre>
 *
 * @author fanyong.kfy Dec 17, 2018 9:00:29 PM
 */
public class IdempotentTemplate<P, R> {

    private static Logger log = LoggerFactory.getLogger(IdempotentTemplate.class);

    private IdempotentService<P, R> idempotentService;

    public IdempotentService<P, R> getIdempotentService() {
        return idempotentService;
    }

    public void setIdempotentService(IdempotentService<P, R> idempotentService) {
        this.idempotentService = idempotentService;
    }

    /**
     * 幂等模板处理器
     *
     * @param request 幂等Request信息
     * @param executeSupplier 幂等处理回调function
     * @param resultPreprocessConsumer 幂等结果回调function 可以对结果做些预处理
     * @param ifResultNeedIdempotence 除了根据异常还需要根据结果判定是否需要幂等性的场景可以提供此参数
     * @return
     */
    public R execute(IdempotentRequest<P> request, Supplier<R> executeSupplier,
        Consumer<IdempotentResult<P, R>> resultPreprocessConsumer, Predicate<R> ifResultNeedIdempotence) {

        return DistributedLockTemplate.execute(IdempotentKeyManager.createIdempotentLockKey(request.getTxId()), () -> {
            IdempotentResult<P, R> idempotenceResult = idempotentService.fetchResult(request.getTxId());
            if (idempotenceResult != null) {
                // 有成功的结果直接返回
                if (IdempotentResult.ResultStatus.SUCCESS.equals(idempotenceResult.getResultStatus())) {
                    log.info("IdempotentTemplate execute txId:{} return previous Result:{}", request.getTxId(),
                        JSON.toJSONString(idempotenceResult, SerializerFeature.WriteClassName));
                    return idempotenceResult.getResult();
                    // 不可恢复性异常直接失败
                } else if (ResultStatus.UNRECOVERABLE_EXCEPTION_THROWN.equals(idempotenceResult.getResultStatus())) {
                    log.info("IdempotentTemplate execute txId:{} throw previous Exception:{}", request.getTxId(),
                        JSON.toJSONString(idempotenceResult, SerializerFeature.WriteClassName));
                    throw new BusinessException(idempotenceResult.getErrCode(), idempotenceResult.getMedusaCode(),
                        idempotenceResult.getErrMsg(), idempotenceResult.getErrorArgs());
                }
            }
            IdempotentResult<P, R> executeResult = new IdempotentResult<P, R>();
            executeResult.setRequest(request);
            try {
                R result = executeSupplier.get();
                executeResult.setResultStatus(ResultStatus.SUCCESS);
                executeResult.setResult(result);
                if (resultPreprocessConsumer != null) {
                    resultPreprocessConsumer.accept(executeResult);
                }

                boolean ifNeedIdempotence;

                if (ifResultNeedIdempotence != null) {
                    ifNeedIdempotence = ifResultNeedIdempotence.test(result);
                } else {
                    if (result instanceof ResultDTO) {
                        ifNeedIdempotence = ((ResultDTO)result).isSuccess()
                            || (!((ResultDTO)result).isSuccess() && !((ResultDTO)result).isRetriable());
                    } else {
                        ifNeedIdempotence = true;
                    }
                }

                if (ifNeedIdempotence) {
                    idempotentService.saveResult(executeResult);
                }
                return result;
            } catch (Throwable ex) {
                HandlerExceptionResolver.resolveException(executeResult, ex);
                idempotentService.saveResult(executeResult);
                throw ex;
            }
        });
    }

    public R execute(IdempotentRequest<P> request, Supplier<R> executeSupplier) {
        return execute(request, executeSupplier, null, null);
    }

    public R execute(IdempotentRequest<P> request, Supplier<R> executeSupplier, Predicate<R> ifResultNeedIdempotence) {
        return execute(request, executeSupplier, null, ifResultNeedIdempotence);
    }

}
