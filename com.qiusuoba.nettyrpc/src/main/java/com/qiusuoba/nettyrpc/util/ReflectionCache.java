package com.qiusuoba.nettyrpc.util;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 反射缓存
 *@Author:caimin 
 *@Since:2015年9月17日  
 *@Version:
 */
public class ReflectionCache {
	private static final Map<String, Class<?>> PRIMITIVE_CLASS = new LRUMap<String, Class<?>>(1024);

	private static final Map<String, Class<?>> CLASS_CACHE = new LRUMap<String, Class<?>>(
			1024);

	private static final Map<String, Method> METHOD_CACHE = new LRUMap<String, Method>(
			1024);
	static {
		PRIMITIVE_CLASS.put("boolean", boolean.class);
		PRIMITIVE_CLASS.put("byte", byte.class);
		PRIMITIVE_CLASS.put("short", short.class);
		PRIMITIVE_CLASS.put("int", int.class);
		PRIMITIVE_CLASS.put("long", long.class);
		PRIMITIVE_CLASS.put("long", long.class);
		PRIMITIVE_CLASS.put("float", float.class);
		PRIMITIVE_CLASS.put("double", double.class);
		PRIMITIVE_CLASS.put("void", void.class);

		CLASS_CACHE.putAll(PRIMITIVE_CLASS);
	}

	public static Class<?> getClass(String className)
			throws ClassNotFoundException {
		Class<?> clazz = CLASS_CACHE.get(className);
		if (null != clazz) {
			return clazz;
		}
		synchronized (CLASS_CACHE) {
			if (null == CLASS_CACHE.get(className)) {
				clazz = PRIMITIVE_CLASS.get(className);
				if (null == clazz) {
					clazz = Class.forName(className);
				}
				CLASS_CACHE.put(className, clazz);
				return clazz;
			} else {
				return CLASS_CACHE.get(className);
			}
		}
	}

	public static Method getMethod(String className, String methodName,
			String[] parameterTypes) throws ClassNotFoundException,
			SecurityException, NoSuchMethodException {
		String key = className + "-" + methodName + "-"
				+ join(parameterTypes, ";");
		Method method = METHOD_CACHE.get(key);
		if (null != method) {
			return method;
		}
		synchronized (METHOD_CACHE) {
			if (null == METHOD_CACHE.get(key)) {
				Class<?> clazz = getClass(className);
				Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
				for (int i = 0; i < parameterClasses.length; i++) {
					parameterClasses[i] = getClass(parameterTypes[i]);
				}

				method = clazz.getMethod(methodName, parameterClasses);
				METHOD_CACHE.put(key, method);
				return method;
			} else {
				return METHOD_CACHE.get(key);
			}
		}
	}

	private static String join(String[] strs, String seperator) {
		if (null == strs || 0 == strs.length) {
			return "";
		}
		StringBuilder sb = new StringBuilder(1024);
		sb.append(strs[0]);
		for (int i = 1; i < strs.length; i++) {
			sb.append(seperator).append(strs[i]);
		}
		return sb.toString();
	}
}
