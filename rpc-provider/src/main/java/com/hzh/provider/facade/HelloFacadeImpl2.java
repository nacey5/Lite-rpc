package com.hzh.provider.facade;

import com.hzh.provider.annotation.RpcService;

/**
 * @ClassName HelloFacadeImpl2
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/9 17:03
 * @Version 0.0.1
 **/
@RpcService(serviceInterface = HelloFacade.class, serviceVersion = "1.0.0",group = "Second")

public class HelloFacadeImpl2 implements HelloFacade{
    @Override
    public String hello(String name) {
        return "hello" + name+"2";
    }
}
