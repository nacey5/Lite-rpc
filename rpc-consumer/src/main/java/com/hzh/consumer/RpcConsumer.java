package com.hzh.consumer;

import com.hzh.consumer.pool.RpcConnection;
import com.hzh.consumer.pool.RpcConnectionFactory;
import com.hzh.provider.registry.RegistryFactory;
import com.hzh.provider.registry.RegistryService;
import com.hzh.provider.registry.RegistryType;
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
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import static com.hzh.consumer.proxy.RpcInvokerProxy.createHeader;
import static com.hzh.provider.registry.RegistryFactory.registryService;


@Slf4j
public class RpcConsumer implements AutoCloseable{
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private GenericObjectPool<RpcConnection> connectionPool;
    private final Object poolLock = new Object();

    private static final RpcConsumer INSTANCE = new RpcConsumer();

    private  RpcConsumer() {
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
            ServiceMeta serviceMetadata = registryService.discovery(serviceKey, invokerHashCode);
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
        MiniRpcProtocol<HeartbeatRequest> heartbeatProtocol = new MiniRpcProtocol<>();
        // ... 设置心跳请求的其他字段 ...
        this.sendRequest(heartbeatProtocol, registryService);
    }

    public void sendHeartbeatRequest() throws Exception {
        MiniRpcProtocol<HeartbeatRequest> heartbeatProtocol = createHeartbeatProtocol();
        MiniRpcFuture<MiniRpcResponse> future = new MiniRpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), 3000);
        MiniRpcRequestHolder.REQUEST_MAP.put(heartbeatProtocol.getHeader().getRequestId(), future);
        this.sendRequest(heartbeatProtocol, registryService);
    }

    private MiniRpcProtocol<HeartbeatRequest> createHeartbeatProtocol() {
        MiniRpcProtocol<HeartbeatRequest> protocol = new MiniRpcProtocol<>();
        protocol.setHeader(createHeader()); // 使用相同的头部创建方法
        protocol.setBody(new HeartbeatRequest()); // 创建一个新的心跳请求实例
        return protocol;
    }

}
