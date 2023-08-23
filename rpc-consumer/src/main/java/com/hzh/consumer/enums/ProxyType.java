package com.hzh.consumer.enums;

/**
 * @ClassName ProxyType
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/16 18:06
 * @Version 0.0.1
 **/
public enum ProxyType {
    JDK, JAVASSIST;


    public String getName() {
        return this.name();
    }
}
