package com.qiusuoba.nettyrpc.server;  

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.qiusuoba.nettyrpc.config.ServerConfig;
import com.qiusuoba.nettyrpc.protocol.RpcRequestDecode;
import com.qiusuoba.nettyrpc.protocol.RpcResponseEncode;

/**
 *rpc服务实现类，在此开启长连接
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class RpcServerBootstrap implements IRpcServer{
	private Log log=LogFactory.getLog(RpcServerBootstrap.class);
	
	private ServerBootstrap bootstrap = null;
	
	private AtomicBoolean stopped = new AtomicBoolean(false);
	
	//处理超时事件
	private Timer timer=null;
	
	private void initHttpBootstrap(int myport) {
		log.info("initHttpBootstrap...........");
		final ServerConfig serverConfig=new ServerConfig(myport);
		final ChannelGroup channelGroup = new DefaultChannelGroup(getClass().getName());
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool(),serverConfig.getThreadCnt()));
		//设置常见参数
		bootstrap.setOption("tcpNoDelay","true");
		bootstrap.setOption("reuseAddress", "true");
		bootstrap.setOption("SO_RCVBUF",1024*128);
		bootstrap.setOption("SO_SNDBUF",1024*128);
		timer = new HashedWheelTimer();
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

				int readTimeout = serverConfig.getReadTimeout();
				if (readTimeout > 0) {
					pipeline.addLast("timeout", new ReadTimeoutHandler(timer,
							readTimeout, TimeUnit.MILLISECONDS));
				}

				pipeline.addLast("decoder", new RpcRequestDecode());
				pipeline.addLast("encoder", new RpcResponseEncode());
				pipeline.addLast("handler", new NettyRpcServerHandler(channelGroup));

				return pipeline;
			}
		});
		
		int port=serverConfig.getPort();
		if (!checkPortConfig(port)) {
			throw new IllegalStateException("port: " + port + " already in use!");
		}

		Channel channel = bootstrap.bind(new InetSocketAddress(port));
		channelGroup.add(channel);
		log.info("QiuRPC server started");

		waitForShutdownCommand();
		ChannelGroupFuture future = channelGroup.close();
		future.awaitUninterruptibly();
		bootstrap.releaseExternalResources();
		timer.stop();
		timer = null;

		log.info("QiuRPC server stoped");

	}
	
	public void start(int port) {
		ExtensionLoader.init();
		initHttpBootstrap(port);
	}

	public void stop() {
		stopped.set(true);
		synchronized (stopped) {
			stopped.notifyAll();
		}
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
			throw new IllegalArgumentException("Invalid start port: "
					+ listenPort);
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
