package com.qiusuoba.nettyrpc.connect;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.qiusuoba.nettyrpc.common.Constants;
import com.qiusuoba.nettyrpc.common.RpcContext;
import com.qiusuoba.nettyrpc.config.ClientConfig;
import com.qiusuoba.nettyrpc.exception.RpcException;
import com.qiusuoba.nettyrpc.protocol.FutureAdapter;
import com.qiusuoba.nettyrpc.protocol.InvokeFuture;
import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcRequestEncode;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;
import com.qiusuoba.nettyrpc.protocol.RpcResponseDecode;
import com.qiusuoba.nettyrpc.util.NamedTheadFactory;

/**
 *netty客户端长连接 
 *@Author:caimin 
 *@Since:2015年9月18日  
 *@Version:
 */
public class NettyRpcConnection extends ChannelInboundHandlerAdapter implements IRpcConnection {
	private static final Log log = LogFactory.getLog(NettyRpcConnection.class);
	
	private static final ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1, new NamedTheadFactory("ConnectionHeart"));

	private volatile long lastConnectedTime = System.currentTimeMillis();
	
	private InetSocketAddress inetAddr;

	private volatile Channel channel;

	private volatile AtomicBoolean connected = new AtomicBoolean(false);
	
	private static final ClientConfig clientConfig = ClientConfig.getInstance();
	
	private Bootstrap bootstrap = null;
	
	private EventLoopGroup group;
	
	public NettyRpcConnection(String connStr) {
		this.inetAddr = new InetSocketAddress(connStr.split(":")[0], Integer.parseInt(connStr.split(":")[1]));
		initReconnect();
	}

	public NettyRpcConnection(String host, int port) {
		this.inetAddr = new InetSocketAddress(host, port);
		initReconnect();
	}

	public RpcResponse sendRequest(RpcRequest request, boolean async) throws Throwable {
		if (!isConnected() || !channel.isActive()) {
			throw new RpcException("not connected");
		}
		if (System.currentTimeMillis() - request.getAddTime().getTime() > Constants.TIMEOUT_INVOKE_MILLSECOND) {
			log.info("request timeout exception");
			throw new RpcException("request timeout exception");
		}
		InvokeFuture invokeFuture = new InvokeFuture(channel, request);
		invokeFuture.send();
		if (async) {
			RpcContext.getContext().setFuture(new FutureAdapter<Object>(invokeFuture));
			return new RpcResponse();
		} else {
			RpcContext.getContext().setFuture(null);
			return invokeFuture.get(Constants.TIMEOUT_INVOKE_MILLSECOND);
		}
	}
	
	public void open() throws Throwable {
		open(true);
	}
	
	public void open(boolean connectStatus) throws Throwable {
		log.info("open start," + getConnStr());
		
		group = new NioEventLoopGroup(clientConfig.getMaxThreadCount());
		bootstrap = new Bootstrap();
		bootstrap.group(group)
			.channel(NioSocketChannel.class)
			.option(ChannelOption.TCP_NODELAY, Boolean.parseBoolean(clientConfig.getTcpNoDelay()))
			.option(ChannelOption.SO_REUSEADDR, Boolean.parseBoolean(clientConfig.getReuseAddress()))
			.option(ChannelOption.SO_RCVBUF, 1024 * 128)
			.option(ChannelOption.SO_SNDBUF, 1024 * 128)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("encoder", new RpcRequestEncode());
					pipeline.addLast("decoder", new RpcResponseDecode());
					pipeline.addLast("handler", NettyRpcConnection.this);
				}
			});
		
		connected.set(connectStatus);
		log.info("open finish," + getConnStr());
	}
	
	public void initReconnect() {
		Runnable connectStatusCheckCommand = new Runnable() {
			@Override
			public void run() {
				try {
					if (!isConnected()) {
						try {
							open(false);
							connect();
							connected.set(true);
						} catch (Throwable e) {
							log.error("connect open error,conn:" + getConnStr());
						}
					}
					if (isConnected() && isClosed()) {
						try {
							connect();
						} catch (Throwable e) {
							log.error("connect error,conn:" + getConnStr());
						}
					}
					if (isConnected() && !isClosed()) {
						lastConnectedTime = System.currentTimeMillis();
					}
					if (System.currentTimeMillis() - lastConnectedTime > Constants.TIMEOUT_HEARTBEAT_MILLSECOND) {
						if (connected.get() == true) {
							connected.set(false);
							log.error("connected has loss heartbeat,conn:" + getConnStr());
						}
					}
				} catch (Throwable e) {
					log.error("connectStatusCheckCommand error");
				}
			}
		};
		executorService.scheduleAtFixedRate(connectStatusCheckCommand, 1000, 1000, TimeUnit.MILLISECONDS);
	}
	
	public void connect() {
		ChannelFuture future = bootstrap.connect(inetAddr);
		try {
			boolean ret = future.awaitUninterruptibly(Constants.TIMEOUT_CONNECTION_MILLSECOND, TimeUnit.MILLISECONDS);
			if (ret && future.isSuccess()) {
				Channel newChannel = future.channel();
				try {
					Channel oldChannel = NettyRpcConnection.this.channel;
					if (oldChannel != null) {
						log.info("Close old netty channel " + oldChannel + " on create new netty channel " + newChannel);
						oldChannel.close();
					}
				} finally {
					if (!isConnected()) {
						try {
							log.info("Close new netty channel " + newChannel + ", because the client closed.");
							newChannel.close();
						} finally {
							NettyRpcConnection.this.channel = null;
						}
					} else {
						NettyRpcConnection.this.channel = newChannel;
					}
				}
			} else if (future.cause() != null) {
				log.error("connect fail", future.cause());
				throw new RuntimeException("connect error", future.cause());
			} else {
				log.error("connect fail,connstr:" + this.getConnStr());
				throw new RuntimeException("connect error");
			}
		} finally {
			if (!isConnected()) {
				future.cancel(true);
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		RpcResponse response = (RpcResponse) msg;
		InvokeFuture.receive(channel, response);
	}

	public void close() throws Throwable {
		connected.set(false);
		if (null != channel) {
			channel.close().awaitUninterruptibly();
			synchronized (channel) {
				channel.notifyAll();
			}
			channel = null;
		}
		if (null != group) {
			group.shutdownGracefully();
		}
	}

	public boolean isConnected() {
		return connected.get();
	}

	public boolean isClosed() {
		return (null == channel) || !channel.isActive() || !channel.isWritable();
	}

	public String getConnStr() {
		return inetAddr.getHostName() + ":" + inetAddr.getPort();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("exceptionCaught", cause);
		ctx.close();
	}

}
