package com.lemon.faster.concurrent.task;

/**
 * 任务回调封装
 * 
 * @author zhangsh
 * 
 * @param <T>
 *            返回类型
 */
public interface TaskAction<T> {
	T doInAction() throws Exception;
}
