package com.hzh.consumer.proxy;

import com.hzh.consumer.RpcConsumer;
import com.hzh.provider.registry.RegistryService;
import com.hzh.rpc.common.MiniRpcFuture;
import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.common.MiniRpcRequestHolder;
import com.hzh.rpc.common.MiniRpcResponse;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import com.hzh.rpc.protocol.MsgHeader;
import com.hzh.rpc.protocol.MsgType;
import com.hzh.rpc.protocol.ProtocolConstants;
import com.hzh.rpc.serialization.SerializationTypeEnum;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class RpcInvokerProxy implements InvocationHandler {
    public static final ThreadLocal<MiniRpcProtocol<MiniRpcRequest>> CURRENT_REQUEST = new ThreadLocal<>();
    private final String serviceVersion;
    private final long timeout;
    private final RegistryService registryService;
    private final RpcConsumer rpcConsumer; // 添加RpcConsumer作为成员变量

    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
        this.rpcConsumer = RpcConsumer.getInstance(); // 在构造函数中初始化RpcConsumer
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MiniRpcProtocol<MiniRpcRequest> protocol = createProtocol(method, args);
        MiniRpcFuture<MiniRpcResponse> future = sendRequest(protocol);
        return future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
    }

    private MiniRpcProtocol<MiniRpcRequest> createProtocol(Method method, Object[] args) {
        MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
        protocol.setHeader(createHeader());
        protocol.setBody(createRequest(method, args));
        return protocol;
    }

    private MsgHeader createHeader() {
        //暂时先不引入这个ThreadLocal，因为consumer对象是复用的，关闭consumer再进行remove会导致内存泄漏
//        CURRENT_REQUEST.set(protocol);
        MsgHeader header = new MsgHeader();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(MiniRpcRequestHolder.REQUEST_ID_GEN.incrementAndGet());
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        return header;
    }

    private MiniRpcRequest createRequest(Method method, Object[] args) {
        MiniRpcRequest request = new MiniRpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParams(args);
        return request;
    }

    private MiniRpcFuture<MiniRpcResponse> sendRequest(MiniRpcProtocol<MiniRpcRequest> protocol) throws Exception {
        MiniRpcFuture<MiniRpcResponse> future = new MiniRpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        MiniRpcRequestHolder.REQUEST_MAP.put(protocol.getHeader().getRequestId(), future);
        rpcConsumer.sendRequest(protocol, this.registryService);
        return future;
    }

}
