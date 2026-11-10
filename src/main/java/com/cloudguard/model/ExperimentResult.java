package com.cloudguard.model;

import java.time.Instant;
import java.util.List;

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
    double averageCompletionTimeSeconds,
    double p95CompletionTimeSeconds,
    double maxCompletionTimeSeconds,
    double makespanSeconds,
    double throughputTasksPerSecond,
    boolean protectionEnabled,
    Integer bucketCapacity,
    Integer refillTokensPerSecond,
    int trialCount,
    List<TaskResult> tasks,
    List<VariantResult> variants,
    String modellingNote
) {
    public record TaskResult(long taskId, double arrivalTimeSeconds, double startTimeSeconds,
                             double finishTimeSeconds, double completionTimeSeconds, String status) {}
    public record VariantResult(String label, int bucketCapacity, int refillTokensPerSecond,
                                int admittedTasks, int rejectedTasks, double rejectedWorkloadPercent,
                                int completedTasks, int unfinishedTasks, int failedTasks,
                                double averageCompletionTimeSeconds,
                                double p95CompletionTimeSeconds, double throughputTasksPerSecond) {}
}
