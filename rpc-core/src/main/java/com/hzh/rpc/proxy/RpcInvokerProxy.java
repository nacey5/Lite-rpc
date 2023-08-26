package com.hzh.rpc.proxy;

import com.hzh.rpc.RpcContext;
import com.hzh.rpc.circuitbreaker.CircuitBreaker;
import com.hzh.rpc.common.*;
import com.hzh.rpc.register.RegistryService;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import com.hzh.rpc.protocol.MsgHeader;
import com.hzh.rpc.protocol.MsgType;
import com.hzh.rpc.protocol.ProtocolConstants;
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
    private final CircuitBreaker circuitBreaker;
    private static final int MAX_RETRIES = 3;// 定义重试次数的常数
    private static final int RETRY_DELAY_MS = 1000; // 定义重试间隔时间的常数

    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService,RpcConsumer rpcConsumer,CircuitBreaker circuitBreaker) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
        this.rpcConsumer = rpcConsumer; // 在构造函数中初始化RpcConsumer
        this.circuitBreaker = circuitBreaker;
    }
    //暂时先将本地存根的方式注释掉，目前没有比较好的实现方式

//    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService,RpcConsumer rpcConsumer,
//                           Class<?> serviceInterface, Object remoteService, Object mockService, Object stubService) {
//        this.serviceVersion = serviceVersion;
//        this.timeout = timeout;
//        this.registryService = registryService;
//        this.rpcConsumer = rpcConsumer; // 在构造函数中初始化RpcConsumer
//        this.serviceHandler= new ServiceHandler(serviceInterface, remoteService, mockService, stubService);
//    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!circuitBreaker.canExecute()) {
            throw new RuntimeException("Circuit Breaker is open, cannot execute the RPC call.");
        }
        int retryCount = 0;
//        if (serviceHandler!=null){
//            return serviceHandler.handle(method,args);
//        }
        while (true) {
            try {
                MiniRpcProtocol<MiniRpcRequest> protocol = createProtocol(method, args);
                //hold request by ThreadLocal
                setContext(protocol);
                Object result = sendRpcRequest(protocol);
                circuitBreaker.recordSuccess();  // 记录成功的调用
                return result;
            } catch (Exception e) {
                if (shouldRetry(e) && retryCount < MAX_RETRIES) {
                    retryCount++;
                    // 此处添加延迟
                    Thread.sleep(RETRY_DELAY_MS * retryCount);
                    continue;
                }
                if (circuitBreaker.isCallTimeout(System.currentTimeMillis())) {
                    circuitBreaker.recordFailure();  // 记录失败
                }
                throw e;  // 如果达到最大重试次数或异常不应该重试，抛出异常
            }
            finally {
                RpcContext.removeContext();
            }
        }
    }

    private static void setContext(MiniRpcProtocol<MiniRpcRequest> protocol) {
        RpcContext context = RpcContext.getContext();
        context.set("header", protocol.getHeader().getRequestId());
        context.set("body", protocol.getBody());
    }

    private MiniRpcProtocol<MiniRpcRequest> createProtocol(Method method, Object[] args) {
        MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
        protocol.setHeader(createHeader());
        protocol.setBody(createRequest(method, args));
        return protocol;
    }

    public static MsgHeader createHeader() {
        //暂时先不引入这个ThreadLocal，因为consumer对象是复用的，关闭consumer再进行remove会导致内存泄漏
//        CURRENT_REQUEST.set(protocol);
        MsgHeader header = new MsgHeader();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(MiniRpcRequestHolder.REQUEST_ID_GEN.incrementAndGet());
        header.setSerialization((byte) 0x10);
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
        request.setRpcContext(RpcContext.getContext());
        return request;
    }

    private Object sendRpcRequest(MiniRpcProtocol<MiniRpcRequest> protocol) throws Exception {
        MiniRpcFuture<MiniRpcResponse> future = new MiniRpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        MiniRpcRequestHolder.REQUEST_MAP.put(protocol.getHeader().getRequestId(), future);
        rpcConsumer.sendRequest(protocol, this.registryService);
        return future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
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

    public Object invokeGeneric(String serviceName, String methodName, Object[] args,Class[] paramTypes) throws Throwable {
        MiniRpcProtocol<MiniRpcRequest> protocol = createGenericProtocol(serviceName, methodName, args,paramTypes);
        return sendRpcRequest(protocol);
    }

    private MiniRpcProtocol<MiniRpcRequest> createGenericProtocol(String serviceName, String methodName, Object[] args,Class[] parameterTypes){
        MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
        protocol.setHeader(createHeader());
        MiniRpcRequest request = new MiniRpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(serviceName);
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParams(args);
        request.setRpcContext(RpcContext.getContext());
        protocol.setBody(request);
        return protocol;
    }




}
