package com.hzh.consumer.controller;

import com.hzh.consumer.controller.local.HelloFacadeLocalImpl;
import com.hzh.consumer.enums.ProxyType;
import com.hzh.consumer.annotation.RpcReference;
import com.hzh.provider.facade.HelloFacade;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {

    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
    @RpcReference(serviceVersion = "1.0.0", timeout = 10000,proxyType = ProxyType.JDK,directAddress = "")//
//    @RpcStub(HelloFacadeLocalImpl.class)
    private HelloFacade helloFacade;


    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String sayHello() {
        return helloFacade.hello("mini rpc");
    }


}
