package com.qiusuoba.nettyrpc.util;  

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *可命名的线程工厂 
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class NamedTheadFactory implements ThreadFactory{
	private String threadName = null;
	
	public NamedTheadFactory(String threadName)
	{
		this.threadName=threadName;
	}
	
    private final AtomicInteger threadId = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        Thread ret = new Thread(Thread.currentThread().getThreadGroup(), r, threadName + "-Thread-"
                + threadId.getAndIncrement(), 0);
        ret.setDaemon(false);
        if (threadId.get() == Integer.MAX_VALUE) {
            threadId.set(0);
        }
        return ret;
    }

}
