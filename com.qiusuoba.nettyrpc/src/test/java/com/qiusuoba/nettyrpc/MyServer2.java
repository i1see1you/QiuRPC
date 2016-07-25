package com.qiusuoba.nettyrpc;  

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qiusuoba.nettyrpc.annotation.ServiceAnnotation;
import com.qiusuoba.nettyrpc.server.RpcServerBootstrap;

@ServiceAnnotation
public class MyServer2 implements IServer1{
	private static final Log log=LogFactory.getLog(MyServer2.class);
	
	public String getMsg()
	{
		log.info("getMsg echo");
		return "Hello";
	}
	
	public static void main(String[] args) {
		RpcServerBootstrap bootstrap=new RpcServerBootstrap();
		bootstrap.start(9090);
	}

	@Override
	public Message echoMsg(String msg) {
		Message result=new Message();
		result.setMsg(msg);
		result.setData(new Date());
		return result;
	}

	@Override
	public Message echoMsg(int msg) {
		Message result=new Message();
		result.setMsg("int:"+msg);
		result.setData(new Date());
		return result;
	}
}
