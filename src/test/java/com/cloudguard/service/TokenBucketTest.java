package com.cloudguard.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {
    @Test void startsFullAndRejectsWhenEmpty() {
        TokenBucket bucket = new TokenBucket(2, 1);
        assertTrue(bucket.tryAcquire(0, 1));
        assertTrue(bucket.tryAcquire(0, 1));
        assertFalse(bucket.tryAcquire(0, 1));
    }
    @Test void refillsUsingSimulationTime() {
        TokenBucket bucket = new TokenBucket(1, 2);
        assertTrue(bucket.tryAcquire(0, 1));
        assertFalse(bucket.tryAcquire(0, 1));
        assertTrue(bucket.tryAcquire(0.5, 1));
    }
    @Test void doesNotExceedCapacity() {
        TokenBucket bucket = new TokenBucket(2, 100);
        assertTrue(bucket.tryAcquire(0, 1));
        assertTrue(bucket.tryAcquire(100, 1));
        assertTrue(bucket.tryAcquire(100, 1));
        assertFalse(bucket.tryAcquire(100, 1));
    }
}
