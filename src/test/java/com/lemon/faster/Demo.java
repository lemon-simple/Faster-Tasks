
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
 * @author zhangsh
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
