package com.hzh.rpc.codec;

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
        if (in.readableBytes() < ProtocolConstants.HEADER_TOTAL_LEN) {
            return;
        }
        in.markReaderIndex();

        short magic = in.readShort();
        if (magic != ProtocolConstants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }

        byte version = in.readByte();
        byte serializeType = in.readByte();
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();

        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        MsgType msgTypeEnum = MsgType.findByType(msgType);
        if (msgTypeEnum == null) {
            return;
        }

        MsgHeader header = new MsgHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerialization(serializeType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgType(msgType);
        header.setMsgLen(dataLength);

        RpcSerialization rpcSerialization = SerializationFactory.getRpcSerialization(serializeType);
        switch (msgTypeEnum) {
            case REQUEST:
                MiniRpcRequest request = rpcSerialization.deserialize(data, MiniRpcRequest.class);
                if (request != null) {
                    MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
            case RESPONSE:
                MiniRpcResponse response = rpcSerialization.deserialize(data, MiniRpcResponse.class);
                if (response != null) {
                    MiniRpcProtocol<MiniRpcResponse> protocol = new MiniRpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
            case HEARTBEAT:
                // TODO
                break;
        }
    }
}
