package com.hzh.consumer.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;

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

    public RpcConnection(Bootstrap bootstrap, String host, int port) {
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
    }

    public ChannelFuture connect() throws InterruptedException {
        return bootstrap.connect(host, port).sync();
    }
}
