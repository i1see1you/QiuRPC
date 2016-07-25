package com.qiusuoba.nettyrpc.client;  

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;
import com.qiusuoba.nettyrpc.util.Sequence;

/**
 *rpc客户端动态代理类，一般为了提高性能会使用javassist等工具直接操作Java字节码
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class RpcClientProxy implements InvocationHandler{
	
	private static final Log log=LogFactory.getLog(RpcClientProxy.class);
	
	//根据serviceName得到是哪个服务
	private final String serviceName;
	//要调用的接口
	private final Class<?> interfaceClass; 
	//要调用的具体接口实现
	private final String className;
	//负载均衡策略
	private final ILoadBlance loadBlance=new LeastActiveLoadBalance();
	//proxy缓存
	private final static Map<String,Object> proxyMap=new ConcurrentHashMap<String, Object>();
	//服务调用链过滤器
	private final Filter lastFilter;
	
	/**
	 *初始化客户端动态代理类
	* @param interfaceClass 接口类
	* @param serviceName 服务名
	* @param className   实现名
	 */
	private RpcClientProxy(Class<?> interfaceClass,String serviceName,String className) {
		super();
		this.serviceName = serviceName;
		this.interfaceClass = interfaceClass;
		this.className=className;
		GenericFilter genericFilter=new GenericFilter();
		TimeOutFilter timeOutFilter=new TimeOutFilter(genericFilter);
		TpsFilter tpsFilter=new TpsFilter(timeOutFilter);
		lastFilter=tpsFilter;
	}
	
	protected static final String generateRequestID() {
		return Sequence.next()+"";
	}
	
	public static <T> T proxy(Class<T> interfaceClass,String serviceName,String className) throws Throwable {
		if (!interfaceClass.isInterface()) {
			throw new IllegalArgumentException(interfaceClass.getName()
					+ " is not an interface");
		}
		String key=interfaceClass.getName()+"_"+serviceName+"_"+className;
		Object proxy=proxyMap.get(key);
		if(proxy==null)
		{
			proxy= Proxy.newProxyInstance(interfaceClass.getClassLoader(),
					new Class<?>[] { interfaceClass }, new RpcClientProxy(interfaceClass,serviceName,className));
			proxyMap.put(key,proxy);
			log.info("proxy generated,serviceName:"+serviceName+",className:"+className);
		}
		return (T)proxy;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		String interfaceName = interfaceClass.getName();
		List<String> parameterTypes = new LinkedList<String>();
		for (Class<?> parameterType : method.getParameterTypes()) {
			parameterTypes.add(parameterType.getName());
		}
		//requestID用来唯一标识一次请求
		String requestID = generateRequestID();
		final RpcRequest request = new RpcRequest(requestID, interfaceName,className,
		method.getName(), parameterTypes.toArray(new String[0]),
		args);
		RpcResponse response =  lastFilter.sendRequest(request, loadBlance, serviceName);
		return response.getResult();
	}
	
}
