package com.hzh.rpc.local;

import com.hzh.rpc.local.annotations.RpcMock;
import com.hzh.rpc.local.annotations.RpcStub;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ServiceHandler
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/24 15:16
 * @Version 0.0.1
 **/
public class ServiceHandler {
    private final Map<Method, Object> methodHandlers = new HashMap<>();

    public ServiceHandler(Class<?> serviceInterface, Object remoteService, Object mockService, Object stubService) {
        for (Method method : serviceInterface.getMethods()) {
            if (method.isAnnotationPresent(RpcMock.class)) {
                methodHandlers.put(method, mockService);
            } else if (method.isAnnotationPresent(RpcStub.class)) {
                methodHandlers.put(method, stubService);
            } else {
                methodHandlers.put(method, remoteService);
            }
        }
    }

    public Object handle(Method method, Object[] args) throws Exception {
        Object service = methodHandlers.get(method);
        if (service != null) {
            return method.invoke(service, args);
        }
        throw new UnsupportedOperationException("No handler for method: " + method.getName());
    }
}

