package com.qiusuoba.nettyrpc.common;  



/**
 *一些常量 
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class Constants {
	
	public static final String ROOT_PATH=System.getProperty("user.dir");
	
	public final static String  FILE_SEPARATOR = System.getProperty("file.separator");
	
	public final static String  PATH_SEPARATOR = System.getProperty("path.separator");
	
	//默认的序列化方式，系统自带两种方式：hessian或者protobuf
	public final static String  DEFAULT_RPC_CODE_MODE = "protobuf";
	
	//单客户端调用服务的最大并发量
	public final static int CLIENT_CONCURRENT_NUM = 100;
	
	//服务调用timeout阈值，超过阈值会记录日志
	public final static long TIMEOUT_LOG_MILLSECOND = 100;
	
	//客户端连接服务的超时时间
	public final static long TIMEOUT_CONNECTION_MILLSECOND = 1000;
	
	//客户端心跳超时时间
	public final static long TIMEOUT_HEARTBEAT_MILLSECOND = 3000;
	
	//服务调用的默认超时时间
	public final static long TIMEOUT_INVOKE_MILLSECOND = 1000;
	
    // magic header.
	public static final short    MAGIC              = (short) 0xca80;
    
	public static final byte     MAGIC_HIGH         = (byte) 0xca;
    
	public static final byte     MAGIC_LOW          = (byte) 0x80;
    
	public static final byte[]     MAGIC_BYTES          = new byte[]{MAGIC_HIGH,MAGIC_LOW};
    
    
}
