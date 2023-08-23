package com.hzh.rpc;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName RpcContext
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/24 0:22
 * @Version 0.0.1
 **/
public class RpcContext {
    private static final ThreadLocal<RpcContext> CONTEXT = ThreadLocal.withInitial(RpcContext::new);

    private final Map<String, Object> data = new HashMap<>();

    private RpcContext() {}

    public static RpcContext getContext() {
        return CONTEXT.get();
    }

    public static void removeContext() {
        CONTEXT.remove();
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    // 可以添加其他常用的快捷方法，如getRequestId(), getAuthToken()等
}
