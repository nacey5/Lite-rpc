package com.hzh.consumer.proxy;

import com.hzh.consumer.factory.RpcConsumerFactory;
import com.hzh.consumer.enums.ProxyType;
import com.hzh.consumer.factory.CircuitBreakerFactory;
import com.hzh.provider.registry.RegistryFactory;
import com.hzh.rpc.circuitbreaker.CircuitBreaker;
import com.hzh.rpc.common.RpcConsumer;
import com.hzh.rpc.exception.RpcException;
import com.hzh.rpc.exception.errorcode.NetErrorCode;
import com.hzh.rpc.exception.errorcode.SystemErrorCode;
import com.hzh.rpc.register.RegistryService;
import com.hzh.provider.registry.RegistryType;
import com.hzh.rpc.spi.proxy.RpcProxy;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

import java.util.ServiceLoader;

@Slf4j
public class RpcReferenceBean implements FactoryBean<Object> {

    private static final String DIRECT_ADDRESS_FORMAT = "host:port";
    private static final String DIRECT_ADDRESS_DELIMITER = ":";

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddr;

    private long timeout;

    private Object object;

    private String directAddress;

    private ProxyType proxyType = ProxyType.JDK; // 默认为JDK代理


    @Override
    public Object getObject(){
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    //在代理的时候会去调用这个方法，这也是netty的核心逻辑之一
    public void init() throws Exception {
        if (isDirectConnection()) {
            setupDirectConnection();
        } else {
            setupConnectionViaRegistry();
        }
    }

    private void setupConnectionViaRegistry() throws Exception {
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddr, RegistryType.valueOf(this.registryType));
        this.object = createProxy(registryService);
    }

    private void setupDirectConnection() throws Exception {
        String[] parts = this.directAddress.split(DIRECT_ADDRESS_DELIMITER);
        if (parts.length != 2) {
            throw new RpcException(NetErrorCode.HOST_PORT_ERROR,"Invalid directAddress format. Expected format: " + DIRECT_ADDRESS_FORMAT);
        }
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        RpcConsumer instance = RpcConsumerFactory.getInstance();
        instance.setDirectAddress(this.directAddress);
        ChannelFuture future = instance.tryConnect();
        if (!future.isSuccess()) {
            log.error("Failed to directly connect to {}:{}", host, port);
            throw new RpcException(NetErrorCode.CONNECT_ERROR,"Failed to directly connect to " + directAddress);
        }
        log.info("Directly connected to {}:{}", host, port);
        this.object = createProxy(null);
    }

    private Object createProxy(RegistryService registryService) throws Exception {
        ServiceLoader<RpcProxy> loader = ServiceLoader.load(RpcProxy.class);
        for (RpcProxy proxy : loader) {
            if (proxyType.getName().equals(proxy.getType())) {
                CircuitBreaker simpleCircuitBreaker = CircuitBreakerFactory.createCircuitBreaker(CircuitBreakerFactory.BreakerType.SIMPLE, 5, 60000, 5000);
                return proxy.getProxy(interfaceClass, serviceVersion, timeout, registryService, RpcConsumerFactory.getInstance(), simpleCircuitBreaker);
            }
        }
        throw new RpcException(SystemErrorCode.ILLEGAL_ARGUMENT_ERROR,new IllegalArgumentException(),"Unsupported proxy type: " + proxyType);
    }

    private boolean isDirectConnection() {
        return this.directAddress != null && !this.directAddress.isEmpty();
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
