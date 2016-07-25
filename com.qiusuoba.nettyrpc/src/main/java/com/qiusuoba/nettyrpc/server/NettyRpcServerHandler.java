package com.qiusuoba.nettyrpc.server;  

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;

import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;
import com.qiusuoba.nettyrpc.util.ReflectionCache;

/**
 *RPC具体处理接口 
 *@Author:caimin 
 *@Since:2015年9月17日  
 *@Version:
 */
public class NettyRpcServerHandler extends SimpleChannelUpstreamHandler {

	private static final Log logger = LogFactory.getLog(NettyRpcServerHandler.class);

	private final ChannelGroup channelGroups;

	public NettyRpcServerHandler() {
		this.channelGroups = null;
	}

	public NettyRpcServerHandler(ChannelGroup channelGroups) {
		this.channelGroups = channelGroups;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if (null != channelGroups) {
			channelGroups.add(e.getChannel());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		RpcRequest request = (RpcRequest) ctx.getAttachment();
		logger.error("handle rpc request fail! request: "+request,e.getCause());
		e.getChannel().close().awaitUninterruptibly();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Object msg = e.getMessage();
		if (!(msg instanceof RpcRequest)) {
			logger.error("not RpcRequest received!");
			return;
		}
		RpcRequest request = (RpcRequest) msg;
		ctx.setAttachment(request);

		RpcResponse response = new RpcResponse(request.getRequestID());
		try {
			Object result = handle(request);
			response.setResult(result);
		} catch (Throwable t) {
			logger.error("handle rpc request fail! request: "+request,t);
			response.setException(t);
		}
		e.getChannel().write(response);
	}

	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		Object rpcService = ExtensionLoader.getProxy(className);
		if (null == rpcService)
			throw new NullPointerException("server interface config is null");

		Method method = ReflectionCache.getMethod(request.getInterfaceName(),
				request.getMethodName(), request.getParameterTypes());
		Object[] parameters = request.getParameters();
		// invoke
		Object result = method.invoke(rpcService, parameters);
		return result;
	}

}
