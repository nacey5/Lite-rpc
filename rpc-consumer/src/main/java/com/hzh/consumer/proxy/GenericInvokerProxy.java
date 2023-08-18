package com.hzh.consumer.proxy;

import com.hzh.consumer.RpcConsumer;
import com.hzh.provider.registry.RegistryService;

/**
 * @ClassName GenericInvokerProxy
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/18 15:34
 * @Version 0.0.1
 **/
public class GenericInvokerProxy {

    private final RegistryService registryService;
    private final RpcConsumer rpcConsumer;

    public GenericInvokerProxy(RegistryService registryService) {
        this.registryService = registryService;
        this.rpcConsumer = RpcConsumer.getInstance();
    }

    public Object invoke(String serviceName, String methodName, String serviceVersion, long timeout,Class[] paramTypes, Object... args) throws Throwable {
        RpcInvokerProxy invoker = new RpcInvokerProxy(serviceVersion, timeout, registryService);
        return invoker.invokeGeneric(serviceName, methodName, args,paramTypes);
    }
}

