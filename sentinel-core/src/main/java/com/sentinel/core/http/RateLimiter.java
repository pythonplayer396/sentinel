package com.sentinel.core.http;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple token bucket rate limiter.
 */
public class RateLimiter {
    
    private final double permitsPerSecond;
    private final AtomicLong nextFreeTicketNanos;
    
    public RateLimiter(double permitsPerSecond) {
        if (permitsPerSecond <= 0) {
            throw new IllegalArgumentException("Rate must be positive");
        }
        this.permitsPerSecond = permitsPerSecond;
        this.nextFreeTicketNanos = new AtomicLong(System.nanoTime());
    }
    
    /**
     * Acquire a permit, blocking if necessary.
     */
    public void acquire() {
        long nowNanos = System.nanoTime();
        long nanosToWait;
        
        while (true) {
            long currentNext = nextFreeTicketNanos.get();
            long newNext = Math.max(currentNext, nowNanos) + (long) (TimeUnit.SECONDS.toNanos(1) / permitsPerSecond);
            
            if (nextFreeTicketNanos.compareAndSet(currentNext, newNext)) {
                nanosToWait = Math.max(0, currentNext - nowNanos);
                break;
            }
        }
        
        if (nanosToWait > 0) {
            try {
                TimeUnit.NANOSECONDS.sleep(nanosToWait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Get the configured rate.
     */
    public double getRate() {
        return permitsPerSecond;
    }
}
