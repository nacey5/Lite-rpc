package com.hzh.rpc.codec;

import com.hzh.rpc.RpcContext;
import com.hzh.rpc.SerializationFactory;
import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.common.MiniRpcResponse;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import com.hzh.rpc.protocol.MsgHeader;
import com.hzh.rpc.protocol.MsgType;
import com.hzh.rpc.protocol.ProtocolConstants;
import com.hzh.rpc.spi.serialization.RpcSerialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public class MiniRpcDecoder extends ByteToMessageDecoder {

    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    |                   数据内容 （长度不定）                          |
    +---------------------------------------------------------------+
    */
    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {
                return;
            }
            in.markReaderIndex();

            MsgHeader header = decodeHeader(in);
            byte[] data = decodeData(in, header.getMsgLen());

            RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(header.getSerialization());
            switch (MsgType.findByType(header.getMsgType())) {
                case REQUEST:
                    handleRequest(data, rpcSerialization, header, out);
                    break;
                case RESPONSE:
                    handleResponse(data, rpcSerialization, header, out);
                    break;
                case HEARTBEAT:
                    // TODO: Handle heartbeat
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type: " + header.getMsgType());
            }
        } finally {
            RpcContext.removeContext();
        }
    }

    private MsgHeader decodeHeader(ByteBuf in) {
        short magic = in.readShort();
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("Invalid magic number: " + magic);
        }

        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(in.readByte());
        header.setSerialization(in.readByte());
        header.setMsgType(in.readByte());
        header.setStatus(in.readByte());
        header.setRequestId(in.readLong());
        header.setMsgLen(in.readInt());

        return header;
    }

    private byte[] decodeData(ByteBuf in, int dataLength) {
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            throw new IllegalArgumentException("Not enough readable bytes for data length: " + dataLength);
        }

        byte[] data = new byte[dataLength];
        in.readBytes(data);
        return data;
    }

    private void handleRequest(byte[] data, RpcSerialization rpcSerialization, MsgHeader header, List<Object> out) throws IOException {
        MiniRpcRequest request = rpcSerialization.deserialize(data, MiniRpcRequest.class);
        if (request != null) {
            MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
            protocol.setHeader(header);
            protocol.setBody(request);
            RpcContext.getContext().setAll(request.getRpcContext());
            out.add(protocol);
        }
    }

    private void handleResponse(byte[] data, RpcSerialization rpcSerialization, MsgHeader header, List<Object> out) throws IOException {
        MiniRpcResponse response = rpcSerialization.deserialize(data, MiniRpcResponse.class);
        if (response != null) {
            MiniRpcProtocol<MiniRpcResponse> protocol = new MiniRpcProtocol<>();
            protocol.setHeader(header);
            protocol.setBody(response);
            out.add(protocol);
        }
    }
}

