package com.qiusuoba.nettyrpc.connect;  

import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;

/**
 *客户端连接
 *@Author:caimin 
 *@Since:2015年9月18日  
 *@Version:
 */
public interface IRpcConnection {

	/**
	 * 发送请求
	 * @param request
	 * @param async 标示是否异步发送请求
	 * @return
	 * @throws Throwable
	 */
	RpcResponse sendRequest(RpcRequest request,boolean async) throws Throwable;
	
	/**
	 * 初始化连接
	 * @throws Throwable
	 */
	void open() throws Throwable; 
	
	/**
	 * 重新连接
	 * @throws Throwable
	 */
	void connect() throws Throwable;

	/**
	 * 关闭连接
	 * @throws Throwable
	 */
	void close() throws Throwable;

	/**
	 * 连接是否已经关闭
	 * @return
	 */
	boolean isClosed();
	
	/**
	 * 心跳是否正常
	 * @return
	 */
	boolean isConnected();
}
