package com.qiusuoba.nettyrpc.annotation;  

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *标准rcp服务的注解,name如果不写默认的是类名 
 *@Author:caimin 
 *@Since:2015年9月10日  
 *@Version:
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceAnnotation {
    /**
     * 缺省服务名
     */
	String name() default "";
}
