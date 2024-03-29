package com.hzh.provider;

import com.hzh.provider.annotation.RpcService;
import com.hzh.provider.chain.ChainHandler;
import com.hzh.provider.handler.RateLimiterHandler;
import com.hzh.rpc.common.RpcProperties;
import com.hzh.rpc.register.RegistryService;
import com.hzh.provider.chain.HandlerChain;
import com.hzh.rpc.codec.MiniRpcDecoder;
import com.hzh.rpc.codec.MiniRpcEncoder;
import com.hzh.rpc.common.RpcServiceHelper;
import com.hzh.rpc.common.ServiceMeta;
import com.hzh.rpc.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {

    private String serverAddress;
    private final int serverPort;
    private final RegistryService serviceRegistry;

    private final HandlerChain handlerChain;

    private final RpcProperties rpcProperties;

    private final Map<String, Object> rpcServiceMap = new ConcurrentHashMap<>();

    public RpcProvider(int serverPort, RegistryService serviceRegistry, RpcProperties rpcProperties) {
        this.serverPort = serverPort;
        this.serviceRegistry = serviceRegistry;
        // 初始化HandlerChain
        this.handlerChain = new HandlerChain();
        // 添加处理器到HandlerChain
        this.handlerChain.
                addHandler(new RateLimiterHandler(10000L, 100L));
        // 初始化RpcProperties
        this.rpcProperties = rpcProperties;
    }

    @Override
    public void afterPropertiesSet() {
        new Thread(() -> {
            try {
                startRpcServer();
            } catch (Exception e) {
                log.error("start rpc server error.", e);
            }
        }).start();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        registerService(bean);
        return bean;
    }

    private void startRpcServer() throws Exception {
        this.serverAddress = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            if (isDirectConnectionConfigured()) {
                startDirectConnectionServer(boss, worker);
            } else {
                startServer(boss, worker);
            }
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private boolean isDirectConnectionConfigured() {
        return (rpcProperties.getDirectAddress() != null && !rpcProperties.getDirectAddress().isEmpty()) && !StringUtils.isBlank(rpcProperties.getDirectAddress());
    }

    private void startDirectConnectionServer(EventLoopGroup boss, EventLoopGroup worker) throws InterruptedException {
        // ... [直连服务器的启动]
        String[] parts = rpcProperties.getDirectAddress().split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid directAddress format. Expected format: host:port");
        }
        String directHost = parts[0];
        int directPort = Integer.parseInt(parts[1]);
        ServerBootstrap directBootstrap = new ServerBootstrap();
        directBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new MiniRpcEncoder())
                                .addLast(new MiniRpcDecoder())
                                .addLast(new ChainHandler(handlerChain))
                                .addLast(RpcRequestHandler.getInstance(rpcServiceMap));
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture directChannelFuture = directBootstrap.bind(directHost, directPort).sync();
        log.info("Direct server addr {} started on port {}", directHost, directPort);
        directChannelFuture.channel().closeFuture().sync();
    }

    private void startServer(EventLoopGroup boss, EventLoopGroup worker) throws InterruptedException {
        // ... [服务器的启动]
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new MiniRpcEncoder())
                                .addLast(new MiniRpcDecoder())
                                .addLast(new ChainHandler(handlerChain))
                                .addLast(RpcRequestHandler.getInstance(rpcServiceMap));
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, this.serverPort).sync();
        log.info("server addr {} started on port {}", this.serverAddress, this.serverPort);
        channelFuture.channel().closeFuture().sync();
    }


    private void registerService(Object bean) {
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            String serviceName = rpcService.serviceInterface().getName();
            String serviceVersion = rpcService.serviceVersion();
            String group=rpcService.group();
            try {
                ServiceMeta serviceMeta = createServiceMeta(serviceName, serviceVersion,group);
                serviceRegistry.register(serviceMeta);
                rpcServiceMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(),serviceMeta.getGroup()), bean);
            } catch (Exception e) {
                log.error("failed to register service {}#{}", serviceName, serviceVersion, e);
            }
        }
    }

    private ServiceMeta createServiceMeta(String serviceName, String serviceVersion,String group) {
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceAddr(serverAddress);
        serviceMeta.setServicePort(serverPort);
        serviceMeta.setServiceName(serviceName);
        serviceMeta.setServiceVersion(serviceVersion);
        serviceMeta.setGroup(group);
        return serviceMeta;
    }

}
