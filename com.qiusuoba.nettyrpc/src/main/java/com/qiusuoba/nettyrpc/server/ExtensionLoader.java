package com.qiusuoba.nettyrpc.server;  

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qiusuoba.nettyrpc.annotation.ServiceAnnotation;
import com.qiusuoba.nettyrpc.common.Constants;
import com.qiusuoba.nettyrpc.util.ClassUtil;
import com.qiusuoba.nettyrpc.util.FileUtil;

/**
 *加载所有的服务   
 *@Author:caimin 
 *@Since:2015年9月17日  
 *@Version:
 */
public class ExtensionLoader {
	 
	private static final Log log=LogFactory.getLog(ExtensionLoader.class);
	//服务实体缓存
	private static final ConcurrentMap<String, Object> cachedInstances = new ConcurrentHashMap<String, Object>();

	/**
	 * 标示是否已经初始化
	 */
	private static volatile boolean isInit=false;
	
	public static void init()
	{
		init(null);
	}
	
	public static void init(String path)
	{
		if(isInit==false)
		{
			isInit=true;
			log.info("init root path:"+path);
			try {
				scan(path);
			} catch (Exception e) {
				log.error("init error", e);
			}
		}
		else
		{
			log.warn("the scan have already inited");
		}
	}
	
	public static Object getProxy(String serviceName)
	{
		if(isInit==false)
		{
			init();
		}
		return cachedInstances.get(serviceName);
	}
	
	/**
	 * scan jars create ContractInfo
	 * @param path
	 * @param classLoader
	 * @return
	 * @throws Exception
	 */
	private static void scan(String path) throws Exception {
		log.info("begin scan jar from path:" + path);
		List<String> jarPathList = null;
		if(path==null)
		{
			String classpath=System.getProperty("java.class.path");
			String[] paths=classpath.split(Constants.PATH_SEPARATOR);
			jarPathList = FileUtil.getFirstPath(paths);
		}
		else
		{
			jarPathList = FileUtil.getFirstPath(path);
		}
		if(jarPathList == null) {
		    throw new Exception("no jar fonded from path: " + path);
		}

		for (String jpath : jarPathList) {
		    Set<Class> clsSet = new HashSet<Class>();
		    if(jpath.endsWith(".class"))
		    {
		    	 // 添加到classes
		    	String className=jpath.substring(0,jpath.length()-6).replace(Constants.FILE_SEPARATOR, ".");
		    	try
		    	{
		            Class c = Thread.currentThread().getContextClassLoader().loadClass(className);
		            clsSet.add(c);
		    	}
		    	catch(Exception e)
		    	{
		    		log.error("class not found,class:"+jpath,e);
		    	}
		    }
		    else
		    {
			    try {
			   	 	clsSet = ClassUtil.getClassFromJar(jpath);
			    } catch (Exception ex) {
			   	 	log.error("getClassFromJar",ex);
			    }
		    }
		    if (clsSet == null) {
		        continue;
		    }
		    
		    for (Class<?> cls : clsSet) {
		        try {
		         ServiceAnnotation behavior = cls.getAnnotation(ServiceAnnotation.class);
		       	 if(behavior != null) {
		       		 Object instance=cls.newInstance();
		       		 String serviceName=behavior.name();
		       		 if(serviceName==null || serviceName.equals(""))
		       		 {
		       			serviceName=cls.getSimpleName();
		       		 }
		       		 cachedInstances.put(serviceName, instance);
		       	 }
		        } catch (Exception ex) {
		        	log.error("",ex);
		        }
		    }
		}
		log.info("finish scan jar");
	}
	
	
	 private static ClassLoader findClassLoader() {
	        return  ExtensionLoader.class.getClassLoader();
	 }
}
