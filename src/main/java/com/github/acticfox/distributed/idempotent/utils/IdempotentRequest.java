package com.github.acticfox.distributed.idempotent.utils;

import java.io.Serializable;

/**
 * 类IdempotenceRequest.java的实现描述：
 * 
 * <pre>
 * 幂等请求参数组合封装
 * </pre>
 * 
 * @author fanyong.kfy Dec 18, 2018 10:25:15 AM
 */
public class IdempotentRequest<P> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务id，要求全局唯一
     */
    private String            txId;

    /**
     * 幂等参数
     */
    P                         params;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public P getParams() {
        return params;
    }

    public void setParams(P params) {
        this.params = params;
    }

}
