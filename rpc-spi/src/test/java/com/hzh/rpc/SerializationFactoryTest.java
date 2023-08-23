package com.hzh.rpc;

import com.hzh.rpc.spi.serialization.RpcSerialization;
import org.junit.Test;

public class SerializationFactoryTest {

    @Test
    public void testGetRpcSerialization() {
        RpcSerialization hessianSerialization = SerializationFactory.getRpcSerialization((byte) 0x10);
        System.out.println(hessianSerialization.getClass().getName());  // 应该输出HessianSerialization的完全限定名

        RpcSerialization jsonSerialization = SerializationFactory.getRpcSerialization((byte) 0x20);
        System.out.println(jsonSerialization.getClass().getName());  // 应该输出JsonSerialization的完全限定名
        // Verify the results
    }
}
