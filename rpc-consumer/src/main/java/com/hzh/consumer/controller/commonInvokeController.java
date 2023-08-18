package com.hzh.consumer.controller;


import com.hzh.consumer.RpcConsumer;
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
public class commonInvokeController {


    @GetMapping("/sayHello/{serviceName}/{methodName}/{parameterTypeNames}")
    public String sayHello(
            @PathVariable String serviceName,
            @PathVariable String methodName,
            @PathVariable String parameterTypeNames,
            @RequestBody String reqBody) throws Throwable {
        RpcConsumer consumer = RpcConsumer.getInstance();
        String serviceVersion = "1.0.0";  // 从配置或其他地方获取
        long timeout = 5000;  // 从配置或其他地方获取
        Class<?>[] parameterTypes = transferToTypes(parameterTypeNames);
        Object[] args = transferToObjects(reqBody, parameterTypes);
        // 使用泛化调用
        String result = (String) consumer.invokeGeneric(
                serviceName,
                methodName,
                serviceVersion,
                timeout,
                parameterTypes,
                args);
        return result;
    }





}

