package com.hzh.rpc.heartbeat;


import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.enums.MessageType;

/**
 * @ClassName HeartbeatRequest
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/17 17:30
 * @Version 0.0.1
 **/
public class HeartbeatRequest extends MiniRpcRequest {
    private RpcMessageHeader header;

    public HeartbeatRequest() {
        this.header = new RpcMessageHeader();
        this.header.setType(MessageType.HEARTBEAT_REQUEST);
        this.header.setTimestamp(System.currentTimeMillis());
        // ... 其他初始化代码 ...
    }
}
