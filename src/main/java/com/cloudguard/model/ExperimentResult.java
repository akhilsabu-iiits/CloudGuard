package com.cloudguard.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ExperimentResult(
    String experimentId,
    String scenarioId,
    String scenarioName,
    Instant createdAt,
    String status,
    int generatedTasks,
    int admittedTasks,
    int rejectedTasks,
    double rejectedWorkloadPercent,
    int completedTasks,
    int unfinishedTasks,
    int failedTasks,
    double completionRatePercent,
    double averageCompletionTimeSeconds,
    double p95CompletionTimeSeconds,
    double maxCompletionTimeSeconds,
    double makespanSeconds,
    double throughputTasksPerSecond,
    boolean protectionEnabled,
    Integer bucketCapacity,
    Integer refillTokensPerSecond,
    int trialCount,
    Map<String, AggregateStat> statistics,
    List<TrialSummary> trialSummaries,
    List<TaskResult> tasks,
    List<VariantResult> variants,
    String modellingNote
) {
    public record TaskResult(long taskId, double arrivalTimeSeconds, double startTimeSeconds,
                             double finishTimeSeconds, double completionTimeSeconds, String status) {}
    public record VariantResult(String label, int bucketCapacity, int refillTokensPerSecond,
                                int admittedTasks, int rejectedTasks, double rejectedWorkloadPercent,
                                int completedTasks, int unfinishedTasks, int failedTasks,
                                double completionRatePercent,
                                double averageCompletionTimeSeconds,
                                double p95CompletionTimeSeconds, double throughputTasksPerSecond,
                                Map<String, AggregateStat> statistics) {}
    /** Aggregate of one metric across trials: mean, sample std dev, half-width of the 95% CI, sample count,
     *  and whether the CI is available (true only when at least two trial samples exist). ci95HalfWidth is
     *  retained for compatibility and is 0 when ciAvailable is false. */
    public record AggregateStat(double mean, double stdDev, double ci95HalfWidth, int samples, boolean ciAvailable) {}
    /** Compact per-trial summary (counts + key metrics only; no per-task records).
     *  Includes admittedTasks so that completedTasks + unfinishedTasks + failedTasks = admittedTasks is self-verifiable. */
    public record TrialSummary(int trial, long seed, int admittedTasks, int completedTasks, int unfinishedTasks, int failedTasks,
                               double completionRatePercent, double averageCompletionTimeSeconds,
                               double makespanSeconds, double throughputTasksPerSecond) {}
}
