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

    // --- Statistical helpers for repeated-trial reporting ---

    /** Completion rate as a percentage of admitted tasks; 0 when admitted == 0 (no division by zero). */
    public static double completionRate(int completed, int admitted) {
        return admitted == 0 ? 0.0 : 100.0 * completed / admitted;
    }

    /** Arithmetic mean; 0 for an empty array. */
    public static double mean(double[] values) {
        if (values.length == 0) return 0.0;
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    /** Sample standard deviation (n-1 denominator); 0 when fewer than two samples. */
    public static double sampleStdDev(double[] values) {
        int n = values.length;
        if (n <= 1) return 0.0;
        double mu = mean(values);
        double sumSq = 0;
        for (double v : values) { double d = v - mu; sumSq += d * d; }
        return Math.sqrt(sumSq / (n - 1));
    }

    /** Half-width of the 95% confidence interval using Student's t; 0 when fewer than two samples. */
    public static double ci95HalfWidth(double[] values) {
        int n = values.length;
        if (n <= 1) return 0.0;
        return tValue95(n - 1) * sampleStdDev(values) / Math.sqrt(n);
    }

    /** Whether a 95% confidence interval is computable: requires at least two trial samples. */
    public static boolean ciAvailable(double[] values) {
        return values.length >= 2;
    }

    /** Two-tailed 95% (0.975 quantile) Student's t critical value by degrees of freedom (df 1..19); 1.96 beyond the table. */
    private static double tValue95(int df) {
        double[] t = {0.0, 12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306, 2.262,
                      2.228, 2.201, 2.179, 2.160, 2.145, 2.131, 2.120, 2.110, 2.101, 2.093};
        if (df < 1) return 0.0;
        return df < t.length ? t[df] : 1.96;
    }
}
