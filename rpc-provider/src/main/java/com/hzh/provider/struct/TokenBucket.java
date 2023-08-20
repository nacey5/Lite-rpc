package com.hzh.provider.struct;

import lombok.Data;

/**
 * @ClassName TokenBucket
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/20 20:02
 * @Version 0.0.1
 **/

@Data
public class TokenBucket {

    private static TokenBucket instance = null;

    //容量
    private final long capacity;
    //这是令牌的生成速率，表示每秒生成的令牌数量
    private final long refillRate;
    //这是当前桶中可用的令牌数量
    private long availableTokens;
    //这是上次向桶中添加令牌的时间
    private long lastRefillTime;

    public TokenBucket(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.availableTokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }

    // Public static method to get the instance of TokenBucket
    public static synchronized TokenBucket instance(long capacity, long refillRate) {
        if (instance == null) {
            instance = new TokenBucket(capacity, refillRate);
        }
        return instance;
    }

    public synchronized boolean tryAcquire() {
        refill();
        if (availableTokens > 0) {
            availableTokens--;
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        long timeElapsedInNanos = now - lastRefillTime;
        double timeElapsedInSeconds = timeElapsedInNanos / 1_000_000_000.0; // Convert nanoseconds to seconds
        long tokensToAdd = (long) Math.floor(timeElapsedInSeconds * refillRate);
        if (tokensToAdd > 0) {
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillTime = now;
        }
    }
}

