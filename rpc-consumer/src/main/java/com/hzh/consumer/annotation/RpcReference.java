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

    String registryAddress() default "192.168.199.128:2181";

    long timeout() default 5000;

    ProxyType proxyType() default ProxyType.JDK; // 默认为JDK代理

}
