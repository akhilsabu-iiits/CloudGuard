package com.cloudguard.model;

import java.util.List;

/**
 * Time-series monitoring samples for one simulation run, derived entirely from real CloudSim event
 * timestamps (task arrival/start/finish, Token Bucket state at each arrival). No values are
 * interpolated or fabricated; limitations are recorded in {@link #notes()}.
 */
public record MonitoringData(
    String experimentId,
    String scenarioId,
    boolean protectionEnabled,
    Integer bucketCapacity,
    Integer refillTokensPerSecond,
    double durationSeconds,
    double makespanSeconds,
    int vmCount,
    List<WorkloadSample> workload,
    List<VmSample> vmSamples,
    List<ThroughputBucket> throughput,
    List<Integer> perVmCompleted,
    FinalCounters finalCounters,
    List<String> notes
) {
    /** One sample per generated task, at its simulated arrival time. Token fields are null when unprotected. */
    public record WorkloadSample(double t, int generated, int admitted, int rejected,
                                 Double tokensAvailable, Double tokensReplenishedCumulative) {}
    /** System state at a real event time, built from completed-cloudlet arrival/start/finish events. */
    public record VmSample(double t, int waiting, int running, int completed, List<Integer> perVmRunning) {}
    /** Completed cloudlets whose finish time falls in [tStart, tEnd). */
    public record ThroughputBucket(double tStart, double tEnd, int completed) {}
    public record FinalCounters(int generated, int admitted, int rejected, int completed, int unfinished, int failed) {}
}
