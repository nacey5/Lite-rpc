package com.hzh.consumer.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.Channel;

/**
 * @ClassName RpcConnection
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/17 14:26
 * @Version 0.0.1
 **/
public class RpcConnection {
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;

    private Channel channel; // channel是用来存储Netty通道的


    public RpcConnection(Bootstrap bootstrap, String host, int port) {
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
    }

    public ChannelFuture connect() throws InterruptedException {
        ChannelFuture future = bootstrap.connect(host, port).sync();
        this.channel= future.channel();
        return future;
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    public void close() {
        if (channel != null) {
            channel.close(); // 关闭Netty通道
        }
    }
}
