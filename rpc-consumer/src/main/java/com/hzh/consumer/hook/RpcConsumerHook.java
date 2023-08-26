package com.hzh.consumer.hook;

import com.hzh.consumer.RpcConsumerImpl;
import com.hzh.consumer.factory.RpcConsumerFactory;

/**
 * @ClassName RpcConsumerHook
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/26 16:52
 * @Version 0.0.1
 **/
public class RpcConsumerHook {

    public static void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ((RpcConsumerImpl) RpcConsumerFactory.getInstance()).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
