package com.hzh.rpc.spi.proxy;

import com.hzh.rpc.circuitbreaker.CircuitBreaker;
import com.hzh.rpc.common.RpcConsumer;
import com.hzh.rpc.register.RegistryService;

/**
 * @ClassName RpcProxy
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/23 17:57
 * @Version 0.0.1
 **/
public interface RpcProxy {
    Object getProxy(Class<?> interfaceClass, String serviceVersion, long timeout,
                    RegistryService registryService, RpcConsumer rpcConsumer, CircuitBreaker circuitBreaker) throws Exception;

    String getType();
}
