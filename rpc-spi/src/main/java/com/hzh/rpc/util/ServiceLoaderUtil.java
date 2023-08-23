package com.hzh.rpc.util;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @ClassName ServiceLoaderUtil
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/23 14:23
 * @Version 0.0.1
 **/
public class ServiceLoaderUtil {
    public static <S> S load(Class<S> serviceClass) {
        Iterator<S> iterator = ServiceLoader.load(serviceClass).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        throw new RuntimeException("No implementation defined in /META-INF/services/ for " + serviceClass.getName());
    }
}

