package com.lemon.faster.concurrent.task;
/**
 * 标记型任务
 * @author zhangsh
 *
 * @param <T>
 */
public interface IdentityTaskAction<T> extends TaskAction<T> {
	String identity();
}
