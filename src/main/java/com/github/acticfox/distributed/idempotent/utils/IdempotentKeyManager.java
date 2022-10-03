/*
 * Copyright 2018 zhichubao.com All right reserved. This software is the
 * confidential and proprietary information of zhichubao.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with zhichubao.com .
 */
package com.github.acticfox.distributed.idempotent.utils;

/**
 * 类IdempotentConstants.java的实现描述：
 * 
 * <pre>
 * 幂等常量key生成
 * </pre>
 * 
 * @author fanyong.kfy Dec 27, 2018 5:21:44 PM
 */
public class IdempotentKeyManager {

    public static final String IDEM_LOCK_PREFIX   = "idempotenceLock_";

    public static final String IDEM_RESULT_PREFIX = "idemResult_";

    /**
     * 幂等key生成
     * 
     * @param txId
     * @return
     */
    public static String createIdempotentKey(String txId) {
        return IDEM_RESULT_PREFIX + txId;
    }

    /**
     * 幂等分布式锁key生成
     * 
     * @param txId
     * @return
     */
    public static String createIdempotentLockKey(String txId) {
        return IDEM_LOCK_PREFIX + txId;
    }

}
