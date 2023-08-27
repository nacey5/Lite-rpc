package com.hzh.consumer.hook.instance;

import com.hzh.consumer.RpcConsumerImpl;
import com.hzh.consumer.factory.RpcConsumerFactory;
import com.hzh.consumer.hook.ShutdownHook;
import com.hzh.consumer.hook.annotations.HookShutdown;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName RpcConsumerHook
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/26 16:52
 * @Version 0.0.1
 **/

@Slf4j
@HookShutdown(priority = 1)
public class RpcConsumerHook implements ShutdownHook {

    private void shutdownHook() {
        log.info("RpcConsumerHook shutdownHook is running......");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ((RpcConsumerImpl) RpcConsumerFactory.getInstance()).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public void execute() {
        shutdownHook();
    }
}
