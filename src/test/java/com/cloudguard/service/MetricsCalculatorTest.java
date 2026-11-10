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

    @Test
    void completionRateHandlesZeroAdmitted() {
        assertEquals(100.0, MetricsCalculator.completionRate(20, 20), 1e-9);
        assertEquals(75.0, MetricsCalculator.completionRate(15, 20), 1e-9);
        assertEquals(0.0, MetricsCalculator.completionRate(0, 20), 1e-9);
        double z = MetricsCalculator.completionRate(0, 0); // admitted == 0 must be safe
        assertEquals(0.0, z, 1e-9);
        assertFalse(Double.isNaN(z));
        assertFalse(Double.isInfinite(z));
    }

    @Test
    void meanBasic() {
        assertEquals(0.0, MetricsCalculator.mean(new double[]{}), 1e-9);
        assertEquals(4.0, MetricsCalculator.mean(new double[]{2, 4, 6}), 1e-9);
    }

    @Test
    void sampleStdDevKnownValues() {
        assertEquals(0.0, MetricsCalculator.sampleStdDev(new double[]{}), 1e-9);      // empty
        assertEquals(0.0, MetricsCalculator.sampleStdDev(new double[]{5.0}), 1e-9);   // single value
        // sample std (n-1) of [2,4,4,4,5,5,7,9] = sqrt(32/7) = 2.138089935...
        assertEquals(2.138089935, MetricsCalculator.sampleStdDev(new double[]{2, 4, 4, 4, 5, 5, 7, 9}), 1e-6);
    }

    @Test
    void ci95HalfWidthEdgeCasesAndKnownValue() {
        assertEquals(0.0, MetricsCalculator.ci95HalfWidth(new double[]{}), 1e-9);       // empty
        assertEquals(0.0, MetricsCalculator.ci95HalfWidth(new double[]{7.0}), 1e-9);    // n = 1 -> 0 (not NaN)
        double[] v = {10, 12, 14};                 // mean 12, sample std 2, n 3, t(0.975,2) = 4.303
        double hw = MetricsCalculator.ci95HalfWidth(v);
        assertEquals(4.303 * 2.0 / Math.sqrt(3), hw, 1e-6);
        assertFalse(Double.isNaN(hw));
        double mean = MetricsCalculator.mean(v);
        assertTrue(mean - hw <= mean && mean <= mean + hw);
    }

    @Test
    void ci95HalfWidthTwoSampleDf1() {
        // n = 2 -> df = 1 -> t = 12.706. values [10,20]: sample std = sqrt(50); std/sqrt(2) = 5.
        double[] v = {10, 20};
        double hw = MetricsCalculator.ci95HalfWidth(v);
        assertEquals(12.706 * 5.0, hw, 1e-6);      // 63.53
    }

    @Test
    void ci95HalfWidthTenSampleDf9() {
        // n = 10 -> df = 9 -> t = 2.262.
        double[] v = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double hw = MetricsCalculator.ci95HalfWidth(v);
        assertEquals(2.262 * MetricsCalculator.sampleStdDev(v) / Math.sqrt(10), hw, 1e-9);
    }

    @Test
    void ciAvailableReflectsSampleCount() {
        assertFalse(MetricsCalculator.ciAvailable(new double[]{}));          // zero samples
        assertFalse(MetricsCalculator.ciAvailable(new double[]{5.0}));       // one trial -> unavailable
        assertTrue(MetricsCalculator.ciAvailable(new double[]{1.0, 2.0}));   // two trials -> available
        assertTrue(MetricsCalculator.ciAvailable(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));
    }

    @Test
    void legacyCompletionRateFallbackMatchesHelper() {
        // The frontend fallback (100 * completedTasks / admittedTasks when completionRatePercent is absent)
        // must equal the backend helper. The JS branch itself is not unit-tested in this Java/JUnit setup.
        assertEquals(100.0 * 15 / 20, MetricsCalculator.completionRate(15, 20), 1e-9);
        assertEquals(0.0, MetricsCalculator.completionRate(0, 0), 1e-9); // admitted 0 -> "—" in UI, 0.0 here
    }
}
