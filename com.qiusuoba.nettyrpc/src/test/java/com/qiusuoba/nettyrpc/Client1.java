package com.qiusuoba.nettyrpc;  

import java.util.concurrent.Future;

import com.qiusuoba.nettyrpc.client.RpcClientProxy;
import com.qiusuoba.nettyrpc.common.RpcContext;
  
public class Client1 {
	
	public static void main(String[] args) {
		try {
			final IServer1 server1=RpcClientProxy.proxy(IServer1.class,"server1" , "myserver1");
			long startMillis=System.currentTimeMillis();
			for(int i=0;i<10000;i++)
			{
				final int f_i=i;
				send(server1,f_i);
			}
			long endMillis=System.currentTimeMillis();
			System.out.println("spend time:"+(endMillis-startMillis));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static void send(IServer1 server1,int f_i)
	{
		Message msg = null;
		try
		{
			//由于客户端配置的async="true"，我们用异步方式来获取结果，如果是同步方式，直接msg=server1.echoMsg(f_i)即可
			server1.echoMsg(f_i);
			Future<Message> future = RpcContext.getContext().getFuture();
			msg=future.get();
			System.out.println("msg:"+msg.getMsg()+","+msg.getData());
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}
