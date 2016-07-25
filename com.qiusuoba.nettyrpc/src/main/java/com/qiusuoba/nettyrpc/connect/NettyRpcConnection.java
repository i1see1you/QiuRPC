package com.qiusuoba.nettyrpc.connect;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

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
public class NettyRpcConnection extends SimpleChannelHandler implements
		IRpcConnection {
	private static final Log log=LogFactory.getLog(NettyRpcConnection.class);
	
	private static final ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1,new NamedTheadFactory("ConnectionHeart"));

	private volatile long lastConnectedTime = System.currentTimeMillis();
	
	private InetSocketAddress inetAddr;

	private volatile Channel channel;

	//是否已经连接的标示，初始化打开和周期检测时会设置该标示
	private volatile AtomicBoolean connected=new AtomicBoolean(false);
	//客户端配置文件
	private static final ClientConfig clientConfig=ClientConfig.getInstance();
	
	private ClientBootstrap bootstrap=null;
	
	//处理超时事件
	private Timer timer=null;
	
	private static final ChannelFactory factory = new NioClientSocketChannelFactory(
			Executors.newCachedThreadPool(),
			Executors.newCachedThreadPool(),clientConfig.getMaxThreadCount());
	
	public NettyRpcConnection(String connStr)
	{
		this.inetAddr = new InetSocketAddress(connStr.split(":")[0],Integer.parseInt(connStr.split(":")[1]));
		initReconnect();
	}

	public NettyRpcConnection(String host, int port) {
		this.inetAddr = new InetSocketAddress(host, port);
		initReconnect();
	}

	public RpcResponse sendRequest(RpcRequest request,boolean async) throws Throwable {
		if (!isConnected() || !channel.isConnected()) {
			throw new RpcException("not connected");
		}
		//如果request已经超时，直接抛弃
		if(System.currentTimeMillis()-request.getAddTime().getTime()>Constants.TIMEOUT_INVOKE_MILLSECOND)
		{
			log.info("request timeout exception");
			throw new RpcException("request timeout exception");
		}
		//异步发送请求
		InvokeFuture invokeFuture=new InvokeFuture(channel,request);
		invokeFuture.send();
		if(async)
		{
			//如果是异步，则封装context
			RpcContext.getContext().setFuture(new FutureAdapter<Object>(invokeFuture));
			return new RpcResponse();
		}
		else
		{
			//如果是同步，则阻塞调用get方法
			RpcContext.getContext().setFuture(null);
			return invokeFuture.get(Constants.TIMEOUT_INVOKE_MILLSECOND);
		}
	}
	
	/**
	 * 初始化连接
	 */
	public void open() throws Throwable 
	{
		open(true);
	}
	
	/**
	 * @param connectStatus 心跳检测状态是否正常
	 * @throws Throwable
	 */
	public void open(boolean connectStatus) throws Throwable {
		log.info("open start,"+getConnStr());
		bootstrap = new ClientBootstrap(factory);
		timer = new HashedWheelTimer();
		{
			bootstrap.setOption("tcpNoDelay", Boolean.parseBoolean(clientConfig.getTcpNoDelay()));
			bootstrap.setOption("reuseAddress", Boolean.parseBoolean(clientConfig.getReuseAddress()));
			bootstrap.setOption("SO_RCVBUF",1024*128);
			bootstrap.setOption("SO_SNDBUF",1024*128);
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() {
					ChannelPipeline pipeline = Channels.pipeline();
					int readTimeout = clientConfig.getReadTimeout();
					if (readTimeout > 0) {
						pipeline.addLast("timeout", new ReadTimeoutHandler(timer,
								readTimeout, TimeUnit.MILLISECONDS));
					}
					pipeline.addLast("decoder", new RpcRequestEncode());
					pipeline.addLast("encoder", new RpcResponseDecode());
					pipeline.addLast("handler", NettyRpcConnection.this);
					return pipeline;
				}
			});
		}
		connected.set(connectStatus);
		log.info("open finish,"+getConnStr());
	}
	
	public void initReconnect()
	{
		Runnable connectStatusCheckCommand =  new Runnable() {
			@Override
			public void run() {
				try
				{
				 if(!isConnected())
				 {
					 try {
						open(false);
						connect();
						connected.set(true);
					} catch (Throwable e) {
						log.error("connect open error,conn:"+getConnStr());
					}
				 }
				 if (isConnected() && isClosed()) {
					 try {
						 connect();
					} catch (Throwable e) {
						log.error("connect error,conn:"+getConnStr());
					}
                 }  
				 if(isConnected() && !isClosed())
				 {
                     lastConnectedTime = System.currentTimeMillis();
                 }
				 if (System.currentTimeMillis() - lastConnectedTime > Constants.TIMEOUT_HEARTBEAT_MILLSECOND){
                     if (connected.get()==true){
                    	 connected.set(false);
                    	 log.error("connected error,conn:"+getConnStr());
                         return ;
                     }
                 }
				}
				catch(Throwable e)
				{
					log.error("connectStatusCheckCommand error");
				}
			}
		};
		//1秒发送一次心跳
		executorService.scheduleAtFixedRate(connectStatusCheckCommand, 1000, 1000, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 尝试连接
	 */
	public void connect()
	{
        ChannelFuture future = bootstrap.connect(inetAddr);
        try{
            boolean ret = future.awaitUninterruptibly(Constants.TIMEOUT_CONNECTION_MILLSECOND, TimeUnit.MILLISECONDS);
            if (ret && future.isSuccess()) {
                Channel newChannel = future.getChannel();
                newChannel.setInterestOps(Channel.OP_READ_WRITE);
                try {
                    // 关闭旧的连接
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
            } else if (future.getCause() != null) {
            	log.error("connect fail",future.getCause());
            	throw new RuntimeException("connect error",future.getCause());
            } else {
            	log.error("connect fail,connstr:"+this.getConnStr());
            	throw new RuntimeException("connect error");
            }
        }finally{
            if (! isConnected()) {
                future.cancel();
            }
        }
	}

	/**
	 * 客户端接受并处理消息
	 */
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		RpcResponse response = (RpcResponse) e.getMessage();
		InvokeFuture.receive(channel, response);
	}
	

	public void close() throws Throwable {
		connected.set(false);
		if (null != timer) {
			timer.stop();
			timer = null;
		}
		if (null != channel) {
			channel.close().awaitUninterruptibly();
			channel.getFactory().releaseExternalResources();

			synchronized (channel) {
				channel.notifyAll();
			}
			channel = null;
		}
	}

	public boolean isConnected() {
		return connected.get();
	}

	public boolean isClosed() {
		return (null == channel) || !channel.isConnected()
				|| !channel.isReadable() || !channel.isWritable();
	}

	public String getConnStr() {
		return inetAddr.getHostName()+":"+inetAddr.getPort();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		super.exceptionCaught(ctx, e);
		log.error("exceptionCaught",e.getCause());
	}

}
