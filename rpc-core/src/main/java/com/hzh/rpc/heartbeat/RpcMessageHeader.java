package com.hzh.rpc.heartbeat;


import com.hzh.rpc.enums.MessageType;
import lombok.Data;

/**
 * @ClassName RpcMessageHeader
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/17 17:31
 * @Version 0.0.1
 **/
@Data
public class RpcMessageHeader {
    private MessageType type;  // 消息类型，例如：REQUEST, RESPONSE, HEARTBEAT_REQUEST, HEARTBEAT_RESPONSE
    private String version;    // 消息版本
    private long timestamp;    // 消息发送的时间戳
}
