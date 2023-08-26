package com.hzh.consumer.factory;

import com.hzh.consumer.circuitbreaker.SimpleCircuitBreaker;
import com.hzh.rpc.circuitbreaker.CircuitBreaker;

/**
 * @ClassName CircuitBreakerFactory
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/26 15:08
 * @Version 0.0.1
 **/
public class CircuitBreakerFactory {

    public enum BreakerType {
        SIMPLE
        // 其他类型可以在这里添加，例如：ADVANCED, RATE_LIMITER, etc.
    }

    public static CircuitBreaker createCircuitBreaker(BreakerType type, int failureThreshold, int halfOpenInterval, long maxTimeout) {
        switch (type) {
            case SIMPLE:
                return new SimpleCircuitBreaker(failureThreshold, halfOpenInterval, maxTimeout);
            // 当你有其他类型的熔断器时，可以在这里添加相应的case
            default:
                throw new IllegalArgumentException("Unknown breaker type: " + type);
        }
    }
}

