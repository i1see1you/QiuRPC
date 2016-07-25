package com.qiusuoba.nettyrpc.client;  

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qiusuoba.nettyrpc.config.ClientConfig;
import com.qiusuoba.nettyrpc.config.ServiceConfig;
import com.qiusuoba.nettyrpc.connect.IRpcConnection;
import com.qiusuoba.nettyrpc.exception.RpcException;
import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;

/**
 *最终执行的Filter，得到socket长连接并发送请求 
 *@Author:caimin 
 *@Since:2016年7月22日  
 *@Version:
 */
public class GenericFilter implements Filter{
	
	private static final Log log=LogFactory.getLog(GenericFilter.class);
	
	@Override
	public RpcResponse sendRequest(RpcRequest request, ILoadBlance loadBlance,String serviceName) throws Throwable {
		IRpcConnection connection = null;
		RpcResponse response = null;
		String connStr=loadBlance.getLoadBlance(serviceName);
		ClientConfig clientConfig = ClientConfig.getInstance();
		ServiceConfig serviceConfig = clientConfig.getService(serviceName);
		try {
			connection = loadBlance.getConnection(connStr);
			if(connection.isConnected() && connection.isClosed())
			{
				connection.connect();
			}
			if(connection.isConnected() && !connection.isClosed())
			{
				response = connection.sendRequest(request,serviceConfig.getAsync());
			}
			else
			{
				throw new RpcException("send rpc request fail");
			}
			return response;
		} catch(RpcException e)
		{
			throw e;
		} catch (Throwable t) {
			log.warn("send rpc request fail! request: "+request,t);
			throw new RpcException(t);
		} finally {
			loadBlance.finishLoadBlance(serviceName, connStr);
		}
	}
	
}
