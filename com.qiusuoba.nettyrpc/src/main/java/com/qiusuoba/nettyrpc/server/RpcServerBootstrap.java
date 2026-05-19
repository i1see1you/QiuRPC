package com.qiusuoba.nettyrpc.server;  

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import com.qiusuoba.nettyrpc.config.ServerConfig;
import com.qiusuoba.nettyrpc.protocol.RpcRequestDecode;
import com.qiusuoba.nettyrpc.protocol.RpcResponseEncode;

/**
 *rpc服务实现类，在此开启长连接
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class RpcServerBootstrap implements IRpcServer {
	private Log log = LogFactory.getLog(RpcServerBootstrap.class);
	
	private ServerBootstrap bootstrap = null;
	
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private AtomicBoolean stopped = new AtomicBoolean(false);
	
	private Channel serverChannel;
	
	private ChannelGroup channelGroup;
	
	private int port;
	
	public RpcServerBootstrap(int port) {
		this.port = port;
	}
	
	public void init() {
		log.info("init RpcServerBootstrap...........");
		final ServerConfig serverConfig = new ServerConfig(port);
		channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup(serverConfig.getThreadCnt());
		
		bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_REUSEADDR, true)
			.option(ChannelOption.SO_RCVBUF, 1024 * 128)
			.option(ChannelOption.SO_SNDBUF, 1024 * 128)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					
					int readTimeout = serverConfig.getReadTimeout();
					if (readTimeout > 0) {
						pipeline.addLast("timeout", new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS));
					}
					
					pipeline.addLast("decoder", new RpcRequestDecode());
					pipeline.addLast("encoder", new RpcResponseEncode());
					pipeline.addLast("handler", new NettyRpcServerHandler(channelGroup));
				}
			});
		
		log.info("RpcServerBootstrap init finish");
	}
	
	public void start() {
		if (!checkPortConfig(port)) {
			throw new IllegalStateException("port: " + port + " already in use!");
		}
		
		try {
			ChannelFuture future = bootstrap.bind(new InetSocketAddress(port)).sync();
			serverChannel = future.channel();
			channelGroup.add(serverChannel);
			log.info("QiuRPC server started on port: " + port);
		} catch (InterruptedException e) {
			log.error("start server failed", e);
			throw new RuntimeException("start server failed", e);
		}
	}
		
		try {
			ChannelFuture future = bootstrap.bind(new InetSocketAddress(port)).sync();
			serverChannel = future.channel();
			channelGroup.add(serverChannel);
			log.info("QiuRPC server started on port: " + port);
		} catch (InterruptedException e) {
			log.error("start server failed", e);
			throw new RuntimeException("start server failed", e);
		}
	}

	public void stop() {
		stopped.set(true);
		synchronized (stopped) {
			stopped.notifyAll();
		}
	}
	
	public void close() {
		log.info("closing QiuRPC server...");
		
		if (channelGroup != null) {
			ChannelGroupFuture future = channelGroup.close();
			future.awaitUninterruptibly();
		}
		
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
		
		log.info("QiuRPC server stopped");
	}
	
	public void startAndWait() {
		init();
		start();
		waitForShutdownCommand();
		close();
	}

	public void start(int port) {
		this.port = port;
		startAndWait();
	}

	private void waitForShutdownCommand() {
		synchronized (stopped) {
			while (!stopped.get()) {
				try {
					stopped.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	private boolean checkPortConfig(int listenPort) {
		if (listenPort < 0 || listenPort > 65536) {
			throw new IllegalArgumentException("Invalid start port: " + listenPort);
		}
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(listenPort);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(listenPort);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

}
