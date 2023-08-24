package com.hzh.rpc.local.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName RpcMock
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/24 13:53
 * @Version 0.0.1
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RpcMock {
    Class<?> value();
}
