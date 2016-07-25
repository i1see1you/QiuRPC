package com.qiusuoba.nettyrpc.client;  

import com.qiusuoba.nettyrpc.common.Constants;
import com.qiusuoba.nettyrpc.exception.RpcException;
import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;

/**
 *控制客户端调用服务的最大并发量，超过最大并发量直接抛异常 
 *@Author:caimin 
 *@Since:2016年7月22日  
 *@Version:
 */
public class TpsFilter implements Filter{
	
	private Filter next;
	
	public TpsFilter(Filter next)
	{
		this.next=next;
	}

	@Override
	public RpcResponse sendRequest(RpcRequest request, ILoadBlance loadBlance,String serviceName) throws Throwable {
		int maxConcurrentNum=Constants.CLIENT_CONCURRENT_NUM;
		if(loadBlance.getCurTotalCount()>maxConcurrentNum)
		{
			throw new RpcException("total invoke is bigger than "+maxConcurrentNum);
		}
		else
		{
			return next.sendRequest(request, loadBlance, serviceName);
		}
	}
}
