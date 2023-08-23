package com.hzh.rpc;

import com.hzh.rpc.spi.serialization.RpcSerialization;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationFactory {

    private static ConcurrentHashMap<Byte, RpcSerialization> serializers = new ConcurrentHashMap<>();

    static {
        ServiceLoader<RpcSerialization> loadedSerializers = ServiceLoader.load(RpcSerialization.class);
        Iterator<RpcSerialization> serializersIterator = loadedSerializers.iterator();
        while (serializersIterator.hasNext()) {
            RpcSerialization serializer = serializersIterator.next();
            serializers.put(serializer.getType(), serializer);
        }
    }

    public static RpcSerialization getRpcSerialization(byte serializationType) {
        return serializers.get(serializationType);
    }
}
