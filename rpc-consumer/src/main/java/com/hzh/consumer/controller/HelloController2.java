//package com.hzh.consumer.controller;
//
//import com.hzh.consumer.annotation.RpcReference;
//import com.hzh.consumer.enums.ProxyType;
//import com.hzh.provider.facade.HelloFacade;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @ClassName HellloController2
// * @Description TODO
// * @Author DaHuangGo
// * @Date 2023/9/9 17:26
// * @Version 0.0.1
// **/
//@RestController
//public class HelloController2 {
//    @RpcReference(serviceVersion = "1.0.0", timeout = 10000,proxyType = ProxyType.JAVASSIST,directAddress = "",group = "Second")//
////    @RpcStub(HelloFacadeLocalImpl.class)
//    private HelloFacade helloFacade;
//
//    @RequestMapping(value = "/hello2", method = RequestMethod.GET)
//    public String sayHello2() {
//        return helloFacade.hello("mini rpc");
//    }
//}
