package com.hzh.rpc.common;

public class RpcServiceHelper {
    public static String buildServiceKey(String serviceName, String serviceVersion,String group) {
        return String.join("#", serviceName, serviceVersion,group);
    }
}
