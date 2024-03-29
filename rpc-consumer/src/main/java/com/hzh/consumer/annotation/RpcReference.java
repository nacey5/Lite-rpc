package com.hzh.consumer.annotation;

import com.hzh.consumer.enums.ProxyType;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Autowired
public @interface RpcReference {

    String serviceVersion() default "1.0";

    String registryType() default "ZOOKEEPER";

    String registryAddress() default "192.168.1.101:2181";

    String group() default "";

    long timeout() default 5000;

    ProxyType proxyType() default ProxyType.JDK; // 默认为JDK代理

    String directAddress() default ""; // 默认为空，表示不使用直连

}

