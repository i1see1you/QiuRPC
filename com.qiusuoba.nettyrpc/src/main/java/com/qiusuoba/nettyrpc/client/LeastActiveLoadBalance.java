package com.qiusuoba.nettyrpc.client;  

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qiusuoba.nettyrpc.config.ClientConfig;
import com.qiusuoba.nettyrpc.config.ServiceConfig;
import com.qiusuoba.nettyrpc.connect.IRpcConnection;
import com.qiusuoba.nettyrpc.connect.NettyRpcConnection;

/**
 *最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。
     使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。 
    常见的负载均衡策略有很多，比如：随机，轮循，最少活跃调用等，开发者可以自己扩展实现   
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
public class LeastActiveLoadBalance implements ILoadBlance{
	private static final Log log=LogFactory.getLog(LeastActiveLoadBalance.class);
	/**
	 * 当前并发量
	 */
	private final AtomicInteger curTotalCount = new AtomicInteger(0);
	
	private final static Random RANDOM_NUM=new Random();
	
	//修改curTotalMap需要加锁
	private final ReadWriteLock lockTotalMap             = new ReentrantReadWriteLock();
	
	private final ReadWriteLock lockconnectionMap             = new ReentrantReadWriteLock();
	
	//key代表serviceName,value Map中的key代表连接串,value代表该连接的当前并发数
	private final Map<String,Map<String,AtomicInteger>> curTotalMap = new ConcurrentHashMap<String, Map<String,AtomicInteger>>();
	
	//长连接池
	private final Map<String,IRpcConnection> connectionMap=new ConcurrentHashMap<String, IRpcConnection>();
	
	public int getCurTotalCount()
	{
		return curTotalCount.get();
	}
	
	public IRpcConnection getConnection(String conn)
	{
		IRpcConnection connection = connectionMap.get(conn);
		if (connection==null) {
			try
			{
				lockconnectionMap.writeLock().lock();
				connection = connectionMap.get(conn);
				//双重检查，避免重复创建连接
				if(connection==null)
				{
					try
					{
						connection=new NettyRpcConnection(conn);
						connection.open();
						connection.connect();
						connectionMap.put(conn, connection);
					}
					catch(Throwable e)
					{
						throw new RuntimeException(e);
					}
				}
			}
			finally
			{
				lockconnectionMap.writeLock().unlock();
			}
		}
		return connection;
	}
	
	
	public String getLoadBlance(String serviceName)
	{
		if(curTotalMap.get(serviceName)==null)
		{
			lockTotalMap.writeLock().lock();
			try
			{
				if(curTotalMap.get(serviceName)==null)
				{
					ClientConfig clientConfig = ClientConfig.getInstance();
					ServiceConfig serviceConfig = clientConfig.getService(serviceName);
					Map<String,AtomicInteger> map = new ConcurrentHashMap<String, AtomicInteger>();
					String[] connStrs = serviceConfig.getConnectStr().split(";");
					for(String connStr:connStrs)
					{
						map.put(connStr,new AtomicInteger(0));
					}
					curTotalMap.put(serviceName, map);
				}
			}
			finally
			{
				lockTotalMap.writeLock().unlock();
			}
		}
		String connStr =  getMin(curTotalMap.get(serviceName));
		if(connStr!=null)
		{
			curTotalCount.incrementAndGet();
		}
		else
		{
			throw new RuntimeException("the service have no alive connection,service:"+serviceName);
		}
		return connStr;
	}
	
	public void finishLoadBlance(String serviceName,String connStr)
	{
		curTotalCount.decrementAndGet();
		curTotalMap.get(serviceName).get(connStr).decrementAndGet();
	}
	
	/**
	 * 得到存活的并且tps最少的连接
	 * @param map
	 * @return
	 */
	private String getMin(Map<String,AtomicInteger> map)
	{
		if(map.size()<=0)
		{
			return null;
		}
		String result=null;
		TreeMap<Integer,String> sortedMap=new TreeMap<Integer,String>();
		List<String> zeroResults=new ArrayList<String>();
		for(Entry<String,AtomicInteger> entry:map.entrySet())
		{
			IRpcConnection connection=connectionMap.get(entry.getKey());
			if(connection==null || (connection!=null && connection.isConnected()))
			{
				int cnt=entry.getValue().get();
				if(cnt==0)
				{
					String tmpResult=entry.getKey();
					zeroResults.add(tmpResult);
				}
				else
				{
					sortedMap.put(entry.getValue().get(), entry.getKey());
				}
			}
		}
		int zsize=zeroResults.size();
		if(zsize>0)
		{
			if(zsize==1)
			{
				result=zeroResults.get(0);
			}
			else
			{
				result=zeroResults.get(RANDOM_NUM.nextInt(zsize));
			}
			return result;
		}
		else if(sortedMap.size()>=1)
		{
			result=sortedMap.firstEntry().getValue();
		}
		else
		{
			return null;
		}
		int lessCnt=map.get(result).incrementAndGet();
		int totalCnt = curTotalCount.get();
		if(totalCnt>=10)
		{
			log.warn("the concurrent connection:"+result+",lessCnt:"+lessCnt+",totalCnt:"+totalCnt);
		}
		return result;
	}
}
