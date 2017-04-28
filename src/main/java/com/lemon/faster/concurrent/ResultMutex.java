
package com.lemon.faster.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author zhangsh
 * 
 */
public class ResultMutex<T> {
    private Sync sync;

    private T result;

    public ResultMutex() {
        super();
        this.sync = new Sync();
        sync.innerSetFalse();
    }

    /**
     * 阻塞等待直到结果被set
     * @return
     * @throws InterruptedException
     */
    public T getResult() throws InterruptedException {
        sync.innerGet();

        return result;
    }

    /**
     * 阻塞等待直到结果被set,允许设置超时时间
     * 
     * @param timeout
     * @param unit
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public T getResult(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        sync.innerGet(unit.toNanos(timeout));
        return result;
    }

    /**
     * 重新设置对应的Boolean mutex
     * 
     * @param mutex
     */
    public void setResult(T result) {
        sync.innerSetTrue();
        this.result = result;
    }

    public boolean isSet() {
        return sync.innerState();
    }

    /**
     * Synchronization control for ResultMutex. Uses AQS sync state to represent
     * run status
     */
    private final class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 2559471934544126329L;

        /** State value representing that TRUE */
        private static final int TRUE = 1;

        /** State value representing that FALSE */
        private static final int FALSE = 2;

        private boolean isTrue(int state) {
            return (state & TRUE) != 0;
        }

        /**
         * 实现AQS的接口，获取共享锁的判断
         */
        protected int tryAcquireShared(int state) {
            // 如果为true，直接允许获取锁对象
            // 如果为false，进入阻塞队列，等待被唤醒
            return isTrue(getState()) ? 1 : -1;
        }

        /**
         * 实现AQS的接口，释放共享锁的判断
         */
        protected boolean tryReleaseShared(int ignore) {
            // 始终返回true，代表可以release
            return true;
        }

        boolean innerState() {
            return isTrue(getState());
        }

        void innerGet() throws InterruptedException {
            acquireSharedInterruptibly(0);
        }

        void innerGet(long nanosTimeout) throws InterruptedException, TimeoutException {
            if (!tryAcquireSharedNanos(0, nanosTimeout))
                throw new TimeoutException();
        }

        void innerSetTrue() {
            for (;;) {
                int s = getState();
                if (s == TRUE) {
                    return; // 直接退出
                }
                if (compareAndSetState(s, TRUE)) {// cas更新状态，避免并发更新true操作
                    releaseShared(0);// 释放一下锁对象，唤醒一下阻塞的Thread
                    return;
                }
            }
        }

        void innerSetFalse() {
            for (;;) {
                int s = getState();
                if (s == FALSE) {
                    return; // 直接退出
                }
                if (compareAndSetState(s, FALSE)) {// cas更新状态，避免并发更新false操作
                    return;
                }
            }
        }

    }
}
