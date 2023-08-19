package com.hzh.provider.chain;

import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @ClassName ChainHandler
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/20 2:06
 * @Version 0.0.1
 **/
public class ChainHandler extends SimpleChannelInboundHandler<MiniRpcProtocol<MiniRpcRequest>> {
    private HandlerChain handlerChain;

    public ChainHandler(HandlerChain handlerChain) {
        this.handlerChain = handlerChain;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, MiniRpcProtocol<MiniRpcRequest> protocol) throws Exception {
        handlerChain.handle(ctx,protocol);
    }
}
