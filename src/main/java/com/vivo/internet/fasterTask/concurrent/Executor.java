package com.vivo.internet.fasterTask.concurrent;

/**
 * @author zhangsh
 */
public class Executor {

    private static final String COMMON_BUSINESS = "COMMON_EXECUTOR";

    private static DefaultTaskProcessFactory taskProcessFactory = new DefaultTaskProcessFactory();

    public static TaskProcess getCommonTaskProcess() {
        return TaskProcessManager.getTaskProcess(COMMON_BUSINESS, taskProcessFactory);
    }
    
    public static TaskProcess getCommonTaskProcess(String threadGroupName) {
        return TaskProcessManager.getTaskProcess(threadGroupName, taskProcessFactory);
    }
    
    
}
