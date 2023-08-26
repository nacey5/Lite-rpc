package com.hzh.consumer.factory;

import com.hzh.consumer.RpcConsumerImpl;
import com.hzh.rpc.common.RpcConsumer;

/**
 * @ClassName RpcConsumerFactory
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/23 18:37
 * @Version 0.0.1
 **/
public class RpcConsumerFactory {
    private static final RpcConsumer INSTANCE = RpcConsumerImpl.getInstance();

    public static RpcConsumer getInstance() {
        return INSTANCE;
    }
}
