package com.hzh.rpc.defaultImpl.proxy;

import com.hzh.rpc.common.RpcConsumer;
import com.hzh.rpc.proxy.RpcInvokerProxy;
import com.hzh.rpc.register.RegistryService;
import com.hzh.rpc.spi.proxy.RpcProxy;
import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;

/**
 * @ClassName JavassistProxy
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/23 18:15
 * @Version 0.0.1
 **/
public class JavassistProxy implements RpcProxy {

    @Override
    public String getType() {
        return "JAVASSIST";
    }

    @Override
    public Object getProxy(Class<?> interfaceClass, String serviceVersion, long timeout, RegistryService registryService, RpcConsumer rpcConsumer) throws Exception {
        return JavassistProxyFactory.getProxy(interfaceClass, new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                RpcInvokerProxy invoker = new RpcInvokerProxy(serviceVersion, timeout, registryService,rpcConsumer);
                return invoker.invoke(self, thisMethod, args);
            }
        });
    }
}
