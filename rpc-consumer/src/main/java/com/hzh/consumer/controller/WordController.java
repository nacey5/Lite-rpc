package com.hzh.consumer.controller;

import com.hzh.consumer.annotation.RpcReference;
import com.hzh.consumer.enums.ProxyType;
import com.hzh.provider.facade.HelloFacade;
import com.hzh.provider.facade.WordFacade;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName WordController
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/9 21:52
 * @Version 0.0.1
 **/

@RestController
public class WordController {
    @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
    @RpcReference(serviceVersion = "1.0.0", timeout = 10000,proxyType = ProxyType.JDK,directAddress = "",group = "Second")//
//    @RpcStub(HelloFacadeLocalImpl.class)
    private WordFacade wordFacade;


    @RequestMapping(value = "/word", method = RequestMethod.GET)
    public String sayHello() {
        return wordFacade.sysWord("rpc");
    }
}
