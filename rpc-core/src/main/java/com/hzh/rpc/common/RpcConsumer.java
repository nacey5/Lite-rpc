package com.hzh.rpc.common;

import com.hzh.rpc.protocol.MiniRpcProtocol;
import com.hzh.rpc.register.RegistryService;
import io.netty.channel.ChannelFuture;

/**
 * @ClassName RpcConsumer
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/23 18:29
 * @Version 0.0.1
 **/
public interface RpcConsumer {
    void sendRequest(MiniRpcProtocol<?> protocol, RegistryService registryService) throws Exception;

    Object invokeGeneric(String serviceName, String methodName, String serviceVersion, long timeout, Class[] paramTypes, Object... args) throws Throwable;

    ChannelFuture tryConnect() throws InterruptedException;

    void setDirectAddress(String directAddress);
}
