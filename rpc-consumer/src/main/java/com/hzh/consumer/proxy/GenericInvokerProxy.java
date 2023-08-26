package com.hzh.consumer.proxy;

import com.hzh.consumer.factory.RpcConsumerFactory;
import com.hzh.rpc.circuitbreaker.CircuitBreaker;
import com.hzh.rpc.proxy.RpcInvokerProxy;
import com.hzh.rpc.register.RegistryService;

/**
 * @ClassName GenericInvokerProxy
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/18 15:34
 * @Version 0.0.1
 **/
public class GenericInvokerProxy {

    private final RegistryService registryService;

    public GenericInvokerProxy(RegistryService registryService) {
        this.registryService = registryService;
    }

    public Object invoke(String serviceName, String methodName, String serviceVersion, long timeout, Class[] paramTypes, CircuitBreaker circuitBreaker, Object... args) throws Throwable {
        RpcInvokerProxy invoker = new RpcInvokerProxy(serviceVersion, timeout, registryService,RpcConsumerFactory.getInstance(),circuitBreaker);
        return invoker.invokeGeneric(serviceName, methodName, args,paramTypes);
    }
}

