package com.hzh.consumer.proxy;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * JavassistProxyFactory
 * <p>
 * 动态代理工厂
 * <p>
 * 通过javassist动态代理生成代理类
 */
public class JavassistProxyFactory {
    public static Object getProxy(Class<?> interfaceClass, MethodHandler handler) throws Exception {
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[]{interfaceClass});
        Class<?> proxyClass = factory.createClass();
        Object proxyInstance = proxyClass.newInstance();
        ((javassist.util.proxy.Proxy) proxyInstance).setHandler(handler);
        return proxyInstance;
    }
}
