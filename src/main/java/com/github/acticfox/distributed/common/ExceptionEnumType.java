/*
 * Copyright 2019 github.com All right reserved. This software is the confidential and proprietary information of
 * github.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with github.com .
 */
package com.github.acticfox.distributed.common;

import org.apache.commons.lang.StringUtils;

import com.github.acticfox.common.api.enums.ExceptionCodeEnum;

/**
 * 类ExceptionEnumType.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Jun 3, 2019 10:20:14 AM
 */
public enum ExceptionEnumType implements ExceptionCodeEnum {

    PARAMETER_ILLEGALL("PARAMETER.ILLEGALL", "参数错误", false), ACQUIRE_LOCK_FAIL("ACQUIRE.LOCK.FAIL", "访问频繁，请稍后重试", true);

    private String errCode;

    private String errDesc;
    /**
     * 对应美杜莎的key
     */
    private String enErrCode;

    private boolean retriable;

    ExceptionEnumType(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }

    ExceptionEnumType(String errCode, String errDesc, boolean allowRetry) {
        this.errCode = errCode;
        this.errDesc = errDesc;
        this.retriable = allowRetry;
    }

    ExceptionEnumType(String errCode, String errDesc, String enErrCode) {
        this.errCode = errCode;
        this.errDesc = errDesc;
        this.enErrCode = enErrCode;
    }

    public static ExceptionEnumType valuesOf(String errCode) {
        for (ExceptionEnumType type : ExceptionEnumType.values()) {
            if (type.errCode.equals(errCode)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String getErrCode() {
        return errCode;
    }

    public void moreDesc(String desc) {
        if (StringUtils.isBlank(desc)) {
            return;
        }
        this.errDesc = desc;
    }

    public String getEnErrCode() {
        return enErrCode;
    }

    @Override
    public String getErrMsg() {
        return errDesc;
    }

    @Override
    public boolean isRetriable() {
        return retriable;
    }

    public void setRetriable(boolean retriable) {
        this.retriable = retriable;
    }

}
