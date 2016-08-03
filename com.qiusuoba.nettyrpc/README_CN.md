# QiuRPC
QiuRPC 参考手册


RPC常见功能<br/>
一个通用的网络RPC框架，它应该包括如下元素：<br/>
<br/>
1.具有服务的分层设计，借鉴Future/Service/Filter概念<br/>
2.具有网络的分层设计，区分协议层、数据层、传输层、连接层<br/>
3.独立的可适配的codec层，可以灵活增加HTTP，Memcache，Redis，MySQL/JDBC，Thrift等协议的支持。<br/>
4.将多年各种远程调用High availability的经验融入在实现中，如负载均衡，failover，多副本策略，开关降级等。<br/>
5.通用的远程调用实现，采用async方式来减少业务服务的开销，并通过future分离远程调用与数据流程的关注。<br/>
6.具有状态查看及统计功能<br/>
7.当然，最终要的是，具备以下通用的远程容错处理能力，超时、重试、负载均衡、failover……<br/>
<br/>
QiuRPC特点<br/>
QiuRPC是一个采用JAVA实现的小巧的RPC框架，一共3K多行代码，实现了RPC的基本功能，开发者也可以自定义扩展，可以供大家学习探讨或者在小项目中使用，目前QiuRPC具有如下特点：<br/>
1.	服务端基于注解，启动时自动扫描所有RPC实现，基本零配置<br/>
2.	客户端实现Filter机制，可以自定义Filter<br/>
3.	基于netty的Reactor  IO多路复用网络模型<br/>
4.	数据层提供protobuff和hessian的实现，可以扩展ISerializer接口自定义实现其他<br/>
5.	负载均衡算法采用最少活跃调用数算法，可以扩展ILoadBlance接口自定义实现其他<br/>
6.	客户端支持服务的同步或异步调用<br/>

<br/>
系统改进点<br/>
1.	增加注册中心功能，在大项目中，一个项目可能依赖成百上千个服务，如果基于配置文件直接指定服务地址会增加维护成本，需要引入注册中心<br/>
2.	目前用的是反射和java代理实现的服务端存根和客户端代理，为了提高性能，可以把这些用javassit，asm等java字节码工具实现<br/>
3.	增加一些监控功能，为了增强服务的稳定性和服务的可控性，监控功能是不可或缺的<br/>
4.	目前应用协议采用的是最简单的协议，仅仅一个魔数+序列化的实体，这些需要增强，比如增加版本号以解决向前兼容性<br/>
5.	增加High availability的一些手段，目前只有负载均衡，其他的比如failover，多副本策略，开关降级等，过载保护等需要自己实现<br/>
6.	目前只支持java语言，后续可能会增加其他语言的支持<br/>
<br/>
参考例子<br/>

<pre>
1.	The service inteface:

public interface IServer1 {
	
	public Message echoMsg(String msg);
	
}

2.	The service inteface implement:

@ServiceAnnotation(name="myserver1")
public class MyServer1 implements IServer1{
	private static final Log log=LogFactory.getLog(MyServer1.class);

	@Override
	public Message echoMsg(String msg) {
		Message result=new Message();
		result.setMsg(msg);
		result.setData(new Date());
		return result;
	}

}

3.	The service main class:

public static void main(String[] args) {
		RpcServerBootstrap bootstrap=new RpcServerBootstrap();
		bootstrap.start(8080);
	}


4.	The client main class:


public class Client1 {
	
	public static void main(String[] args) {
		try {
			final IServer1 server1=RpcClientProxy.proxy(IServer1.class,"server1" , "myserver1");
			long startMillis=System.currentTimeMillis();
			for(int i=0;i<10000;i++)
			{
				final int f_i=i;
				send(server1,"hello"+f_i);
			}
			long endMillis=System.currentTimeMillis();
			System.out.println("spend time:"+(endMillis-startMillis));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static void send(IServer1 server1,int f_i)
	{
		Message msg = null;
		try
		{
			//Client config file used async="true",so we used future to get the async result,
			//if configured async="false",used msg=server1.echoMsg(f_i) instead
			server1.echoMsg(f_i);
			Future<Message> future = RpcContext.getContext().getFuture();
			msg=future.get();
			System.out.println("msg:"+msg.getMsg()+","+msg.getData());
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}

5.  The config file at client side:


&lt;application maxThreadCount="100"&gt;
	&lt;service name="server1" connectStr="127.0.0.1:9090;127.0.0.1:8080" maxConnection="100" async="true"&gt;&lt;/service&gt;
&lt;/application&gt;


</pre>
