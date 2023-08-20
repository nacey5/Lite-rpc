package com.hzh.provider.handler;

import com.hzh.provider.struct.TokenBucket;
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

    private TokenBucket tokenBucket;

    public RateLimiterHandler(long capacity, long refillRate) {
        this.tokenBucket = TokenBucket.instance(capacity, refillRate);
    }

    @Override
    public boolean handle(MiniRpcRequest request) {
        // 如果没有可用的令牌，拒绝请求
        if (!tokenBucket.tryAcquire()) {
            log.warn("Rate limit exceeded, request denied.");
            return false; // 不再传递给链中的其他处理器
        }
        log.info("RateLimiterHandler handle request,num:{}",tokenBucket.getAvailableTokens());
        log.info("RateLimiterHandler handle request,rate:{}",tokenBucket.getRefillRate());
        return true; // 继续处理链中的下一个处理器
    }

    @Override
    protected MiniRpcResponse handleResponse() {
        MiniRpcResponse miniRpcResponse=new MiniRpcResponse();
        miniRpcResponse.setMessage("cure error");
        return miniRpcResponse;
    }

}

