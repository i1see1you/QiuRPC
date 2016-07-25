package com.qiusuoba.nettyrpc.client;  

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qiusuoba.nettyrpc.common.Constants;
import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;

/**
 *监控服务性能，如果超时则输出监控日志 
 *@Author:caimin 
 *@Since:2016年7月22日  
 *@Version:
 */
public class TimeOutFilter implements Filter{
	private static final Log log=LogFactory.getLog(GenericFilter.class);
	
	private Filter next;
	
	public TimeOutFilter(Filter next)
	{
		this.next=next;
	}
	@Override
	public RpcResponse sendRequest(RpcRequest request, ILoadBlance loadBlance,String serviceName) throws Throwable {
		long start = System.currentTimeMillis();
		RpcResponse response=next.sendRequest(request, loadBlance, serviceName);
		long spendTime = System.currentTimeMillis()-start;
		if(spendTime>Constants.TIMEOUT_LOG_MILLSECOND)
		{
			log.warn("spend time is bigger than "+ Constants.TIMEOUT_LOG_MILLSECOND +",the serviceName is:"+serviceName);
		}
		return response;
	}

}
