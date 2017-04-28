package com.lemon.faster.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author zhangsh
 */
public class TaskProcessManager {

    TaskProcessManager() {
    }

    private static final String DEFAULT_BUSINESS_DOMAIN = "DefaultDomain";

    private static Map<String, TaskProcess> taskProcessContainer = new ConcurrentHashMap<String, TaskProcess>();

    private static TaskProcessFactory defaultTaskProcessFactory = new DefaultTaskProcessFactory();

    volatile static TaskProcess taskProcess = null;

    public static TaskProcess getTaskProcess(String businessDomain, TaskProcessFactory factory) {
        if (factory == null) {
            factory = defaultTaskProcessFactory;
        }
        taskProcess = taskProcessContainer.get(businessDomain);
        if (taskProcess == null) {
            // DCL双重检查
            synchronized (TaskProcessManager.class) {
                taskProcess = taskProcessContainer.get(businessDomain);
                if (taskProcess == null) {
                    taskProcess = factory.createTaskProcess(businessDomain);
                    taskProcessContainer.put(businessDomain, taskProcess);
                }
            }
        }
        return taskProcess;
    }

    public static TaskProcess getTaskProcess(String businessDomain) {
        return getTaskProcess(businessDomain, defaultTaskProcessFactory);
    }

    public static TaskProcess getTaskProcess() {
        return getTaskProcess(DEFAULT_BUSINESS_DOMAIN, defaultTaskProcessFactory);
    }
}
