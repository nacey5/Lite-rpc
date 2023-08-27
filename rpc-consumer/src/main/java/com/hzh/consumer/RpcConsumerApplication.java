package com.hzh.consumer;

import com.hzh.consumer.hook.ShutdownHookManager;
import com.hzh.consumer.hook.instance.RpcConsumerHook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class RpcConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcConsumerApplication.class, args);
        // 添加关闭RpcConsumer的钩子
        //todo 后面将优化一个hook队列，直接加入队列hook进行关闭
        ShutdownHookManager.getInstance().shutdown();
    }
}
