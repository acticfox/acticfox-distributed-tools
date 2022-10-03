package com.github.acticfox.distributed.lock;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类的实现描述：分布式可重入锁，封装了心跳及可重入逻辑
 *
 * @author fanyong.kfy 2018-03-06 10:22:42
 */
public class DistributedReentrantLock {

    private static final Logger log = LoggerFactory.getLogger(DistributedReentrantLock.class);

    private static DistributedLockStore distributedLockStore;

    private final static int LOCK_EXPIRE_TIME_SECONDS = 180;

    private final static int MAX_TRYING_LOCK_TIME_MILLIS = 1000 * 60*5;

    private static Map<String, LockHolder> lockHolders = new ConcurrentHashMap<String, LockHolder>();

    private static ReentrantLock startHeartbeatLock = new ReentrantLock();

    private static boolean heartbeatThreadStarted = false;

    private DistributedReentrantLock() {
    }

    public static DistributedLockStore getDistributedLockStore() {
        return distributedLockStore;
    }

    static void setDistributedLockStore(DistributedLockStore distributedLockStoreParam) {
        distributedLockStore = new DistributedLockStoreWrapper(distributedLockStoreParam);
    }

    public static void start() {
        startHeartbeatThread();
    }

    private static void startHeartbeatThread() {
        startHeartbeatLock.lock();
        try {
            if (heartbeatThreadStarted) {
                return;
            }

            Thread heartbeatThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            LockSupport.parkUntil(System.currentTimeMillis() + 10000);

                            for (Entry<String, LockHolder> entry : lockHolders.entrySet()) {
                                InvokeResult<Boolean> updateResult = updateHeartbeatTime(entry.getKey(),
                                    entry.getValue().getLockToken());
                                log.info("DistributedReentrantLock heartbeat updateResult success:{},result:{}",
                                    updateResult.isSuccess(), updateResult.getResult());
                                if (updateResult.isSuccess() && updateResult.getResult()) {
                                    entry.getValue().setLastHeartbeatTimeMillis(System.currentTimeMillis());
                                }
                            }

                            for (Entry<String, LockHolder> entry : lockHolders.entrySet()) {
                                if ((System.currentTimeMillis() - entry.getValue().getLastHeartbeatTimeMillis())
                                    > (LOCK_EXPIRE_TIME_SECONDS * 1000)) {
                                    lockHolders.remove(entry.getKey());
                                    log.info("DistributedReentrantLock heartbeat lock expired {}", entry.getKey());
                                }
                            }
                        } catch (Exception ex) {
                            log.error("heartbeatThread error", ex);
                        }
                    }
                }
            };
            heartbeatThread.setDaemon(true);
            heartbeatThread.start();

            heartbeatThreadStarted = true;

        } finally {
            startHeartbeatLock.unlock();
        }
    }

    private static InvokeResult<Boolean> updateHeartbeatTime(String resource, String lockToken) {
        return distributedLockStore.updateLockExpireTime(resource, lockToken, LOCK_EXPIRE_TIME_SECONDS);
    }

    /**
     * 默认支持同一线程重入，如果加锁线程与业务处理的线程及解锁线程不在同一个线程，建议不要使用重入特性
     *
     * @param resource 待加锁的资源
     * @return Lock
     */
    public static Lock newLock(String resource) {
        return new InnerDistributedReentrantLock(resource, distributedLockStore);
    }

    /**
     * 如果加锁线程与业务处理的线程及解锁线程不在同一个线程，建议不要使用重入特性
     *
     * @param resource      待加锁的资源
     * @param needReentrant 是否需要同一线程重入特性
     * @return
     */
    public static Lock newLock(String resource, boolean needReentrant) {
        return new InnerDistributedReentrantLock(resource, distributedLockStore, needReentrant);
    }

    private static class InnerDistributedReentrantLock implements Lock {
        private DistributedLockStore distributedLockStore;
        private String resource;
        private boolean needReentrant = true;
        private String localLockToken;

        InnerDistributedReentrantLock(String resource, DistributedLockStore distributedLockStore) {
            this.resource = resource;
            this.distributedLockStore = distributedLockStore;
        }

        InnerDistributedReentrantLock(String resource, DistributedLockStore distributedLockStore,
                                      boolean needReentrant) {
            this.resource = resource;
            this.distributedLockStore = distributedLockStore;
            this.needReentrant = needReentrant;
        }

        @Override
        public void lock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock() {
            if (this.localLockToken != null) {
               return false;
            }
            String lockToken = UUID.randomUUID().toString();
            InvokeResult lockInvokeResult = distributedLockStore.lock(resource, lockToken, LOCK_EXPIRE_TIME_SECONDS);
            if (lockInvokeResult.isSuccess() && Boolean.TRUE.equals(lockInvokeResult.getResult())) {
                addLock(lockToken);
                return true;
            }
            if (lockInvokeResult.isSuccess() && Boolean.FALSE.equals(lockInvokeResult.getResult())) {
                if (needReentrant && ifSameHolderThread()) {
                    updateLock();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            if (unit.toMillis(time) > MAX_TRYING_LOCK_TIME_MILLIS) {
                throw new IllegalArgumentException("time is greater than " + MAX_TRYING_LOCK_TIME_MILLIS + " millis");
            }
            long tryingLockTime = System.currentTimeMillis() + unit.toMillis(time);
            while (System.currentTimeMillis() < tryingLockTime) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                boolean locked = tryLock();
                if (locked) {
                    return true;
                } else {
                    LockSupport.parkUntil(this,System.currentTimeMillis() + 200);
                }
            }
            return false;
        }

        @Override
        public void unlock() {
            LockHolder holder = lockHolders.get(resource);
            if (holder != null && holder.getLockToken().equals(this.localLockToken)) {
                holder.setLockCount(holder.getLockCount() - 1);
                this.localLockToken = null;
                if (holder.getLockCount() <= 0) {
                    lockHolders.remove(resource);
                    InvokeResult<Boolean> unlockResult = distributedLockStore.unlock(resource, holder.getLockToken());
                    if (!unlockResult.isSuccess()) {
                        log.info("unlock failed lock {}", resource);
                    }
                }
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        private boolean ifSameHolderThread() {
            LockHolder holder = lockHolders.get(resource);
            return holder != null && holder.getHolderThread() == Thread.currentThread();
        }

        private void addLock(String lockToken) {
            LockHolder holder = new LockHolder();
            holder.setHolderThread(Thread.currentThread());
            holder.setLockCount(1);
            holder.setLockToken(lockToken);
            this.localLockToken = lockToken;
            holder.setLastHeartbeatTimeMillis(System.currentTimeMillis());
            lockHolders.put(resource, holder);
        }

        private void updateLock() {
            LockHolder holder = lockHolders.get(resource);
            if (holder != null) {
                this.localLockToken = holder.getLockToken();
                holder.setLockCount(holder.getLockCount() + 1);
            }
        }
    }

}