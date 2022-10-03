package com.github.acticfox.distributed.lock;

/**
 * 类的实现描述：TODO 类实现描述
 *
 * @author fanyong.kfy 2018-03-07 10:00:43
 */

public class LockHolder {
    private Thread holderThread;
    private String lockToken = "";
    private int lockCount = 0;
    private long lastHeartbeatTimeMillis = System.currentTimeMillis();

    public Thread getHolderThread() {
        return holderThread;
    }

    public void setHolderThread(Thread holderThread) {
        this.holderThread = holderThread;
    }

    public int getLockCount() {
        return lockCount;
    }

    public void setLockCount(int lockCount) {
        this.lockCount = lockCount;
    }

    public String getLockToken() {
        return lockToken;
    }

    public void setLockToken(String lockToken) {
        this.lockToken = lockToken;
    }

    public long getLastHeartbeatTimeMillis() {
        return lastHeartbeatTimeMillis;
    }

    public void setLastHeartbeatTimeMillis(long lastHeartbeatTimeMillis) {
        this.lastHeartbeatTimeMillis = lastHeartbeatTimeMillis;
    }
}