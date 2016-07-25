package com.qiusuoba.nettyrpc.util;  

import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;

import com.qiusuoba.nettyrpc.protocol.RpcRequest;
import com.qiusuoba.nettyrpc.protocol.RpcResponse;

/**
 *protostuff的schama缓存 
 *@Author:caimin 
 *@Since:2015年9月17日  
 *@Version:
 */
public class SchemaCache {

	private static final Map<String, Schema<?>> SCHEMA_CACHE = new LRUMap<String, Schema<?>>(
			4096);

	@SuppressWarnings("unchecked")
	public static <T> Schema<T> getSchema(Class<T> clazz) {
		String className = clazz.getName();
		Schema<T> schema = (Schema<T>) SCHEMA_CACHE.get(className);
		if (null != schema) {
			return schema;
		}
		synchronized (SCHEMA_CACHE) {
			if (null == SCHEMA_CACHE.get(className)) {
				schema = RuntimeSchema.getSchema(clazz);
				SCHEMA_CACHE.put(className, schema);
				return schema;
			} else {
				return (Schema<T>) SCHEMA_CACHE.get(className);
			}
		}
	}

	public static Schema<RpcRequest> getSchema(RpcRequest request) {
		Schema<RpcRequest> schema = getSchema(RpcRequest.class);
		Object[] parameters = request.getParameters();
		if (null != parameters && parameters.length > 0) {
			for (Object param : parameters) {
				if (null != param) {
					getSchema(param.getClass());
				}
			}
		}
		return schema;
	}

	public static Schema<RpcResponse> getSchema(RpcResponse response) {
		Schema<RpcResponse> schema = getSchema(RpcResponse.class);
		if (response.getException() != null) {
			getSchema(response.getException().getClass());
		}
		if (response.getResult() != null) {
			getSchema(response.getResult().getClass());
		}
		return schema;
	}

}
