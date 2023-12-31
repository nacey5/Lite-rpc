package com.hzh.rpc.common;

import com.hzh.rpc.RpcContext;
import lombok.Data;

import java.io.Serializable;

@Data
public class MiniRpcRequest implements Serializable {
    private String serviceVersion;
    private String className;
    private String methodName;
    private Object[] params;
    private Class<?>[] parameterTypes;
    private RpcContext rpcContext;
    private String group;
}
