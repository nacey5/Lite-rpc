package com.hzh.rpc.circuitbreaker;

/**
 * @ClassName CircuitBreaker
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/26 14:51
 * @Version 0.0.1
 **/
public interface CircuitBreaker {
    boolean canExecute();
    void recordFailure();
    void recordSuccess();
    boolean isCallTimeout(long callStartTime);
}
