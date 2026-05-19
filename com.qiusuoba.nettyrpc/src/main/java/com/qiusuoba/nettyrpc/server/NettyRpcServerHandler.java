package com.qiusuoba.nettyrpc.server;  

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;
import com.qiusuoba.nettyrpc.util.ReflectionCache;

/**
 *RPC具体处理接口 
 *@Author:caimin 
 *@Since:2015年9月17日  
 *@Version:
 */
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

	private static final Log logger = LogFactory.getLog(NettyRpcServerHandler.class);

	private final ChannelGroup channelGroup;

	public NettyRpcServerHandler() {
		this.channelGroup = null;
	}

	public NettyRpcServerHandler(ChannelGroup channelGroup) {
		this.channelGroup = channelGroup;
	}

	private static final ExecutorService WORKER_SERVICE = Executors.newFixedThreadPool(100);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (null != channelGroup) {
			channelGroup.add(ctx.channel());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		RpcRequest request = (RpcRequest) ctx.attr(ctx.alloc().attrKey("request")).get();
		logger.error("handle rpc request fail! " + cause.getMessage(), cause);
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof RpcRequest)) {
			logger.error("not RpcRequest received!");
			return;
		}
		final RpcRequest request = (RpcRequest) msg;
		ctx.attr(ctx.alloc().attrKey("request")).set(request);
		final RpcResponse response = new RpcResponse(request.getRequestID());
		WORKER_SERVICE.submit(new Runnable() {
			public void run() {
				try {
					Object result = handle(request);
					response.setResult(result);
				} catch (Throwable t) {
					logger.error("handle rpc request fail! request: " + request, t);
					response.setException(t);
				}
				ctx.writeAndFlush(response);
			}
		});
	}

	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		Object rpcService = ExtensionLoader.getProxy(className);
		if (null == rpcService)
			throw new NullPointerException("server interface config is null");

		Method method = ReflectionCache.getMethod(request.getInterfaceName(),
				request.getMethodName(), request.getParameterTypes());
		Object[] parameters = request.getParameters();
		Object result = method.invoke(rpcService, parameters);
		return result;
	}

}
