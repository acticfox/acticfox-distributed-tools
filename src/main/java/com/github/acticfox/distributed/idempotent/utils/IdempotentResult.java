package com.github.acticfox.distributed.idempotent.utils;

import java.io.Serializable;

/**
 * 类的实现描述：TODO 类实现描述
 * 
 * <pre>
 * 幂等结果组合封装
 * </pre>
 * 
 * @author fanyong.kfy 2018-06-08 10:02:18
 */
public class IdempotentResult<P, R> implements Serializable {

    private static final long    serialVersionUID = 8625770253026450536L;

    private IdempotentRequest<P> request;

    private String               resultStatus;

    private R                    result;

    /**
     * 异常代码
     */
    private String               errCode;

    private String               errMsg;
    /**
     * 美杜莎KEY
     */
    private String               medusaCode;

    /**
     * 错误消息替换字符串数组
     */
    private Object[]             errorArgs;

    /**
     * 一级存储幂等有效期
     */
    private int              firstLevelExpireDate;

    /**
     * 二级存储幂等有效期
     */
    private int              secondLevelExpireDate;

    public static class ResultStatus {
        public static final String SUCCESS                        = "success";
        public static final String RECOVERABLE_EXCEPTION_THROWN   = "recoverableExceptionThrown";
        public static final String UNRECOVERABLE_EXCEPTION_THROWN = "unrecoverableExceptionThrown";
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getMedusaCode() {
        return medusaCode;
    }

    public void setMedusaCode(String medusaCode) {
        this.medusaCode = medusaCode;
    }

    public Object[] getErrorArgs() {
        return errorArgs;
    }

    public void setErrorArgs(Object[] errorArgs) {
        this.errorArgs = errorArgs;
    }

    public IdempotentRequest<P> getRequest() {
        return request;
    }

    public void setRequest(IdempotentRequest<P> request) {
        this.request = request;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
        this.result = result;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public int getFirstLevelExpireDate() {
        return firstLevelExpireDate;
    }

    public void setFirstLevelExpireDate(int firstLevelExpireDate) {
        this.firstLevelExpireDate = firstLevelExpireDate;
    }

    public int getSecondLevelExpireDate() {
        return secondLevelExpireDate;
    }

    public void setSecondLevelExpireDate(int secondLevelExpireDate) {
        this.secondLevelExpireDate = secondLevelExpireDate;
    }
}
