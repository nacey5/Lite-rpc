package com.hzh.consumer.heartbear;

import com.hzh.consumer.RpcConsumerImpl;
import com.hzh.rpc.heartbeat.HeartbeatRequest;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import com.hzh.rpc.proxy.RpcInvokerProxy;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.hzh.provider.registry.RegistryFactory.registryService;

/**
 * @ClassName HeartbeatManager
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/26 16:17
 * @Version 0.0.1
 **/
@Slf4j
public class HeartbeatManager {
    private final RpcConsumerImpl rpcConsumer;
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);

    public HeartbeatManager(RpcConsumerImpl rpcConsumer) {
        this.rpcConsumer = rpcConsumer;
        //开启心跳
        startHeartbeatTask();
    }

    private void sendHeartbeat() throws Exception {
        MiniRpcProtocol<HeartbeatRequest> heartbeatProtocol = createHeartbeatProtocol();
        rpcConsumer.sendRequest(heartbeatProtocol, registryService);
    }

    public void sendHeartbeatRequest() throws Exception {
        log.info("heartbeat...");
        MiniRpcProtocol<HeartbeatRequest> heartbeatProtocol = createHeartbeatProtocol();
        Channel channel = null;
        try {
            channel = rpcConsumer.getConnectionPool().borrowObject().connect().channel();
        } catch (Exception ignoreFirst) {
            return;
        }
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(heartbeatProtocol);
        } else {
            log.warn("Channel is not active, cannot send heartbeat");
        }
    }

    private MiniRpcProtocol<HeartbeatRequest> createHeartbeatProtocol() {
        MiniRpcProtocol<HeartbeatRequest> protocol = new MiniRpcProtocol<>();
        protocol.setHeader(RpcInvokerProxy.createHeader());
        protocol.setBody(new HeartbeatRequest());
        return protocol;
    }

    private void startHeartbeatTask() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                log.error("Error sending heartbeat", e);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}

