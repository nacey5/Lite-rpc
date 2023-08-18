package com.hzh.consumer.controller;

import com.hzh.consumer.RpcConsumer;
import com.hzh.provider.registry.RegistryFactory;
import com.hzh.provider.registry.RegistryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName commonInvokeController
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/18 15:43
 * @Version 0.0.1
 **/

@RestController
public class commonInvokeController {


    @GetMapping("/sayHello")
    public String sayHello() throws Throwable {
        RpcConsumer consumer = RpcConsumer.getInstance();
        String serviceVersion = "1.0.0";  // 从配置或其他地方获取
        long timeout = 5000;  // 从配置或其他地方获取
        // 使用泛化调用
        String result = (String) consumer.invokeGeneric("com.hzh.provider.facade.HelloFacade", "hello", serviceVersion, timeout, new Class[]{String.class},"mini rpc");
        return result;
    }
}

