package com.hzh.consumer;

import com.hzh.rpc.common.RpcConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class RpcConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcConsumerApplication.class, args);

        // 添加关闭RpcConsumer的钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ((RpcConsumerImpl)RpcConsumerFactory.getInstance()).close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
