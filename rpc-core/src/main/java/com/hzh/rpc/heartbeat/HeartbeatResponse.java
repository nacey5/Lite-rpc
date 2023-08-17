package com.hzh.rpc.heartbeat;

import com.hzh.rpc.enums.MessageType;

/**
 * @ClassName HeartbeatResponse
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/17 17:31
 * @Version 0.0.1
 **/
public class HeartbeatResponse {
    private RpcMessageHeader header;
    private boolean status;  // 表示响应的状态，例如：true表示正常，false表示有问题

    public HeartbeatResponse() {
        this.header = new RpcMessageHeader();
        this.header.setType(MessageType.HEARTBEAT_RESPONSE);
        this.header.setTimestamp(System.currentTimeMillis());
        this.status = true;  // 默认为正常
        // ... 其他初始化代码 ...
    }
}
