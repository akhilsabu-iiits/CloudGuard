package com.cloudguard.service;

import com.cloudguard.model.ExperimentResult;
import com.cloudguard.model.RunRequest;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.vms.VmSimple;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class CloudSimSimulationService {
    public record Scenario(String id, String name, int tasks, boolean protectedMode, boolean thresholdSweep) {}
    private record OneRun(int generated, int admitted, int rejected, int completed, int unfinished, int explicitlyFailed,
                          double avg, double p95, double max, double makespan, double throughput,
                          double completionRatePercent,
                          Map<String, ExperimentResult.AggregateStat> statistics,
                          List<ExperimentResult.TrialSummary> trialSummaries,
                          List<ExperimentResult.TaskResult> tasks) {}

    public List<Scenario> scenarios() {
        return List.of(
            new Scenario("S1", "Normal workload – unprotected", 100, false, false),
            new Scenario("S2", "Elevated workload – unprotected", 500, false, false),
            new Scenario("S3", "Normal workload – protected", 100, true, false),
            new Scenario("S4", "Elevated workload – protected", 500, true, false),
            new Scenario("S5", "Elevated workload – policy threshold comparison", 500, true, true)
        );
    }

    public ExperimentResult run(String experimentId, String scenarioId, RunRequest request) {
        Scenario scenario = scenarios().stream().filter(s -> s.id().equalsIgnoreCase(scenarioId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown scenario: " + scenarioId));
        RunRequest.Infrastructure infra = request.infrastructure() == null ? defaultsInfra() : request.infrastructure();
        RunRequest.Workload suppliedWorkload = request.workload() == null ? defaultsWorkload(scenario) : request.workload();
        RunRequest.Security suppliedSecurity = request.security() == null ? defaultsSecurity(scenario) : request.security();
        int trials = request.trials() == null ? 1 : Math.max(1, Math.min(20, request.trials()));
        int taskCount = suppliedWorkload.taskCount() == null ? scenario.tasks() : suppliedWorkload.taskCount();
        int duration = suppliedWorkload.durationSeconds() == null ? 60 : suppliedWorkload.durationSeconds();
        long taskLength = suppliedWorkload.taskLengthMi() == null ? 10000L : suppliedWorkload.taskLengthMi();
        long seed = suppliedWorkload.randomSeed() == null ? 42L : suppliedWorkload.randomSeed();

        List<ExperimentResult.VariantResult> variants = new ArrayList<>();
        OneRun main;
        int bucket = suppliedSecurity.bucketCapacity() == null ? 50 : suppliedSecurity.bucketCapacity();
        int refill = suppliedSecurity.refillTokensPerSecond() == null ? 5 : suppliedSecurity.refillTokensPerSecond();
        // S1/S2 must remain unprotected and S3/S4/S5 protected to preserve the research matrix.
        boolean protectedMode = scenario.protectedMode();

        if (scenario.thresholdSweep()) {
            int[][] settings = {{25, 2}, {50, 5}, {100, 10}};
            OneRun[] results = new OneRun[settings.length];
            for (int i=0; i<settings.length; i++) {
                results[i] = runTrials(infra, taskCount, duration, taskLength, seed, true, settings[i][0], settings[i][1], trials);
                OneRun r = results[i];
                variants.add(new ExperimentResult.VariantResult("Capacity " + settings[i][0] + " / refill " + settings[i][1],
                    settings[i][0], settings[i][1], r.admitted(), r.rejected(), pct(r.rejected(), r.generated()),
                    r.completed(), r.unfinished(), r.explicitlyFailed(), r.completionRatePercent(),
                    r.avg(), r.p95(), r.throughput(), r.statistics()));
            }
            main = results[1]; bucket = 50; refill = 5;
        } else {
            main = runTrials(infra, taskCount, duration, taskLength, seed, protectedMode, bucket, refill, trials);
        }

        return new ExperimentResult(experimentId, scenario.id(), scenario.name(), Instant.now(), "COMPLETED",
            main.generated(), main.admitted(), main.rejected(), pct(main.rejected(), main.generated()),
            main.completed(), main.unfinished(), main.explicitlyFailed(), main.completionRatePercent(),
            main.avg(), main.p95(), main.max(), main.makespan(), main.throughput(),
            protectedMode, protectedMode ? bucket : null, protectedMode ? refill : null, trials,
            main.statistics(), main.trialSummaries(),
            main.tasks(), variants,
            "CloudSim Plus models datacenter/host/VM task execution. Synthetic DoS-like pressure is represented as controlled Cloudlet arrivals; this is not real HTTP or network-layer DDoS traffic.");
    }

    private OneRun runTrials(RunRequest.Infrastructure infra, int count, int duration, long length, long seed,
                             boolean protectedMode, int capacity, int refill, int trials) {
        List<OneRun> runs = new ArrayList<>();
        for (int t=0; t<trials; t++) runs.add(runOnce(infra, count, duration, length, seed + t, protectedMode, capacity, refill));
        OneRun last = runs.get(runs.size()-1);
        // Derive unfinished from admitted so the invariant holds exactly even after averaging across trials.
        int admitted = last.admitted();
        int completed = Math.min(admitted, (int)Math.round(runs.stream().mapToInt(OneRun::completed).average().orElse(0)));
        int explicitlyFailed = Math.min(admitted - completed, (int)Math.round(runs.stream().mapToInt(OneRun::explicitlyFailed).average().orElse(0)));
        int unfinished = admitted - completed - explicitlyFailed;

        // Collect per-trial values for standard-deviation / confidence-interval reporting.
        int n = runs.size();
        double[] completedArr = new double[n], rateArr = new double[n], avgArr = new double[n], makeArr = new double[n], thrArr = new double[n];
        List<ExperimentResult.TrialSummary> summaries = new ArrayList<>();
        for (int t = 0; t < n; t++) {
            OneRun r = runs.get(t);
            double rate = MetricsCalculator.completionRate(r.completed(), r.admitted());
            completedArr[t] = r.completed(); rateArr[t] = rate; avgArr[t] = r.avg(); makeArr[t] = r.makespan(); thrArr[t] = r.throughput();
            summaries.add(new ExperimentResult.TrialSummary(t, seed + t, r.admitted(), r.completed(), r.unfinished(), r.explicitlyFailed(),
                rate, r.avg(), r.makespan(), r.throughput()));
        }
        Map<String, ExperimentResult.AggregateStat> stats = new LinkedHashMap<>();
        stats.put("completedTasks", agg(completedArr));
        stats.put("completionRatePercent", agg(rateArr));
        stats.put("averageCompletionTimeSeconds", agg(avgArr));
        stats.put("makespanSeconds", agg(makeArr));
        stats.put("throughputTasksPerSecond", agg(thrArr));
        double completionRate = MetricsCalculator.completionRate(completed, admitted);

        return new OneRun(last.generated(), admitted, last.rejected(),
            completed, unfinished, explicitlyFailed,
            avg(runs, OneRun::avg), avg(runs, OneRun::p95), avg(runs, OneRun::max),
            avg(runs, OneRun::makespan), avg(runs, OneRun::throughput),
            completionRate, stats, summaries, last.tasks());
    }

    private double avg(List<OneRun> runs, java.util.function.ToDoubleFunction<OneRun> f) {
        return runs.stream().mapToDouble(f).average().orElse(0);
    }

    private ExperimentResult.AggregateStat agg(double[] values) {
        return new ExperimentResult.AggregateStat(MetricsCalculator.mean(values),
            MetricsCalculator.sampleStdDev(values), MetricsCalculator.ci95HalfWidth(values),
            values.length, MetricsCalculator.ciAvailable(values));
    }

    private OneRun runOnce(RunRequest.Infrastructure i, int taskCount, int duration, long taskLength,
                           long seed, boolean protectedMode, int capacity, int refill) {
        CloudSimPlus simulation = new CloudSimPlus();
        List<HostSimple> hosts = new ArrayList<>();
        for (int h=0; h<value(i.hosts(),2); h++) {
            List<Pe> peList = new ArrayList<>();
            for (int p=0; p<value(i.pesPerHost(),4); p++) peList.add(new PeSimple(value(i.mipsPerPe(),2000L)));
            hosts.add(new HostSimple(value(i.ramMbPerHost(),16384L), 100000L, 1000000L, peList));
        }
        new DatacenterSimple(simulation, hosts);
        DatacenterBrokerSimple broker = new DatacenterBrokerSimple(simulation);
        List<VmSimple> vms = new ArrayList<>();
        for (int v=0; v<value(i.vmCount(),4); v++) {
            VmSimple vm = new VmSimple(value(i.vmMips(),2000L), value(i.pesPerVm(),1));
            vm.setRam(value(i.vmRamMb(),4096L)).setBw(10000).setSize(100000);
            // Space-shared scheduling: cloudlets queue and run to completion on a PE instead of
            // continuously time-slicing it. Under heavy oversubscription this avoids the O(n^2)
            // event rescheduling of the default time-shared scheduler, so large runs finish quickly
            // in wall-clock time. Capacity, workload and metrics are unchanged.
            vm.setCloudletScheduler(new CloudletSchedulerSpaceShared());
            vms.add(vm);
        }
        broker.submitVmList(vms);

        record Arrival(int id, double time, long length) {}
        List<Arrival> arrivals = new ArrayList<>();
        Random random = new Random(seed);
        for (int n=0; n<taskCount; n++) {
            double arrival = taskCount <= 1 ? 0 : (double)n * duration / (taskCount - 1);
            // Small deterministic task-size variation makes the workload less artificial while preserving the same distribution by seed.
            long variedLength = Math.max(1000L, (long)(taskLength * (0.9 + random.nextDouble() * 0.2)));
            arrivals.add(new Arrival(n, arrival, variedLength));
        }
        TokenBucket bucket = protectedMode ? new TokenBucket(capacity, refill) : null;
        List<Cloudlet> admitted = new ArrayList<>();
        for (Arrival a : arrivals) {
            if (!protectedMode || bucket.tryAcquire(a.time(), 1.0)) {
                CloudletSimple cloudlet = new CloudletSimple(a.length(), 1);
                cloudlet.setUtilizationModelCpu(new UtilizationModelDynamic(0.5));
                cloudlet.setUtilizationModelRam(new UtilizationModelDynamic(0.05));
                cloudlet.setUtilizationModelBw(new UtilizationModelDynamic(0.05));
                cloudlet.setSubmissionDelay(a.time());
                admitted.add(cloudlet);
            }
        }
        broker.submitCloudletList(admitted);
        simulation.start();

        List<Cloudlet> finished = broker.getCloudletFinishedList();
        List<ExperimentResult.TaskResult> taskResults = new ArrayList<>();
        List<MetricsCalculator.Completion> completions = new ArrayList<>();
        for (Cloudlet c : finished) {
            double arrival = c.getSubmissionDelay();
            double finish = c.getFinishTime();
            double start = c.getStartTime();
            double completion = Math.max(0, finish - arrival);
            completions.add(new MetricsCalculator.Completion(arrival, finish));
            taskResults.add(new ExperimentResult.TaskResult(c.getId(), arrival, start, finish, completion, c.getStatus().toString()));
        }
        // Only CloudSim-reported failures count as "explicitly failed"; other non-finished admitted tasks are "unfinished".
        int explicitlyFailed = 0;
        for (Cloudlet c : admitted) {
            Cloudlet.Status st = c.getStatus();
            if (st == Cloudlet.Status.FAILED || st == Cloudlet.Status.FAILED_RESOURCE_UNAVAILABLE) explicitlyFailed++;
        }
        double minAdmittedArrival = admitted.isEmpty() ? 0
            : admitted.stream().mapToDouble(Cloudlet::getSubmissionDelay).min().orElse(0);
        MetricsCalculator.Metrics m = MetricsCalculator.compute(admitted.size(), minAdmittedArrival, completions, explicitlyFailed);
        double completionRate = MetricsCalculator.completionRate(m.completed(), admitted.size());
        return new OneRun(taskCount, admitted.size(), taskCount - admitted.size(),
            m.completed(), m.unfinished(), m.explicitlyFailed(),
            m.averageCompletionTime(), m.p95CompletionTime(), m.maxCompletionTime(),
            m.makespan(), m.throughput(),
            completionRate, Map.of(), List.of(), taskResults);
    }

    private double pct(int n, int total) { return total == 0 ? 0 : 100.0*n/total; }
    private int value(Integer v, int d) { return v == null ? d : v; }
    private long value(Long v, long d) { return v == null ? d : v; }
    private RunRequest.Infrastructure defaultsInfra() { return new RunRequest.Infrastructure(2,4,2000L,16384L,4,1,2000L,4096L); }
    private RunRequest.Workload defaultsWorkload(Scenario s) { return new RunRequest.Workload(s.tasks(),60,10000L,42L); }
    private RunRequest.Security defaultsSecurity(Scenario s) { return new RunRequest.Security(s.protectedMode(),50,5); }
}
