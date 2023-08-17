package com.hzh.consumer;

import com.hzh.consumer.pool.RpcConnection;
import com.hzh.consumer.pool.RpcConnectionFactory;
import com.hzh.provider.registry.RegistryService;
import com.hzh.rpc.codec.MiniRpcDecoder;
import com.hzh.rpc.codec.MiniRpcEncoder;
import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.common.RpcServiceHelper;
import com.hzh.rpc.common.ServiceMeta;
import com.hzh.rpc.handler.RpcResponseHandler;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

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

    public void sendRequest(MiniRpcProtocol<MiniRpcRequest> protocol, RegistryService registryService) throws Exception {
        RpcConnection connection = null;

        MiniRpcRequest request = protocol.getBody();
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
}
