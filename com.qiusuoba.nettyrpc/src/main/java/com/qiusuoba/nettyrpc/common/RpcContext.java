package com.qiusuoba.nettyrpc.common;  

import java.util.concurrent.Future;

/**
 *上下文对象   
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class RpcContext {
	
	private static final ThreadLocal<RpcContext> LOCAL = new ThreadLocal<RpcContext>() {
		@Override
		protected RpcContext initialValue() {
			return new RpcContext();
		}
	};
	
	public static RpcContext getContext() {
	    return LOCAL.get();
	}
	
	private Future<?> future = null;

	public <T> Future<T> getFuture() {
		return (Future<T>)future;
	}

	public void setFuture(Future<?> future) {
		this.future = future;
	}
}
