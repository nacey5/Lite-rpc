package com.hzh.consumer.hook;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * @ClassName ShutdownHookManager
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/27 13:42
 * @Version 0.0.1
 **/
public class ShutdownHookManager {
    private static volatile ShutdownHookManager instance;
    private final PriorityBlockingQueue<ShutdownHookEntry> hooks = new PriorityBlockingQueue<>();

    // 私有构造函数，确保外部无法实例化
    private ShutdownHookManager() {}

    // 提供一个全局访问点
    public static ShutdownHookManager getInstance() {
        if (instance == null) {
            synchronized (ShutdownHookManager.class) {
                if (instance == null) {
                    instance = new ShutdownHookManager();
                }
            }
        }
        return instance;
    }

    // 添加关闭钩子 注意：PriorityBlockingQueue是线程安全的
    public void addShutdownHook(ShutdownHook hook, int priority) {
        hooks.add(new ShutdownHookEntry(hook, priority));
    }

    public void shutdown() {
        while (!hooks.isEmpty()) {
            ShutdownHookEntry entry = hooks.poll();
            entry.getHook().execute();
        }
    }
}
