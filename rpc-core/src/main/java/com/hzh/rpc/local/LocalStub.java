package com.hzh.rpc.local;

import java.lang.reflect.Method;

/**
 * @ClassName LocalStub
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/24 13:51
 * @Version 0.0.1
 **/
public interface LocalStub <T>{
    Object invoke(T proxy, Method method, Object[] args) throws Throwable;
}
