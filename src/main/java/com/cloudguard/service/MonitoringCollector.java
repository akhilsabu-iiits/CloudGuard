package com.cloudguard.service;

import com.cloudguard.model.MonitoringData.ThroughputBucket;
import com.cloudguard.model.MonitoringData.VmSample;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pure, CloudSim-independent builder that turns real completed-cloudlet events
 * (arrival/start/finish/vmId) into monitoring time-series. Unit-testable in isolation.
 */
public final class MonitoringCollector {
    private MonitoringCollector() {}

    /** A completed cloudlet's real event times and the VM it ran on. */
    public record Exec(double arrival, double start, double finish, int vmId) {}

    /**
     * System waiting/running/completed over time plus per-VM running counts, from real event times.
     * waiting = arrived but not started; running = started but not finished; completed = finished.
     */
    public static List<VmSample> buildVmSamples(List<Exec> execs, int vmCount, int maxSamples) {
        record Ev(double t, int type, int vm) {} // 0=enqueue(arrival) 1=start 2=finish
        List<Ev> evs = new ArrayList<>();
        for (Exec e : execs) {
            evs.add(new Ev(e.arrival(), 0, e.vmId()));
            evs.add(new Ev(e.start(), 1, e.vmId()));
            evs.add(new Ev(e.finish(), 2, e.vmId()));
        }
        evs.sort(Comparator.comparingDouble(Ev::t).thenComparingInt(Ev::type));

        int waiting = 0, running = 0, completed = 0;
        int[] perVmRun = new int[Math.max(0, vmCount)];
        List<VmSample> samples = new ArrayList<>();
        int i = 0, n = evs.size();
        while (i < n) {
            double t = evs.get(i).t();
            while (i < n && evs.get(i).t() == t) {
                Ev e = evs.get(i);
                if (e.type() == 0) { waiting++; }
                else if (e.type() == 1) { if (waiting > 0) waiting--; running++; if (e.vm() >= 0 && e.vm() < perVmRun.length) perVmRun[e.vm()]++; }
                else { if (running > 0) running--; completed++; if (e.vm() >= 0 && e.vm() < perVmRun.length) perVmRun[e.vm()]--; }
                i++;
            }
            List<Integer> pv = new ArrayList<>();
            for (int k : perVmRun) pv.add(Math.max(0, k));
            samples.add(new VmSample(t, Math.max(0, waiting), Math.max(0, running), completed, pv));
        }
        return downsample(samples, maxSamples);
    }

    /** Keep at most maxSamples real samples (uniform stride); never interpolates. */
    static <T> List<T> downsample(List<T> s, int maxSamples) {
        if (maxSamples <= 0 || s.size() <= maxSamples) return s;
        List<T> out = new ArrayList<>();
        double stride = (double) s.size() / maxSamples;
        int lastIdx = -1;
        for (int i = 0; i < maxSamples; i++) {
            int idx = (int) Math.floor(i * stride);
            if (idx != lastIdx) { out.add(s.get(idx)); lastIdx = idx; }
        }
        if (lastIdx != s.size() - 1) out.add(s.get(s.size() - 1));
        return out;
    }

    /** Completed cloudlets per equal-width time bucket over [minArrival, minArrival+makespan]. */
    public static List<ThroughputBucket> buildThroughput(List<Exec> execs, double makespan, int buckets) {
        List<ThroughputBucket> out = new ArrayList<>();
        if (buckets <= 0 || makespan <= 0 || execs.isEmpty()) return out;
        double minArr = execs.stream().mapToDouble(Exec::arrival).min().orElse(0);
        double w = makespan / buckets;
        int[] counts = new int[buckets];
        for (Exec e : execs) {
            int b = (int) ((e.finish() - minArr) / w);
            if (b < 0) b = 0;
            if (b >= buckets) b = buckets - 1;
            counts[b]++;
        }
        for (int b = 0; b < buckets; b++) out.add(new ThroughputBucket(minArr + b * w, minArr + (b + 1) * w, counts[b]));
        return out;
    }

    public static List<Integer> perVmCompleted(List<Exec> execs, int vmCount) {
        int[] c = new int[Math.max(0, vmCount)];
        for (Exec e : execs) if (e.vmId() >= 0 && e.vmId() < c.length) c[e.vmId()]++;
        List<Integer> out = new ArrayList<>();
        for (int x : c) out.add(x);
        return out;
    }
}
