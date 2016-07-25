package com.qiusuoba.nettyrpc;  
  
public interface IServer1 {
	public String getMsg();
	
	public Message echoMsg(String msg);
	
	public Message echoMsg(int msg);
}
