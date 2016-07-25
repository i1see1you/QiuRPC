package com.qiusuoba.nettyrpc.client;  

import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;

/**
 *服务调用链过滤器,可以自定义过滤器来实现一些AOP功能
 *@Author:caimin 
 *@Since:2016年7月22日  
 *@Version:
 */

public interface Filter {
	
	/**
	 * 服务调用
	 * @param request
	 * @param loadBlance
	 * @param serviceName
	 * @return
	 * @throws Throwable
	 */
	RpcResponse sendRequest(RpcRequest request, ILoadBlance loadBlance,String serviceName) throws Throwable;

}