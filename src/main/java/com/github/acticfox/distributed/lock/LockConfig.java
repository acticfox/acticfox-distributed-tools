package com.github.acticfox.distributed.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类的实现描述：TODO 类实现描述
 *
 * @author fanyong.kfy 2018-03-07 17:57:45
 */
public class LockConfig {

    private DistributedLockStore distributedLockStore;

    public DistributedLockStore getDistributedLockStore() {
        return distributedLockStore;
    }

    public void setDistributedLockStore(DistributedLockStore distributedLockStore) {
        this.distributedLockStore = distributedLockStore;
    }

    public void init() {
        DistributedReentrantLock.setDistributedLockStore(distributedLockStore);
        DistributedReentrantLock.start();
    }

}
