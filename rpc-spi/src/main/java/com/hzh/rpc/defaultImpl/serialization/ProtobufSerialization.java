package com.hzh.rpc.defaultImpl.serialization;

import com.hzh.rpc.spi.serialization.RpcSerialization;

/**
 * @ClassName ProtobufSerialization
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/5 17:52
 * @Version 0.0.1
 **/
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.hzh.rpc.exception.SerializationException;

public class ProtobufSerialization implements RpcSerialization {

    @Override
    public <T> byte[] serialize(T object) {
        if (object == null) {
            throw new NullPointerException();
        }
        if (!(object instanceof MessageLite)) {
            throw new SerializationException("Object to be serialized is not a protobuf object");
        }

        MessageLite messageLite = (MessageLite) object;
        return messageLite.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null) {
            throw new NullPointerException();
        }

        T result;
        try {
            MessageLite defaultInstance = (MessageLite) clz.getMethod("getDefaultInstance").invoke(null);
            result = (T) defaultInstance.getParserForType().parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new SerializationException("Failed to deserialize protobuf object", e);
        } catch (Exception e) {
            throw new SerializationException("Failed to get default instance for protobuf object", e);
        }

        return result;
    }

    @Override
    public byte getType() {
        return 0x30;
    }
}

