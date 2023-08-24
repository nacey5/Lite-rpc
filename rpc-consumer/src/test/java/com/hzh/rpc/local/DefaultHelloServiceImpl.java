package com.hzh.rpc.local;

/**
 * @ClassName DefaultHelloServiceImpl
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/24 14:10
 * @Version 0.0.1
 **/
public class DefaultHelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello,"+name;
    }
}
