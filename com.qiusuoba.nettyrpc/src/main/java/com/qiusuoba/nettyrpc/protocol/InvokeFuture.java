package com.qiusuoba.nettyrpc.protocol;  

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.qiusuoba.nettyrpc.exception.RpcException;

/**
 *封装请求和响应的桥梁 
 *@Author:caimin 
 *@Since:2016年7月22日  
 *@Version:
 */
public class InvokeFuture {
	
	private static final Log log=LogFactory.getLog(InvokeFuture.class);
	
	private Channel channel;
	private RpcRequest request;
	private RpcResponse response;
	
	private static Map<String, InvokeFuture> invokeMap=new ConcurrentHashMap<String, InvokeFuture>();
	
	private CountDownLatch cdl = new CountDownLatch(1);
	
	/**
	 * 发送请求
	 */
	public void send()
	{
		ChannelFuture writeFuture = channel.write(request);
		boolean ret = writeFuture.awaitUninterruptibly(1000, TimeUnit.MILLISECONDS);
		if (ret && writeFuture.isSuccess()) {
			return;
		}
		else if(writeFuture.getCause() != null)
		{
			invokeMap.remove(request.getRequestID());
			throw new RpcException(writeFuture.getCause());
		}
		else
		{
			invokeMap.remove(request.getRequestID());
			throw new RpcException("sendRequest error");
		}
	}
	
	public RpcResponse get(long awaitTime)
	{
		boolean isOverTime=false;
		try {
			isOverTime=cdl.await(awaitTime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			invokeMap.remove(request.getRequestID());
			throw new RpcException("InterruptedException",e);
		}
		if(isOverTime)
		{
			invokeMap.remove(request.getRequestID());
			return response;
		}
		else
		{
			invokeMap.remove(request.getRequestID());
			throw new RpcException("sendRequest overtime");
		}
	}
	
	public boolean isDone()
	{
		return cdl.getCount()==0;
	}
	
	public void doReceive(RpcResponse response)
	{
		this.response=response;
		cdl.countDown();
	}
	
	public static void receive(Channel channel,RpcResponse response)
	{
		InvokeFuture future=invokeMap.remove(response.getRequestID());
		if(future!=null)
		{
			future.doReceive(response);
		}
		else
		{
			throw new RpcException("TimeOut");
		}
	}
	
	public InvokeFuture(Channel channel, RpcRequest request)
	{
		this.channel=channel;
		this.request=request;
		invokeMap.put(request.getRequestID(), this);
	}
	
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	public RpcRequest getRequest() {
		return request;
	}
	public void setRequest(RpcRequest request) {
		this.request = request;
	}
	public RpcResponse getResponse() {
		return response;
	}
	public void setResponse(RpcResponse response) {
		this.response = response;
	}
	
}
