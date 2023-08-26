package com.hzh.consumer;

import com.hzh.consumer.pool.RpcConnection;
import com.hzh.consumer.pool.RpcConnectionFactory;
import com.hzh.consumer.proxy.GenericInvokerProxy;
import com.hzh.rpc.circuitbreaker.CircuitBreaker;
import com.hzh.rpc.register.RegistryService;
import com.hzh.rpc.codec.MiniRpcDecoder;
import com.hzh.rpc.codec.MiniRpcEncoder;
import com.hzh.rpc.common.*;
import com.hzh.rpc.handler.RpcResponseHandler;
import com.hzh.rpc.heartbeat.HeartbeatRequest;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.hzh.rpc.proxy.RpcInvokerProxy.createHeader;
import static com.hzh.provider.registry.RegistryFactory.registryService;


@Slf4j
public class RpcConsumerImpl implements RpcConsumer, AutoCloseable {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private GenericObjectPool<RpcConnection> connectionPool;
    private final Object poolLock = new Object();

    private static final RpcConsumerImpl INSTANCE = new RpcConsumerImpl();

    private ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);

    private String directAddress;


    private RpcConsumerImpl() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new MiniRpcEncoder())
                                .addLast(new MiniRpcDecoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
        //开启心跳
        startHeartbeatTask();

    }

    public static RpcConsumer getInstance() {
        return INSTANCE;
    }

    public void sendRequest(MiniRpcProtocol<?> protocol, RegistryService registryService) throws Exception {
        if (protocol.getBody() instanceof HeartbeatRequest) {
            sendHeartbeatRequest();
            return;
        }
        RpcConnection connection = null;
        MiniRpcRequest request = (MiniRpcRequest) protocol.getBody();
        Object[] params = request.getParams();
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());

        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        try {
            ServiceMeta serviceMetadata;
            //直连和注册中心的区别
            if (this.directAddress != null && !this.directAddress.isEmpty()) {
                // 使用直连地址
                String[] parts = this.directAddress.split(":");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid directAddress format. Expected format: host:port");
                }
                serviceMetadata = new ServiceMeta();
                serviceMetadata.setServiceAddr(parts[0]);
                serviceMetadata.setServicePort(Integer.parseInt(parts[1]));
                // 设置serviceName和serviceVersion
                serviceMetadata.setServiceName(request.getClassName()); // 服务名称
                serviceMetadata.setServiceVersion(request.getServiceVersion());
            } else {
                serviceMetadata = registryService.discovery(serviceKey, invokerHashCode);
            }
            if (serviceMetadata != null) {
//            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort()).sync();
                log.info("connect rpc server {} on port {} success.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                if (connectionPool == null) {
                    initConnectionPool(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                }
                connection = connectionPool.borrowObject();
                ChannelFuture future = connection.connect();
                future.addListener((ChannelFutureListener) arg0 -> {
                    if (future.isSuccess()) {
                        log.info("connect rpc server {} on port {} success.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                    } else {
                        log.error("connect rpc server {} on port {} failed.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                        future.cause().printStackTrace();
                        eventLoopGroup.shutdownGracefully();
                    }
                });
                future.channel().writeAndFlush(protocol);
            }
        } finally {
            if (connection != null) {
                connectionPool.returnObject(connection);
            }
        }
    }


    private void initConnectionPool(String serviceAddr, int servicePort) {
        //lazy load 第一次加载的时候发现对象池不存在的时候，才需要去进行加载
        synchronized (poolLock) {
            if (connectionPool == null) {
                GenericObjectPoolConfig<RpcConnection> poolConfig = new GenericObjectPoolConfig<>();
                poolConfig.setMaxTotal(50);
                poolConfig.setMinIdle(10);
//                poolConfig.setTestOnBorrow(true);  // 设置为true以在借用连接之前测试连接的有效性
                connectionPool = new GenericObjectPool<>(new RpcConnectionFactory(bootstrap, serviceAddr, servicePort), poolConfig);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (connectionPool != null) {
            connectionPool.close();
        }
    }

    @Override
    public void close() throws Exception {
        if (connectionPool != null) {
            connectionPool.close();
        }
        eventLoopGroup.shutdownGracefully();
    }


    private void sendHeartbeat() throws Exception {
        MiniRpcProtocol<HeartbeatRequest> heartbeatProtocol = createHeartbeatProtocol();
        // ... 设置心跳请求的其他字段 ...
        this.sendRequest(heartbeatProtocol, registryService);
    }

    public void sendHeartbeatRequest() throws Exception {
        log.info("heartbeat...");
        MiniRpcProtocol<HeartbeatRequest> heartbeatProtocol = createHeartbeatProtocol();
        // 在这里我直接忽略了调用可能出现的异常-第一次调用的时候没有进行初始化必定为空，因为是心跳，非业务可以忽略错误，所以可以使用try catch
        Channel channel = null;
        try {
            channel = connectionPool.borrowObject().connect().channel();
        } catch (Exception ignoreFirst) {
            return;
        }
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(heartbeatProtocol);
        } else {
            // 如果Channel不可用，可以记录日志或执行其他操作
            log.warn("Channel is not active, cannot send heartbeat");
        }
    }

    private MiniRpcProtocol<HeartbeatRequest> createHeartbeatProtocol() {
        MiniRpcProtocol<HeartbeatRequest> protocol = new MiniRpcProtocol<>();
        protocol.setHeader(createHeader()); // 使用相同的头部创建方法
        protocol.setBody(new HeartbeatRequest()); // 创建一个新的心跳请求实例
        return protocol;
    }

    private void startHeartbeatTask() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                log.error("Error sending heartbeat", e);
            }
        }, 0, 10, TimeUnit.SECONDS);  // 每10秒发送一次心跳
    }

    public Object invokeGeneric(String serviceName, String methodName, String serviceVersion, long timeout, Class[] paramTypes, CircuitBreaker circuitBreaker, Object... args) throws Throwable {
        GenericInvokerProxy genericInvoker = new GenericInvokerProxy(registryService);
        return genericInvoker.invoke(serviceName, methodName, serviceVersion, timeout, paramTypes,circuitBreaker, args);
    }

    @Override
    public ChannelFuture tryConnect() throws InterruptedException {
        String[] parts = this.directAddress.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid directAddress format. Expected format: host:port");
        }
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        return bootstrap.connect(host, port).sync();
    }

    public void setDirectAddress(String directAddress) {
        this.directAddress = directAddress;
    }


}
