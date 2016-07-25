package com.qiusuoba.nettyrpc.exception;  

/**
 *所有的异常都统一封装成这个类 
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class RpcException extends RuntimeException{

	public RpcException() {
		super();  
	}

	public RpcException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);  
	}

	public RpcException(String message, Throwable cause) {
		super(message, cause);  
	}

	public RpcException(String message) {
		super(message);  
	}

	public RpcException(Throwable cause) {
		super(cause);  
	}

}
