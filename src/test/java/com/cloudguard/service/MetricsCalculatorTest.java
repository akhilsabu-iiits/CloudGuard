package com.cloudguard.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCalculatorTest {

    @Test
    void fullCompletionAllAdmittedFinish() {
        var completions = List.of(
            new MetricsCalculator.Completion(0, 10),
            new MetricsCalculator.Completion(1, 11),
            new MetricsCalculator.Completion(2, 12),
            new MetricsCalculator.Completion(3, 13));
        var m = MetricsCalculator.compute(4, 0.0, completions, 0);
        assertEquals(4, m.completed());
        assertEquals(0, m.unfinished());
        assertEquals(0, m.explicitlyFailed());
        assertEquals(4, m.completed() + m.unfinished() + m.explicitlyFailed()); // == admitted
        assertEquals(10.0, m.averageCompletionTime(), 1e-9);
        assertEquals(10.0, m.maxCompletionTime(), 1e-9);
        assertEquals(13.0, m.makespan(), 1e-9);          // maxFinish(13) - minArrival(0)
        assertEquals(4.0 / 13.0, m.throughput(), 1e-9);  // completed / makespan
    }

    @Test
    void partialCompletionCountsUnfinished() {
        var completions = List.of(
            new MetricsCalculator.Completion(0, 20),
            new MetricsCalculator.Completion(1, 21),
            new MetricsCalculator.Completion(2, 22),
            new MetricsCalculator.Completion(3, 23),
            new MetricsCalculator.Completion(4, 24),
            new MetricsCalculator.Completion(5, 25));
        var m = MetricsCalculator.compute(10, 0.0, completions, 0);
        assertEquals(6, m.completed());
        assertEquals(4, m.unfinished());
        assertEquals(0, m.explicitlyFailed());
        assertEquals(10, m.completed() + m.unfinished() + m.explicitlyFailed());
        assertEquals(25.0, m.makespan(), 1e-9);          // full admitted window start (0) -> last finish (25)
        assertEquals(6.0 / 25.0, m.throughput(), 1e-9);
    }

    @Test
    void zeroCompletionNoDivisionByZero() {
        var m = MetricsCalculator.compute(5, 0.0, List.of(), 0);
        assertEquals(0, m.completed());
        assertEquals(5, m.unfinished());
        assertEquals(0, m.explicitlyFailed());
        assertEquals(5, m.completed() + m.unfinished() + m.explicitlyFailed());
        assertEquals(0.0, m.makespan(), 1e-9);
        assertEquals(0.0, m.throughput(), 1e-9);
        assertEquals(0.0, m.averageCompletionTime(), 1e-9);
        assertEquals(0.0, m.p95CompletionTime(), 1e-9);
        assertFalse(Double.isNaN(m.throughput()));
        assertFalse(Double.isInfinite(m.throughput()));
    }

    @Test
    void explicitFailuresSeparatedFromUnfinished() {
        var completions = new ArrayList<MetricsCalculator.Completion>();
        for (int k = 0; k < 6; k++) completions.add(new MetricsCalculator.Completion(0, 10));
        var m = MetricsCalculator.compute(10, 0.0, completions, 2); // 6 done, 2 failed -> 2 unfinished
        assertEquals(6, m.completed());
        assertEquals(2, m.explicitlyFailed());
        assertEquals(2, m.unfinished());
        assertEquals(10, m.completed() + m.unfinished() + m.explicitlyFailed());
    }

    @Test
    void completionTimeIsFinishMinusArrival() {
        var m = MetricsCalculator.compute(1, 2.0, List.of(new MetricsCalculator.Completion(2, 12)), 0);
        assertEquals(10.0, m.maxCompletionTime(), 1e-9);
        assertEquals(10.0, m.averageCompletionTime(), 1e-9);
        assertEquals(10.0, m.makespan(), 1e-9);          // finish(12) - minArrival(2)
    }

    @Test
    void makespanUsesEarliestAdmittedArrivalNotEarliestCompleted() {
        // Admitted window starts at 0, but the only completed task arrived at 5 and finished at 30.
        // Makespan must span 0 -> 30 (= 30), NOT 30 - 5 (= 25).
        var m = MetricsCalculator.compute(3, 0.0, List.of(new MetricsCalculator.Completion(5, 30)), 0);
        assertEquals(30.0, m.makespan(), 1e-9);
    }

    @Test
    void counterInvariantHoldsForVariousInputs() {
        for (int completed = 0; completed <= 5; completed++) {
            for (int failed = 0; failed + completed <= 10; failed++) {
                var comps = new ArrayList<MetricsCalculator.Completion>();
                for (int k = 0; k < completed; k++) comps.add(new MetricsCalculator.Completion(0, 1));
                var m = MetricsCalculator.compute(10, 0.0, comps, failed);
                assertEquals(10, m.completed() + m.unfinished() + m.explicitlyFailed(), "invariant must hold");
                assertTrue(m.unfinished() >= 0);
            }
        }
    }
}
