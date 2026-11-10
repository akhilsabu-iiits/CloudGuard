package com.cloudguard.service;

import java.util.ArrayList;
import java.util.List;

/** Pure, CloudSim-independent experiment metric calculations, unit-testable in isolation. */
public final class MetricsCalculator {
    private MetricsCalculator() {}

    /** Arrival/finish pair for a successfully completed cloudlet. */
    public record Completion(double arrival, double finish) {}

    public record Metrics(int admitted, int completed, int unfinished, int explicitlyFailed,
                          double averageCompletionTime, double p95CompletionTime, double maxCompletionTime,
                          double makespan, double throughput) {}

    /**
     * @param admittedCount      cloudlets that passed the Token Bucket
     * @param minAdmittedArrival earliest arrival among admitted (workload-window start)
     * @param completions        arrival/finish of SUCCESS cloudlets
     * @param explicitlyFailed   cloudlets CloudSim reported as FAILED
     */
    public static Metrics compute(int admittedCount, double minAdmittedArrival,
                                  List<Completion> completions, int explicitlyFailed) {
        int completed = completions.size();
        int unfinished = Math.max(0, admittedCount - completed - explicitlyFailed);

        List<Double> times = new ArrayList<>();
        double maxFinish = 0, maxCompletion = 0;
        for (Completion c : completions) {
            double ct = Math.max(0, c.finish() - c.arrival());   // completion = finish - arrival
            times.add(ct);
            maxFinish = Math.max(maxFinish, c.finish());
            maxCompletion = Math.max(maxCompletion, ct);
        }
        times.sort(Double::compareTo);
        double mean = times.isEmpty() ? 0 : times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double p95 = times.isEmpty() ? 0 : times.get(Math.max(0, (int) Math.ceil(times.size() * 0.95) - 1));

        double makespan = completed == 0 ? 0 : Math.max(0, maxFinish - minAdmittedArrival);
        double throughput = makespan > 0 ? completed / makespan : 0;

        return new Metrics(admittedCount, completed, unfinished, explicitlyFailed,
            mean, p95, maxCompletion, makespan, throughput);
    }
}
