package com.lemon.faster.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 
 * @author zhangsh
 */
public class BarrierLatch {

    private static final class Sync extends AbstractQueuedSynchronizer {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private int count;

        Sync(int count) {
            this.count = count;
        }
        int getCount() {
            return getState();
        }

        public int tryAcquireShared(int acquires) {
            for (;;) {
                int c = getState();
                int nextc = c + 1;
                if (nextc <= count && compareAndSetState(c, nextc)) {
                    return 1;//to compare  CountDownLatch: return (getState() == 0) ? 1 :-1;
                } else {
                    return -1;// failure 
                }
            }
        }

        public boolean tryReleaseShared(int releases) {
            boolean f = false;
            for (;;) {
                int c = getState();
                if (c == 0) {
                    return f;
                }
                int nextc = c - 1;
                if (compareAndSetState(c, nextc)) {
                    return true;
                }
            }
        }
    }

    private final Sync sync;

    public BarrierLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        sync = new Sync(count);
    }

    public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    public void release() {
        sync.releaseShared(1);
    }

    public long getCount() {
        return sync.getCount();
    }
}
