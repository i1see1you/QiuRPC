package com.qiusuoba.nettyrpc.protocol;  

import java.io.Serializable;
import java.util.Date;

/**
 *协议请求实体 
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class RpcRequest implements Serializable{

	/**  
	 *   
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 请求唯一标示
	 */
	private String requestID;
	
	/**
	 * 接口名
	 */
	private String interfaceName; 

	/**
	 * 服务名，也就是@ServiceAnnotation的name
	 */
	private String className;

	/**
	 * 方法名
	 */
	private String methodName;

	/**
	 * 请求参数类型
	 */
	private String[] parameterTypes;

	/**
	 * 请求参数值
	 */
	private Object[] parameters;
	
	/**
	 * 请求时间
	 */
	private Date addTime;
	
	public RpcRequest() {
		super();
	}

	public RpcRequest(String requestID, String interfaceName, String className,
			String methodName, String[] parameterTypes, Object[] parameters) {
		super();
		this.requestID = requestID;
		this.interfaceName = interfaceName;
		this.className = className;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.parameters = parameters;
		this.addTime = new Date();
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(String[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

}
