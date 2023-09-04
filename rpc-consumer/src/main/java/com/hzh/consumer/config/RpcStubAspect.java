package com.hzh.consumer.config;

/**
 * @ClassName RpcStubAspect
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/4 19:14
 * @Version 0.0.1
 **/

import com.hzh.consumer.controller.HelloController;
import com.hzh.rpc.local.annotations.RpcStub;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Aspect
@Component
public class RpcStubAspect {
    @Autowired
    private ApplicationContext applicationContext;

    @Before("execution(*  com.hzh.consumer.controller..*.*(..))")
    public void beforeExecute(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget();  // 获取目标对象
        Class<?> targetClass = target.getClass();  // 获取目标对象的类
        try {
            for (Field field : targetClass.getDeclaredFields()) {
                RpcStub rpcStub = field.getAnnotation(RpcStub.class);
                if (rpcStub != null) {
                    field.setAccessible(true);
                    // 获取全类名
                    Class<?> value = rpcStub.value();
                    Constructor<?> constructor = value.getConstructor();

                    Object newInstance = constructor.newInstance();
                    // 这里可以进一步初始化新创建的实例，如果需要

                    // 将新创建的实例设置到目标字段中
                    field.set(target, newInstance);
                }
            }
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

