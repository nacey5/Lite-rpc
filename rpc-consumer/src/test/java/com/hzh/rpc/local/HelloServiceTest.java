package com.hzh.rpc.local;

import com.hzh.consumer.RpcConsumerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

/**
 * @ClassName HelloServiceTest
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/24 14:11
 * @Version 0.0.1
 **/

// 单元测试
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RpcConsumerApplication.class)
public class HelloServiceTest{

        @Resource
        private HelloService helloService;  // 这应该是一个代理，使用LocalHelloServiceStub作为本地存根

        @Test
        public void testSayHello() {
            String response = helloService.sayHello("World");
            // 断言本地存根被调用
            assertEquals("Local: Hello, World", response);
        }
}
