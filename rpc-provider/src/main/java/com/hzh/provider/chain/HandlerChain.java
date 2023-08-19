package com.hzh.provider.chain;

import com.hzh.provider.handler.RpcHandler;
import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName HandlerChain
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/20 0:51
 * @Version 0.0.1
 **/
@Slf4j
public class HandlerChain {

    private final List<RpcHandler> handlers = new ArrayList<>();

    public HandlerChain addHandler(RpcHandler handler) {
        handlers.add(handler);
        return this;
    }

    public void handle(ChannelHandlerContext ctx, MiniRpcProtocol<MiniRpcRequest> protocol) throws Exception {
        log.info("HandlerChain handle request");
        for (RpcHandler handler : handlers) {
           handler.channelRead0(ctx, protocol);
        }
    }
}

