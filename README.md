# FasterTask
the simplest way to Make your Tasks Concurrent!
![Image text](https://github.com/lemon-simple/Faster-Tasks/blob/master/FasterTask.png)
##为什么写个组件？
- 为了方便串行执行转换为并行执行，大幅提升业务代码的执行速度
当我们写业务代码时,经常会遇到需要提升运行速度的场景，这时候会想到并发编程，但是有很多coder并不太熟悉并发，为了避免引入风险，因此望而却步.
对于并发不是很熟悉的coder，我希望能够提供一个安全、 快速、轻量级并简单易用的组件，能够快速让代码从串行执行变为并行执行，大幅提升业务代码的执行速度。

##为什么要并行执行？

1.充分利用多核处理器 

一个进程下会有多个线程，一个线程的运行会占用一个处理器核心。
现在多核cpu已经司空见惯，如果我们编程还是单线程，那么多核cpu中只会有一个核心被使用，
其他核心被闲置。为了重复利用cpu多核资源，提高运算能力，我们使任务并行，同一时间可以在cpu的多核上运行多个任务。

2.更快的响应体验
比如,催收项目跑批数据隐射，使用并发，原来30分钟执行成功的任务，可缩短10分钟内.
再比如，我们请求一个页面，如果是单线程，会挨个获取这个页面上的视频、图片、文字等等资源；如果是多线程，会并发回去这个页面上的资源，以缩短页面加载的时间。更快的速度，更好的用户体验。

##如何使用?

使用demo展示
Demo

```
/*
 */
package com.lemon.faster;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
 
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import com.lemon.faster.concurrent.Executor;
import com.lemon.faster.concurrent.task.IdentityTaskAction;
import com.lemon.faster.concurrent.task.TaskAction;
 
/**
 *
 */
public class Demo {
    private final Logger logger = LoggerFactory.getLogger(getClass());
 
    /**
     * 针对需要获取返回值的任务，类似FutureTask
     *
     * 并行执行多个任务,并对每个任务做标记,并行执行后，取回每个任务执行结果的返回值.
     *
     * (需要保证任务之间没有依赖，否则需要用戶进行一定同步)
     */
    @SuppressWarnings("unchecked")
    @Test
    public <T> void testIdentityTaskAction() {
        List<String> taskDResult = new ArrayList<String>();
        Map<String, String> taskEResult = new HashMap<String, String>();
        String taskFResult = "";
 
        // assemble your task for executing concurrently
        List<IdentityTaskAction<Object>> tasks = new ArrayList<IdentityTaskAction<Object>>();
        IdentityTaskAction<Object> taskD = new IdentityTaskAction<Object>() {
            @Override
            public Object doInAction() throws Exception {
                List<String> result = bussinessLogicTaskD();
                return result;
            }
 
            @Override
            public String identity() {
                return "taskD";
            }
        };
        IdentityTaskAction<Object> taskE = new IdentityTaskAction<Object>() {
            @Override
            public Object doInAction() throws Exception {
                Map<String, String> result = bussinessLogicTaskE();
                return result;
            }
 
            @Override
            public String identity() {
                return "taskE";
            }
        };
        IdentityTaskAction<Object> taskF = new IdentityTaskAction<Object>() {
            @Override
            public Object doInAction() throws Exception {
                String result = bussinessLogicTaskF();
                return result;
            }
 
            @Override
            public String identity() {
                return "taskF";
            }
        };
        long start = System.currentTimeMillis();
        // to execute 3 tasks concurrently
        tasks.add(taskD);
        tasks.add(taskE);
        tasks.add(taskF);
        Map<String, Object> result = Executor.getCommonTaskProcess().executeIdentityTask(tasks);
        // get result by task identity
        logger.info("current testIdentityTaskAction elapsed:{}", (System.currentTimeMillis() - start));// 9015ms
        taskDResult = (List<String>) result.get("taskD");
        taskEResult = (Map<String, String>) result.get("taskE");
        taskFResult = (String) result.get("taskF");
 
        logger.info("current testIdentityTaskAction. result of TaskD:{}", taskDResult);
        logger.info("current testIdentityTaskAction. result of TaskE:{}", taskEResult);
        logger.info("current testIdentityTaskAction. result of TaskF:{}", taskFResult);
 
    }
 
    /**
     * 并行执行多个任务
     *
     * (需要保证任务之间没有依赖，否则需要用戶进行一定同步)
     */
    @Test
    public <T> void testExecuteTask() {
        List<TaskAction<T>> tasks = new ArrayList<TaskAction<T>>();
        // assemble your business logics to be Tasks
        TaskAction<T> taskA = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskA();
                return null;
            }
        };
        TaskAction<T> taskB = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskB();
                return null;
            }
        };
        TaskAction<T> taskC = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskC();
                return null;
            }
        };
        // to execute 3 tasks concurrently
        tasks.add(taskA);
        tasks.add(taskB);
        tasks.add(taskC);
 
        long start = System.currentTimeMillis();
        Executor.getCommonTaskProcess().executeTask(tasks);
        logger.info("current testExecuteTask elapsed:{}", (System.currentTimeMillis() - start));// 9019ms
    }
 
    /**
     * 异步并行执行多个任务,不等待结果返回
     */
    @Test
    public <T> void testAsyncExecuteTask() {
        List<TaskAction<T>> tasks = new ArrayList<TaskAction<T>>();
        // assemble your business logics to be Tasks
        TaskAction<T> taskA = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskA();
                return null;
            }
        };
        TaskAction<T> taskB = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskB();
                return null;
            }
        };
        TaskAction<T> taskC = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskC();
                return null;
            }
        };
        // to execute 3 tasks concurrently
        tasks.add(taskA);
        tasks.add(taskB);
        tasks.add(taskC);
 
        long start = System.currentTimeMillis();
        Executor.getCommonTaskProcess().asyncExecuteTask(tasks);
        logger.info("current testAsyncExecuteTask elapsed:{}", (System.currentTimeMillis() - start));// 1ms
    }
 
    /**
     * 可控制一次并行执行的最大任务数量,以调整资源的使用
     *
     * 可控制barrier数量,barrier数量到达后latch打开.
     *
     *
     * (需要保证任务之间没有依赖，否则需要用戶进行一定同步)
     */
    @Test
    public <T> void testExeucteTaskByLatch() {
        List<TaskAction<T>> tasks = new ArrayList<TaskAction<T>>();
        // assemble your business logics to be Tasks
        TaskAction<T> taskA = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskA();
                return null;
            }
        };
        TaskAction<T> taskB = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskB();
                return null;
            }
        };
        TaskAction<T> taskC = new TaskAction<T>() {
            @Override
            public T doInAction() throws Exception {
                bussinessLogicTaskC();
                return null;
            }
        };
        // to execute 3 tasks concurrently
        tasks.add(taskA);
        tasks.add(taskB);
        tasks.add(taskC);
 
        long start = System.currentTimeMillis();
        Executor.getCommonTaskProcess().exeucteTaskByLatch(3, tasks);// 9021ms
        logger.info("current testExeucteTaskByLatch elapsed:{}", (System.currentTimeMillis() - start));
    }
 
    @Test
    public void testSerialTaskExecution() throws InterruptedException {
        long start = System.currentTimeMillis();
        bussinessLogicTaskA();
        bussinessLogicTaskB();
        bussinessLogicTaskC();
        logger.info("serial elapsed:{}", (System.currentTimeMillis() - start));// 21023ms
    }
 
    /**
     * Task A
     * @throws InterruptedException
     */
    private void bussinessLogicTaskA() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        logger.info("bussinessLogicTaskA execute By thread[:{} ] at time:{}", Thread.currentThread().getName(),
                new Date());
    }
 
    /**
     * Task B
     * @throws InterruptedException
     */
    private void bussinessLogicTaskB() throws InterruptedException {
        TimeUnit.SECONDS.sleep(7);
        logger.info("bussinessLogicTaskB execute By thread[:{} ] at time:{}", Thread.currentThread().getName(),
                new Date());
    }
 
    /**
     * Task C
     * @throws InterruptedException
     */
    private void bussinessLogicTaskC() throws InterruptedException {
        TimeUnit.SECONDS.sleep(9);
        logger.info("bussinessLogicTaskC execute By thread[:{} ] at time:{}", Thread.currentThread().getName(),
                new Date());
    }
 
    /**
     * Task D
     * @throws InterruptedException
     */
    private List<String> bussinessLogicTaskD() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        logger.info("bussinessLogicTaskD  execute By thread[:{} ] at time:{}", Thread.currentThread().getName(),
                new Date());
        return Arrays.asList("A", "B", "...", "X");// result
    }
 
    /**
     * Task E
     * @throws InterruptedException
     */
    private Map<String, String> bussinessLogicTaskE() throws InterruptedException {
        TimeUnit.SECONDS.sleep(7);
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("A", "resultA");
        resultMap.put("B", "resultB");
        logger.info("bussinessLogicTaskE  execute By thread[:{} ] at time:{}", Thread.currentThread().getName(),
                new Date());
        return resultMap;// result
    }
 
    /**
     * Task F
     * @throws InterruptedException
     */
    private String bussinessLogicTaskF() throws InterruptedException {
        TimeUnit.SECONDS.sleep(9);
        String result = new StringBuffer().append("result").append("of").append("bussinessLogicTaskF").toString();
        logger.info("bussinessLogicTaskF  execute By thread[:{} ] at time:{}", Thread.currentThread().getName(),
                new Date());
        return result;// result
    }
 
}
```
