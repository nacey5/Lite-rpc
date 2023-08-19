package com.hzh.provider.handler;

import com.google.common.util.concurrent.RateLimiter;
import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.common.MiniRpcResponse;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName RateLimiterHandler
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/20 0:54
 * @Version 0.0.1
 **/
@Slf4j
@ChannelHandler.Sharable
public class RateLimiterHandler extends RpcHandler {

    private RateLimiter rateLimiter;

    public RateLimiterHandler(double  permitsPerSecond) {
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    @Override
    public boolean handle(MiniRpcRequest request) {
        // 如果没有可用的令牌，拒绝请求
        if (!rateLimiter.tryAcquire()) {
            log.warn("Rate limit exceeded, request denied.");
            return false; // 不再传递给链中的其他处理器
        }
        log.info("RateLimiterHandler handle request,rate:{}",rateLimiter.getRate());
        return true; // 继续处理链中的下一个处理器
    }

    @Override
    protected MiniRpcResponse handleResponse() {
        MiniRpcResponse miniRpcResponse=new MiniRpcResponse();
        miniRpcResponse.setMessage("cure error");
        return miniRpcResponse;
    }

}

