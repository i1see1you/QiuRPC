package com.qiusuoba.nettyrpc.config;  

/**
 *客户端单个服务配置实体 
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class ServiceConfig {
	
	private String name=null;
	
	private String connectStr=null;

	private String maxConnection=null;
	
	private boolean async=false;
	
	public ServiceConfig() {
		super();
	}

	public ServiceConfig(String name, String connectStr, String maxConnection,String async) {
		super();
		this.name = name;
		this.connectStr = connectStr;
		this.maxConnection = maxConnection;
		this.async="true".equals(async);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConnectStr() {
		return connectStr;
	}

	public void setConnectStr(String connectStr) {
		this.connectStr = connectStr;
	}

	public String getMaxConnection() {
		return maxConnection;
	}

	public void setMaxConnection(String maxConnection) {
		this.maxConnection = maxConnection;
	}

	public boolean getAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}
	
	
}

