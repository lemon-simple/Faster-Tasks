
package com.vivo.internet.fasterTask.concurrent;

/**
 * 
 * @author zhangsh
 * 
 */
public interface TaskProcessFactory {
    TaskProcess createTaskProcess(String domain);
}
