package com.hzh.consumer.hook.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName HookShutdown
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/27 13:52
 * @Version 0.0.1
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HookShutdown {
    int priority() default 0;  // 你可以为注解添加属性，例如优先级
}
