package com.hzh;

import com.hzh.rpc.SerializationFactory;
import com.hzh.rpc.spi.serialization.RpcSerialization;

public class Main {
    public static void main(String[] args) {
        RpcSerialization hessianSerialization = SerializationFactory.getRpcSerialization((byte) 0x10);
        System.out.println(hessianSerialization.getClass().getName());  // 应该输出HessianSerialization的完全限定名

        RpcSerialization jsonSerialization = SerializationFactory.getRpcSerialization((byte) 0x20);
        System.out.println(jsonSerialization.getClass().getName());
    }
}