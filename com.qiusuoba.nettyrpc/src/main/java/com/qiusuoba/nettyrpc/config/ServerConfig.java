package com.qiusuoba.nettyrpc.config;  

/**
 *服务启动配置，参数有线程数目，超时时间，端口等 
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class ServerConfig {
	private int threadCnt=100;
	
	private int readTimeout=1000;
	
	private int connectTimeout=1000;
	
	private int port=9090;
	
	public ServerConfig(int port)
	{
		this.port=port;
	}
	
	public int getThreadCnt() {
		return threadCnt;
	}

	public void setThreadCnt(int threadCnt) {
		this.threadCnt = threadCnt;
	}


	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	

}
