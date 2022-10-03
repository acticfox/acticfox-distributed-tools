/*
 * Copyright 2019 zhichubao.com All right reserved. This software is the
 * confidential and proprietary information of zhichubao.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with zhichubao.com .
 */
package com.github.acticfox.distributed.lock;

/**
 * 类InvokeResult.java的实现描述：TODO 类实现描述
 * 
 * @author fanyong.kfy Jun 3, 2019 10:12:30 AM
 */
public class InvokeResult<T> {

    private boolean success = false;

    private T       result;

    public InvokeResult() {
    }

    public InvokeResult(boolean success, T result) {
        this.success = success;
        this.result = result;
    }

    public InvokeResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public static <T> InvokeResult<T> newInvokeResult(boolean success, T result) {
        return new InvokeResult<T>(success, result);
    }

}
