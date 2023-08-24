package com.hzh.consumer.proxy;

import com.hzh.consumer.RpcConsumerFactory;
import com.hzh.consumer.enums.ProxyType;
import com.hzh.provider.registry.RegistryFactory;
import com.hzh.rpc.common.RpcConsumer;
import com.hzh.rpc.defaultImpl.proxy.JavassistProxyFactory;
import com.hzh.rpc.proxy.RpcInvokerProxy;
import com.hzh.rpc.register.RegistryService;
import com.hzh.provider.registry.RegistryType;
import com.hzh.rpc.spi.proxy.RpcProxy;
import io.netty.channel.ChannelFuture;
import javassist.util.proxy.MethodHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ServiceLoader;

@Slf4j
public class RpcReferenceBean implements FactoryBean<Object> {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddr;

    private long timeout;

    private Object object;

    private String directAddress;

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
        // 检查是否设置了直连地址
        if (this.directAddress != null && !this.directAddress.isEmpty()) {
            // 使用直连地址进行连接
            String[] parts = this.directAddress.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid directAddress format. Expected format: host:port");
            }
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            // 使用Netty直接连接到host和port
            RpcConsumer instance = RpcConsumerFactory.getInstance();
            instance.setDirectAddress(this.directAddress);
            ChannelFuture future = instance.tryConnect();
            if (future.isSuccess()) {
                log.info("Directly connected to {}:{}", host, port);
            } else {
                log.error("Failed to directly connect to {}:{}", host, port);
                throw new RuntimeException("Failed to directly connect to " + directAddress);
            }
            ServiceLoader<RpcProxy> loader = ServiceLoader.load(RpcProxy.class);
            for (RpcProxy proxy : loader) {
                if (proxyType.getName().equals(proxy.getType())) {
                    this.object = proxy.getProxy(interfaceClass, serviceVersion, timeout, null,instance);
                    return;
                }
            }
        } else {
            // 使用注册中心进行连接
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

    public void setDirectAddress(String directAddress) {
        this.directAddress = directAddress;
    }
}
