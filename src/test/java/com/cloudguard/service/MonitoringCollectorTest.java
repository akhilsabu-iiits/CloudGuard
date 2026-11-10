package com.cloudguard.service;

import com.cloudguard.model.MonitoringData.ThroughputBucket;
import com.cloudguard.model.MonitoringData.VmSample;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringCollectorTest {

    private List<MonitoringCollector.Exec> sample() {
        // 4 completed cloudlets across 2 VMs with real arrival/start/finish times.
        List<MonitoringCollector.Exec> e = new ArrayList<>();
        e.add(new MonitoringCollector.Exec(0, 0, 10, 0));
        e.add(new MonitoringCollector.Exec(1, 10, 20, 0));
        e.add(new MonitoringCollector.Exec(2, 2, 12, 1));
        e.add(new MonitoringCollector.Exec(3, 12, 22, 1));
        return e;
    }

    @Test
    void perVmCompletedReconciles() {
        List<Integer> pv = MonitoringCollector.perVmCompleted(sample(), 2);
        assertEquals(List.of(2, 2), pv);
        assertEquals(4, pv.stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void vmSamplesReachFinalCompletedAndNeverGoNegative() {
        List<VmSample> s = MonitoringCollector.buildVmSamples(sample(), 2, 300);
        assertFalse(s.isEmpty());
        // Final sample's cumulative completed must equal the number of completed cloudlets.
        assertEquals(4, s.get(s.size() - 1).completed());
        for (VmSample v : s) {
            assertTrue(v.waiting() >= 0);
            assertTrue(v.running() >= 0);
            assertTrue(v.completed() >= 0 && v.completed() <= 4);
            assertTrue(v.perVmRunning().stream().allMatch(x -> x >= 0));
        }
    }

    @Test
    void throughputBucketsSumToTotalCompleted() {
        List<ThroughputBucket> t = MonitoringCollector.buildThroughput(sample(), 22, 11);
        int sum = t.stream().mapToInt(ThroughputBucket::completed).sum();
        assertEquals(4, sum);
    }

    @Test
    void emptyInputsAreSafe() {
        assertTrue(MonitoringCollector.buildVmSamples(List.of(), 2, 300).isEmpty());
        assertTrue(MonitoringCollector.buildThroughput(List.of(), 0, 10).isEmpty());
        assertEquals(List.of(0, 0), MonitoringCollector.perVmCompleted(List.of(), 2));
    }

    @Test
    void downsampleKeepsBoundAndEndpoints() {
        List<Integer> big = new ArrayList<>();
        for (int i = 0; i < 1000; i++) big.add(i);
        List<Integer> small = MonitoringCollector.downsample(big, 100);
        assertTrue(small.size() <= 101);
        assertEquals(Integer.valueOf(0), small.get(0));
        assertEquals(Integer.valueOf(999), small.get(small.size() - 1)); // last real sample preserved
    }
}
