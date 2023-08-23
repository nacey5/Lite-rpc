package com.hzh.consumer.proxy;

import com.hzh.consumer.RpcConsumerFactory;
import com.hzh.consumer.enums.ProxyType;
import com.hzh.provider.registry.RegistryFactory;
import com.hzh.rpc.defaultImpl.proxy.JavassistProxyFactory;
import com.hzh.rpc.proxy.RpcInvokerProxy;
import com.hzh.rpc.register.RegistryService;
import com.hzh.provider.registry.RegistryType;
import com.hzh.rpc.spi.proxy.RpcProxy;
import javassist.util.proxy.MethodHandler;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ServiceLoader;

public class RpcReferenceBean implements FactoryBean<Object> {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddr;

    private long timeout;

    private Object object;

    private ProxyType proxyType = ProxyType.JDK; // 默认为JDK代理

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }


    public void init() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddr, RegistryType.valueOf(this.registryType));
        ServiceLoader<RpcProxy> loader = ServiceLoader.load(RpcProxy.class);
        for (RpcProxy proxy : loader) {
            if (proxyType.getName().equals(proxy.getType())) {
                this.object = proxy.getProxy(interfaceClass, serviceVersion, timeout, registryService, RpcConsumerFactory.getInstance());
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported proxy type: " + proxyType);
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
