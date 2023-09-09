package com.hzh.consumer;

import com.hzh.consumer.heartbear.HeartbeatManager;
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

import static com.hzh.provider.registry.RegistryFactory.registryService;


@Slf4j
public class RpcConsumerImpl implements RpcConsumer, AutoCloseable {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private final HeartbeatManager heartbeatManager;


    private GenericObjectPool<RpcConnection> connectionPool;
    private final Object poolLock = new Object();

    private static final RpcConsumerImpl INSTANCE = new RpcConsumerImpl();

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
        this.heartbeatManager = new HeartbeatManager(this);
    }

    public static RpcConsumer getInstance() {
        return INSTANCE;
    }

    public void sendRequest(MiniRpcProtocol<?> protocol, RegistryService registryService) throws Exception {
        if (protocol.getBody() instanceof HeartbeatRequest) {
            heartbeatManager.sendHeartbeatRequest();
            return;
        }
        RpcConnection connection = null;
        MiniRpcRequest request = (MiniRpcRequest) protocol.getBody();
        try {
            ServiceMeta serviceMetadata = discoverService(request);
            ensureConnectionPoolInitialized(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
            connection = connectionPool.borrowObject();
            connectToService(serviceMetadata, connection, protocol);
        } finally {
            if (connection != null) {
                connectionPool.returnObject(connection);
            }
        }
    }


    //todo 这是一个小兜底，我有想着要不要把一些消费是实例任务改成弱引用
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (connectionPool != null) {
            connectionPool.close();
        }
    }

    @Override
    public void close() {
        if (connectionPool != null) {
            connectionPool.close();
        }
        eventLoopGroup.shutdownGracefully();
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

    public GenericObjectPool<RpcConnection> getConnectionPool() {
        return connectionPool;
    }

    public Object invokeGeneric(String serviceName, String methodName, String serviceVersion,String group, long timeout, Class[] paramTypes, CircuitBreaker circuitBreaker, Object... args) throws Throwable {
        GenericInvokerProxy genericInvoker = new GenericInvokerProxy(registryService);
        return genericInvoker.invoke(serviceName, methodName, serviceVersion,group, timeout, paramTypes, circuitBreaker, args);
    }


    private void connectToService(ServiceMeta serviceMetadata, RpcConnection connection, MiniRpcProtocol<?> protocol) throws InterruptedException {
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

    private void ensureConnectionPoolInitialized(String serviceAddr, int servicePort) {
        //lazy load 第一次加载的时候发现对象池不存在的时候，才需要去进行加载
        synchronized (poolLock) {
            if (connectionPool == null) {
                GenericObjectPoolConfig<RpcConnection> poolConfig = new GenericObjectPoolConfig<>();
                poolConfig.setMaxTotal(50);
                poolConfig.setMinIdle(10);
                connectionPool = new GenericObjectPool<>(new RpcConnectionFactory(bootstrap, serviceAddr, servicePort), poolConfig);
            }
        }
    }

    private ServiceMeta discoverService(MiniRpcRequest request) throws Exception {
        Object[] params = request.getParams();
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion(),request.getGroup());
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        ServiceMeta serviceMetadata = null;
        if (this.directAddress != null && !this.directAddress.isEmpty()) {
            serviceMetadata = directService(request, serviceMetadata);
        } else {
            serviceMetadata = registryService.discovery(serviceKey, invokerHashCode);
        }
        return serviceMetadata;
    }

    private ServiceMeta directService(MiniRpcRequest request, ServiceMeta serviceMetadata) {
        String[] parts = this.directAddress.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid directAddress format. Expected format: host:port");
        }
        serviceMetadata = new ServiceMeta();
        serviceMetadata.setServiceAddr(parts[0]);
        serviceMetadata.setServicePort(Integer.parseInt(parts[1]));
        serviceMetadata.setServiceName(request.getClassName());
        serviceMetadata.setServiceVersion(request.getServiceVersion());
        serviceMetadata.setGroup(request.getGroup());
        return serviceMetadata;
    }


}
