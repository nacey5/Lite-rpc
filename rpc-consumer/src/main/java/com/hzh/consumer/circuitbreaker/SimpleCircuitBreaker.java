package com.hzh.consumer.circuitbreaker;

import com.hzh.rpc.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName SimpleCircuitBreaker
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/26 13:38
 * @Version 0.0.1
 **/

@Slf4j
public class SimpleCircuitBreaker implements CircuitBreaker {
    private enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private State state;
    private int failureThreshold;
    private int halfOpenInterval;
    private long maxTimeout;
    private long lastOpenedTime;
    private int consecutiveFailures;

    public SimpleCircuitBreaker(int failureThreshold, int halfOpenInterval, long maxTimeout) {
        this.state = State.CLOSED;
        this.failureThreshold = failureThreshold;
        this.halfOpenInterval = halfOpenInterval;
        this.maxTimeout = maxTimeout;
        this.lastOpenedTime = 0;
        this.consecutiveFailures = 0;
    }

    public synchronized boolean canExecute() {
        switch (state) {
            case CLOSED:
                return true;
            case OPEN:
                if ((System.currentTimeMillis() - lastOpenedTime) >= halfOpenInterval) {
                    state = State.HALF_OPEN;
                    return true;
                }
                return false;
            case HALF_OPEN:
                return true;
        }
        log.warn("Unknown state: " + state);
        return false;
    }

    public synchronized void recordFailure() {
        consecutiveFailures++;
        log.warn("recordFailure: " + consecutiveFailures);
        if (consecutiveFailures >= failureThreshold) {
            state = State.OPEN;
            lastOpenedTime = System.currentTimeMillis();
        }
    }

    public synchronized void recordSuccess() {
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            consecutiveFailures = 0;
        }
        if (consecutiveFailures > 0) {
            consecutiveFailures--;
        }
    }

    public synchronized boolean isCallTimeout(long callStartTime) {
        log.info("isCallTimeout: " + (System.currentTimeMillis() - callStartTime) + " > " + maxTimeout);
        return (System.currentTimeMillis() - callStartTime) > maxTimeout;
    }
}

