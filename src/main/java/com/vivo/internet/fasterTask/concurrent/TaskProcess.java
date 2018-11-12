package com.vivo.internet.fasterTask.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vivo.internet.fasterTask.concurrent.task.IdentityTaskAction;
import com.vivo.internet.fasterTask.concurrent.task.TaskAction;

/**
 * 
 * @author zhangsh
 * 
 */
@SuppressWarnings("unchecked")
public class TaskProcess {
	private static Logger logger = LoggerFactory.getLogger(TaskProcess.class);

	private ExecutorService executor;

	private int coreSize;

	private int poolSize;

	private String domain;

	public TaskProcess(String domain, int coreSize, int poolSize) {
		this.coreSize = coreSize;
		this.poolSize = poolSize;
		this.domain = domain;
		init();
	}

	public TaskProcess(ExecutorService executor) {
		this.executor = executor;
		addHook();
	}

	private void createThreadPool() {
		executor = new ThreadPoolExecutor(coreSize, poolSize, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
				new DefaultThreadFactory(domain), new ThreadPoolExecutor.CallerRunsPolicy());
	}

	private void addHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (executor == null) {
					return;
				}
				try {
					executor.shutdown();
					executor.awaitTermination(5, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					logger.warn("interrupted when shuting down the process executor:\n{}", e);
				}
			}
		});
	}

	private void init() {
		createThreadPool();
		addHook();
	}

	public <T> List<T> executeTask(List<TaskAction<T>> tasks) {
		TaskAction<T>[] actions = new TaskAction[tasks.size()];
		for (int i = 0; i < tasks.size(); i++) {
			actions[i] = tasks.get(i);
		}
		return executeTask(actions);
	}

	/**
	 * 
	 * @param tasks
	 * @return
	 */
	public <T> List<T> executeTask(TaskAction<T>... tasks) {
		final CountDownLatch latch = new CountDownLatch(tasks.length);

		List<Future<T>> futures = new ArrayList<Future<T>>();
		List<T> resultList = new ArrayList<T>();

		for (final TaskAction<T> runnable : tasks) {
			Future<T> future = executor.submit(new Callable<T>() {
				@Override
				public T call() throws Exception {
					try {
						return runnable.doInAction();
					} finally {
						latch.countDown();
					}
				}
			});
			futures.add(future);
		}

		try {
			latch.await(30, TimeUnit.SECONDS);// block if all tasks be finished!
		} catch (Exception e) {
			logger.info("Executing Task is interrupt.");
		}

		for (Future<T> future : futures) {
			try {
				T result = future.get();// wait
				if (result != null) {
					resultList.add(result);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return resultList;
	}

	/**
	 * 异步执行任务
	 * 
	 * @param tasks
	 */
	public <T> void asyncExecuteTask(List<TaskAction<T>> tasks) {
		TaskAction<T>[] actions = new TaskAction[tasks.size()];
		for (int i = 0; i < tasks.size(); i++) {
			actions[i] = tasks.get(i);
		}
		asyncExecuteTask(actions);
	}

	public void asyncExecuteTask(TaskAction<?>... tasks) {
		for (final TaskAction<?> runnable : tasks) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						runnable.doInAction();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	/**
	 * @param concurrentCount
	 *            并行执行的最大任务数
	 * @param tasks
	 * @return
	 */
	public <T> List<T> exeucteTaskByLatch(int concurrentCount, List<TaskAction<T>> tasks) {
		TaskAction<T>[] actions = new TaskAction[tasks.size()];
		for (int i = 0; i < tasks.size(); i++) {
			actions[i] = tasks.get(i);
		}
		return exeucteTaskByLatch(concurrentCount, actions);
	}

	public <T> List<T> exeucteTaskByLatch(int concurrentCount, TaskAction<T>... tasks) {
		final BarrierLatch latch = new BarrierLatch(concurrentCount);

		List<Future<T>> futures = new ArrayList<Future<T>>();
		List<T> resultList = new ArrayList<T>();

		for (final TaskAction<T> runnable : tasks) {
			try {
				latch.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Future<T> future = executor.submit(new Callable<T>() {
				@Override
				public T call() throws Exception {
					try {
						return runnable.doInAction();
					} finally {
						latch.release();
					}
				}
			});
			futures.add(future);
		}

		for (Future<T> future : futures) {
			try {
				T result = future.get();// wait
				if (result != null) {
					resultList.add(result);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return resultList;
	}

	/**
	 * 标识性任务执行，等待执行结果，对任务执行结果分类
	 * 
	 * @param tasks
	 * @return
	 */
	public <T> Map<String, Object> executeIdentityTask(List<IdentityTaskAction<Object>> tasks) {
		IdentityTaskAction<Object>[] actions = new IdentityTaskAction[tasks.size()];
		for (int i = 0; i < tasks.size(); i++) {
			actions[i] = tasks.get(i);
		}
		return executeIdentityTask(actions);
	}

	public <T> Map<String, T> executeIdentityTask(IdentityTaskAction<T>... tasks) {
		final CountDownLatch latch = new CountDownLatch(tasks.length);

		Map<String, Future<T>> futures = new HashMap<String, Future<T>>();
		Map<String, T> resultMap = new HashMap<String, T>();

		for (final IdentityTaskAction<T> runnable : tasks) {
			Future<T> future = executor.submit(new Callable<T>() {
				@Override
				public T call() throws Exception {
					long time = System.currentTimeMillis();
					try {
						return runnable.doInAction();
					} finally {
						logger.debug("Executing Task : {} ,time :{}", runnable.identity(),
								System.currentTimeMillis() - time);
						latch.countDown();
					}

				}
			});
			futures.put(runnable.identity(), future);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.info("Executing Task is interrupt.");
		}

		Iterator<Entry<String, Future<T>>> it = futures.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Future<T>> entry = it.next();
			try {
				T result = entry.getValue().get();
				resultMap.put(entry.getKey(), result);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return resultMap;
	}

	/**
	 * The default thread factory
	 */
	static class DefaultThreadFactory implements ThreadFactory {
		static final AtomicInteger poolNumber = new AtomicInteger(1);

		final ThreadGroup group;

		final AtomicInteger threadNumber = new AtomicInteger(1);

		final String namePrefix;

		DefaultThreadFactory(String domain) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = domain + "-TaskProcess-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	public <T> Holder<T> syncExecuteWaiting(final TaskAction<T> task) {
		Future<T> future = executor.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				try {
					return task.doInAction();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		return new Holder<T>(future);
	}
}
