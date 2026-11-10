package com.cloudguard.service;

/** Deterministic token bucket evaluated against simulation time, not wall-clock time. */
public final class TokenBucket {
    private final double capacity;
    private final double refillPerSecond;
    private double tokens;
    private double lastRefillTime;

    public TokenBucket(double capacity, double refillPerSecond) {
        if (capacity < 1 || refillPerSecond < 0) throw new IllegalArgumentException("Invalid token bucket parameters");
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.tokens = capacity;
        this.lastRefillTime = 0;
    }

    public boolean tryAcquire(double simulationTime, double cost) {
        double elapsed = Math.max(0, simulationTime - lastRefillTime);
        tokens = Math.min(capacity, tokens + elapsed * refillPerSecond);
        lastRefillTime = simulationTime;
        if (tokens + 1e-9 < cost) return false;
        tokens -= cost;
        return true;
    }

    /** Read-only snapshot of the current token count (for monitoring; does not change behaviour). */
    public double getTokens() { return tokens; }
}
