package com.hzh.consumer.hook;

/**
 * @ClassName ShutdownHookEntry
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/27 13:42
 * @Version 0.0.1
 **/
public class ShutdownHookEntry implements Comparable<ShutdownHookEntry>{
    private final ShutdownHook hook;
    private final int priority;

    public ShutdownHookEntry(ShutdownHook hook, int priority) {
        this.hook = hook;
        this.priority = priority;
    }

    public ShutdownHook getHook() {
        return hook;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(ShutdownHookEntry other) {
        return Integer.compare(this.priority, other.priority);
    }
}
