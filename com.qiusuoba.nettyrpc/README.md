# QiuRPC
QiuRPCHelper &nbsp;&nbsp;<a href="/com.qiusuoba.nettyrpc/README_CN.md">QiuRPC中文参考手册<a>


<b>RPC common features:</b><br/>
1.Service layered design,such as Future/Service/Filter etc.<br/>
2.Network communication layered design,such as protocol layer/data layer/transport layer/connection layer<br/>
3.The codec layer can be adapted,developers can add custom protocol,such as HTTP/Memcache/Redis/MySQL/JDBC/Thrift<br/>
4.With high availability,such as load-balancing/failover/clustering/automatically switch capabilities<br/>
5.Provide sync and async invoke,functions that do not return a result or don't care the return result can be invoked asynchronously so the client is not blocked until the server has finished <br/>
6.With monitor and statistical functions<br/>
7.Most important is reliable and remote fault tolerance capacity, such as timeout/retry/load balance/failover etc.<br/>
<br/>

<b>QiuRPC features:</b><br/>

QiuRPC is a smart framework implemented by java langurage,with  only three thousand lines code.Provide common functions of RPC, developers can also custom extend, Open Source for some one to learn and discuss or used in  some java project, QiuRPC has the following features current:<br/>
1.	The server side based annotation, automatically scan all the RPC implementation on application started, can be zero configuration<br/>
2.	The client side  has the Filter inteface, you can implement this inteface to customize <br/>
3.	IO Reactor multiplex network model based on netty<br/>
4.	Data serialize layer provides protobuff and Hessian implementation, you can implement the ISerializer interface to customize other<br/>
5.      Load balancing algorithm using the smaller number of current active algorithm, you can implement the ILoadBlance interface to customize other<br/>
6.	The client can invoke method over sync or async  <br/>

<br/>
<b>Non-features:</b><br/>
1.      The registration center, in large projects, a project may rely on hundreds and thoudsands of services, if based on the configuration file directly,the service address will increase  maintenance costs,  the registration center can avoid this<br/>
2.	The server proxy and client proxy are provied by java proxy currently,to improve performance,we can provied proxy by Java byte code tools,such as asm,javassit<br/>
3.	The monitor and statistics , in order to enhance the stability and the controllability of the service, monitoring and statistics function is indispensable<br/>
4.	At present, the protocol is the most simple protocol, only a magic number at the head and serialization entity next, these need to be enhanced, such as an increase version to keep the forward compatibility<br/>
5.	The reliability can be enhanced,current with only loadblance strategy, such as timeout/retry/failover etc can be added<br/>
6.	QiuRPC is supported only java language currently,we can support other languages in future. 
<br/>

Example:<br/>

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
