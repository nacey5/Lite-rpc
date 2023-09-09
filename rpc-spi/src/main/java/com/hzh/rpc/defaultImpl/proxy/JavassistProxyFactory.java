package com.hzh.rpc.defaultImpl.proxy;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JavassistProxyFactory
 * <p>
 * 动态代理工厂
 * <p>
 * 通过javassist动态代理生成代理类
 */
public class JavassistProxyFactory {

    private static final Map<String, Object> PROXY_CACHE = new ConcurrentHashMap<>();

    public static Object getProxy(Class<?> interfaceClass, String group, MethodHandler handler) throws Exception {
        // 使用接口类和分组属性组合起来作为键来检查缓存
        String key = interfaceClass.getName() + "#" + group;
        if (PROXY_CACHE.containsKey(key)) {
            return PROXY_CACHE.get(key);
        }

        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[]{interfaceClass});
        Class<?> proxyClass = factory.createClass();
        Object proxyInstance = proxyClass.newInstance();
        ((javassist.util.proxy.Proxy) proxyInstance).setHandler(handler);

        // 将新创建的代理对象存入缓存，使用接口类和分组属性组合起来作为键
        PROXY_CACHE.put(key, proxyInstance);

        return proxyInstance;
    }

}
