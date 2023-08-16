package com.hzh.consumer;

import com.hzh.provider.registry.RegistryFactory;
import com.hzh.provider.registry.RegistryService;
import com.hzh.provider.registry.RegistryType;
import javassist.util.proxy.MethodHandler;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcReferenceBean implements FactoryBean<Object> {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddr;

    private long timeout;

    private Object object;

    private ProxyType  proxyType = ProxyType.JDK; // 默认为JDK代理

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }


    public void init() throws Exception {
        switch (proxyType) {
            case JDK:
                intiJDK();
                break;
            case JAVASSIST:
                initJavassist();
                break;
            default:
                throw new IllegalArgumentException("Unsupported proxy type: " + proxyType);
        }
    }

    // 使用jdk动态代理
    private void intiJDK() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddr, RegistryType.valueOf(this.registryType));
        this.object = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokerProxy(serviceVersion, timeout, registryService));
    }

    // 使用javassist动态代理
    private void initJavassist() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddr, RegistryType.valueOf(this.registryType));
        this.object = JavassistProxyFactory.getProxy(interfaceClass, new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                RpcInvokerProxy invoker = new RpcInvokerProxy(serviceVersion, timeout, registryService);
                return invoker.invoke(self, thisMethod, args);
            }
        });
    }


    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }

    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setProxyType(ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }
}
