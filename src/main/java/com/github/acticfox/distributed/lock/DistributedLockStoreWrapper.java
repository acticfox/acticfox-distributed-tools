package com.github.acticfox.distributed.lock;

/**
 * 类的实现描述：包装类
 *
 * @author fanyong.kfy 2018-03-07 10:03:05
 */
public class DistributedLockStoreWrapper implements DistributedLockStore {
    private DistributedLockStore distributedLockStore;

    public DistributedLockStoreWrapper(DistributedLockStore distributedLockStore) {
        this.distributedLockStore = distributedLockStore;
    }

    @Override
    public InvokeResult<Boolean> lock(String resource, String lockToken, int expireTimeInSecond) {
        try {
            return distributedLockStore.lock(resource, lockToken, expireTimeInSecond);
        } catch (Exception ex) {
            return new InvokeResult<Boolean>(false);
        }
    }

    @Override
    public InvokeResult<Boolean> unlock(String resource, String lockToken) {
        try {
            return distributedLockStore.unlock(resource, lockToken);
        } catch (Exception ex) {
            return new InvokeResult<Boolean>(false);
        }
    }

    @Override
    public InvokeResult updateLockExpireTime(String resource, String lockToken, int expireTimeInSecond) {
        try {
            return distributedLockStore.updateLockExpireTime(resource, lockToken, expireTimeInSecond);
        } catch (Exception ex) {
            return new InvokeResult<Boolean>(false);
        }
    }
}