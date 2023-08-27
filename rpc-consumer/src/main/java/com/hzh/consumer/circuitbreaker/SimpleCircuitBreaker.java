package com.hzh.consumer.circuitbreaker;

import com.hzh.consumer.enums.CircuitBreakerState;
import com.hzh.rpc.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName SimpleCircuitBreaker
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/26 13:38
 * @Version 0.0.1
 **/

@Slf4j
public class SimpleCircuitBreaker implements CircuitBreaker {

    private CircuitBreakerState state;
    private int failureThreshold;
    private int halfOpenInterval;
    private long maxTimeout;
    private long lastOpenedTime;
    private int consecutiveFailures;

    private final ReentrantLock lock = new ReentrantLock();

    public SimpleCircuitBreaker(int failureThreshold, int halfOpenInterval, long maxTimeout) {
        this.state = CircuitBreakerState.CLOSED;
        this.failureThreshold = failureThreshold;
        this.halfOpenInterval = halfOpenInterval;
        this.maxTimeout = maxTimeout;
        this.lastOpenedTime = 0;
        this.consecutiveFailures = 0;
    }

    public boolean canExecute() {
        lock.lock();
        try {
            return canExecuteLogic();
        } finally {
            lock.unlock();
        }
    }

    public void recordFailure() {
        lock.lock();
        try {
            recordFailureLogic();
        } finally {
            lock.unlock();
        }
    }


    public void recordSuccess() {
        lock.lock();
        try {
            recordSuccessLogic();
        } finally {
            lock.unlock();
        }

    }


    public boolean isCallTimeout(long callStartTime) {
        lock.lock();
        try {
            return isCallTimeoutLogic(callStartTime);
        } finally {
            lock.unlock();
        }
    }

    private void recordSuccessLogic() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            state = CircuitBreakerState.CLOSED;
            consecutiveFailures = 0;
        }
        if (consecutiveFailures > 0) {
            consecutiveFailures--;
        }
    }

    private void recordFailureLogic() {
        consecutiveFailures++;
        log.warn("recordFailure: " + consecutiveFailures);
        if (consecutiveFailures >= failureThreshold) {
            state = CircuitBreakerState.OPEN;
            lastOpenedTime = System.currentTimeMillis();
        }
    }

    private boolean canExecuteLogic() {
        switch (state) {
            case CLOSED:
                return true;
            case OPEN:
                if ((System.currentTimeMillis() - lastOpenedTime) >= halfOpenInterval) {
                    state = CircuitBreakerState.HALF_OPEN;
                    return true;
                }
                return false;
            case HALF_OPEN:
                return true;
        }
        log.warn("Unknown state: " + state);
        return false;
    }

    private boolean isCallTimeoutLogic(long callStartTime) {
        log.info("isCallTimeout: " + (System.currentTimeMillis() - callStartTime) + " > " + maxTimeout);
        return (System.currentTimeMillis() - callStartTime) > maxTimeout;
    }
}

