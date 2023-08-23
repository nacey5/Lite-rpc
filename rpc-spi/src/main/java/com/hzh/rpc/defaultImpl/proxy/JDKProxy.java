package com.hzh.rpc.defaultImpl.proxy;

import com.hzh.rpc.common.RpcConsumer;
import com.hzh.rpc.proxy.RpcInvokerProxy;
import com.hzh.rpc.register.RegistryService;
import com.hzh.rpc.spi.proxy.RpcProxy;

import java.lang.reflect.Proxy;

/**
 * @ClassName JDKProxy
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/23 18:13
 * @Version 0.0.1
 **/
public class JDKProxy implements RpcProxy {

    @Override
    public Object getProxy(Class<?> interfaceClass, String serviceVersion, long timeout, RegistryService registryService, RpcConsumer rpcConsumer) {
        return Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokerProxy(serviceVersion, timeout, registryService,rpcConsumer)
        );
    }

    @Override
    public String getType() {
        return "JDK";
    }
}

