package com.hzh.consumer.controller.local;

import com.hzh.provider.facade.HelloFacade;
import com.hzh.rpc.local.LocalMock;
import com.hzh.rpc.local.LocalStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * @ClassName helloFacadeLocalImpl
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/24 14:21
 * @Version 0.0.1
 **/
//@Service
@Slf4j
//@Service
public class HelloFacadeLocalImpl implements HelloFacade {
    @Override
    public String hello(String name) {
        log.info("HelloFacadeLocalImpl.hello() is called");
        return "Hello, LOCAL, " + name;
    }
}
