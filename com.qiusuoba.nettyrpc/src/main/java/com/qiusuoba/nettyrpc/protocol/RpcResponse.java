package com.qiusuoba.nettyrpc.protocol;  

/**
 *协议响应实体 
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class RpcResponse {
	
	
	public RpcResponse() {
		super();
	}

	public RpcResponse(String requestID) {
		super();
		this.requestID = requestID;
	}

	private String requestID;

	private Throwable exception;

	private Object result;

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
