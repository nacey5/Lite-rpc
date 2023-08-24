package com.hzh.rpc.local;

/**
 * @ClassName LocalHelloServiceStub
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/24 14:10
 * @Version 0.0.1
 **/
public class LocalHelloServiceStub implements HelloService{
    private final HelloService helloService;

    public LocalHelloServiceStub(HelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    public String sayHello(String name) {
        return "Local: " +helloService.sayHello(name);
    }
}
