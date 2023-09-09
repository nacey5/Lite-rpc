package com.hzh.consumer.controller;


import com.hzh.consumer.factory.RpcConsumerFactory;
import com.hzh.consumer.factory.CircuitBreakerFactory;
import com.hzh.rpc.circuitbreaker.CircuitBreaker;
import com.hzh.rpc.common.RpcConsumer;
import org.springframework.web.bind.annotation.*;

import static com.hzh.rpc.transfer.ArgsTransfer.transferToObjects;
import static com.hzh.rpc.transfer.ArgsTransfer.transferToTypes;

/**
 * @ClassName commonInvokeController
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/18 15:43
 * @Version 0.0.1
 **/

@RestController
public class CommonInvokeController {


    @GetMapping("/sayHello/{serviceName}/{methodName}/{parameterTypeNames}")
    public String sayHello(
            @PathVariable String serviceName,
            @PathVariable String methodName,
            @PathVariable String parameterTypeNames,
            @RequestBody String reqBody) throws Throwable {
        RpcConsumer consumer = RpcConsumerFactory.getInstance();
        String serviceVersion = "1.0.0";  // 从配置或其他地方获取
        long timeout = 5000;  // 从配置或其他地方获取
        Class<?>[] parameterTypes = transferToTypes(parameterTypeNames);
        String group="";
        Object[] args = transferToObjects(reqBody, parameterTypes);
        CircuitBreaker simpleCircuitBreaker= CircuitBreakerFactory.createCircuitBreaker(CircuitBreakerFactory.BreakerType.SIMPLE, 5, 60000, 5000);
        // 使用泛化调用
        String result = (String) consumer.invokeGeneric(
                serviceName,
                methodName,
                serviceVersion,
                group,
                timeout,
                parameterTypes,
                simpleCircuitBreaker,
                args);
        return result;
    }





}

