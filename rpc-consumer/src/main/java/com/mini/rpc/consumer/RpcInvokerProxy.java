package com.mini.rpc.consumer;

import com.mini.rpc.common.MiniRpcFuture;
import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.MiniRpcRequestHolder;
import com.mini.rpc.common.MiniRpcResponse;
import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.protocol.MsgHeader;
import com.mini.rpc.protocol.MsgType;
import com.mini.rpc.protocol.ProtocolConstants;
import com.mini.rpc.provider.registry.RegistryService;
import com.mini.rpc.serialization.SerializationTypeEnum;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcInvokerProxy implements InvocationHandler {

    private final String serviceVersion;
    private final long timeout;
    private final RegistryService registryService;
    private final RpcConsumer rpcConsumer;

    private static final int MAX_RETRIES = 3;// 定义重试次数的常数
    private static final int RETRY_DELAY_MS = 1000; // 定义重试间隔时间的常数

    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
        this.rpcConsumer = new RpcConsumer();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int retryCount = 0;
        while (true) {
            try {
                MiniRpcProtocol<MiniRpcRequest> protocol = createProtocol(method, args);
                // TODO hold request by ThreadLocal
                return sendRpcRequest(protocol);
            } catch (Exception e) {
                if (shouldRetry(e) && retryCount < MAX_RETRIES) {
                    retryCount++;
                    // 此处添加延迟
                    Thread.sleep(RETRY_DELAY_MS * retryCount);
                    continue;
                }
                throw e;  // 如果达到最大重试次数或异常不应该重试，抛出异常
            }
        }

    }

    private boolean shouldRetry(Exception e) {
        // 如果是网络异常，重试
        if (e instanceof java.net.SocketTimeoutException ||
                e instanceof java.net.ConnectException) {
            return true;
        }
        // 如果是业务异常，不重试
        if (e instanceof RuntimeException) {
            return false;
        }
        // 其他情况，可以根据实际需求判断
        // ...
        return false;  // 默认不重试
    }

    private MiniRpcProtocol<MiniRpcRequest> createProtocol(Method method, Object[] args) {
        MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
        protocol.setHeader(createHeader());
        protocol.setBody(createRequestBody(method, args));
        return protocol;
    }

    private MsgHeader createHeader() {
        MsgHeader header = new MsgHeader();
        long requestId = MiniRpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        return header;
    }

    private MiniRpcRequest createRequestBody(Method method, Object[] args) {
        MiniRpcRequest request = new MiniRpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParams(args);
        return request;
    }

    private Object sendRpcRequest(MiniRpcProtocol<MiniRpcRequest> protocol) throws Exception {
        MiniRpcFuture<MiniRpcResponse> future = new MiniRpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        MiniRpcRequestHolder.REQUEST_MAP.put(protocol.getHeader().getRequestId(), future);
        rpcConsumer.sendRequest(protocol, this.registryService);
        return future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
    }

}
