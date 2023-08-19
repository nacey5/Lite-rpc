package com.hzh.provider.handler;

import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.common.MiniRpcResponse;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @ClassName RpcHandler
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/20 0:53
 * @Version 0.0.1
 **/
public abstract class RpcHandler extends SimpleChannelInboundHandler<MiniRpcProtocol<MiniRpcRequest>> {
    /**
     * 处理 RPC 请求。
     *
     * @param ctx ChannelHandlerContext
     * @param msg RPC 请求
     * @return 如果返回 true，则请求将被传递给链中的下一个处理器。如果返回 false，则停止处理并不再调用其他处理器。
     */
    public void channelRead0(ChannelHandlerContext ctx, MiniRpcProtocol<MiniRpcRequest> msg) throws Exception {
        MiniRpcRequest body = msg.getBody();
        if (handle(body)){
            ctx.fireChannelRead(msg); // 将消息传递给pipeline中的下一个处理器
        }else {
            // 如果不想继续处理，可能需要给客户端发送一个响应或者关闭连接
            ctx.writeAndFlush(handleResponse());
            ctx.close(); // 关闭连接
        }
    }

    protected abstract boolean handle(MiniRpcRequest request);

    protected abstract MiniRpcResponse handleResponse();
}

