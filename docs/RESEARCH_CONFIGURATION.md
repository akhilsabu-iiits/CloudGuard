# CloudGuard — Frozen Research Configuration (Experimental Baseline)

**Project:** Performance Evaluation of Cloud Services Under Simulated DoS Workloads
**Stack:** Java 17 · Spring Boot 3.4.5 · CloudSim Plus 8.5.7 · Maven · H2 (file-backed) · vanilla-JS frontend

## Research framing (academic scope)
CloudGuard is a **CloudSim Plus computational-task workload and admission-control simulation**. It is **not** real network-layer DoS/DDoS testing. Cloudlets represent computational tasks that abstract application workload. "Task completion time" is the **simulated Cloudlet finish time minus arrival time**, not real HTTP latency, network saturation, or packet flooding. The workload models **sustained elevated computational task-arrival pressure**. Admission control **sheds computational workload**: lower completion times for admitted tasks come at the cost of **rejecting incoming tasks**; it does **not** increase physical infrastructure capacity. (Source: `modellingNote` in `CloudSimSimulationService.run()` and `researchNote()` in `app.js`.)

## Infrastructure (verified against source)
All defaults are defined in `CloudSimSimulationService.defaultsInfra()` and built in `runOnce()`.

| Parameter | Value | Source (file:line) |
|---|---|---|
| Hosts | 2 | `CloudSimSimulationService.java:212` (`defaultsInfra` arg 1) |
| Processing elements (PE) per host | 4 | `:212` (arg 2); built at `:139` |
| MIPS per PE | 2000 | `:212` (arg 3); `new PeSimple(2000)` at `:139` |
| Host RAM | 16384 MB (16 GB) | `:212` (arg 4); `new HostSimple(16384, …)` at `:140` |
| Host bandwidth / storage | 100000 / 1000000 | `:140` (fixed) |
| VMs | 4 | `:212` (arg 5) |
| PE per VM | 1 | `:212` (arg 6); `new VmSimple(2000, 1)` at `:146` |
| MIPS per VM | 2000 | `:212` (arg 7); `:146` |
| VM RAM | 4096 MB | `:212` (arg 8); `vm.setRam(4096)` at `:147` |
| VM bandwidth | 10000 | `vm.setBw(10000)` at `:147` (fixed) |
| VM size | 100000 | `vm.setSize(100000)` at `:147` (fixed) |
| Cloudlet scheduler | SpaceShared | `vm.setCloudletScheduler(new CloudletSchedulerSpaceShared())` at `:152` |
| Cloudlet CPU utilisation | 50% | `new UtilizationModelDynamic(0.5)` at `:171` |
| Cloudlet RAM utilisation | 5% | `new UtilizationModelDynamic(0.05)` at `:172` |
| Cloudlet BW utilisation | 5% | `new UtilizationModelDynamic(0.05)` at `:173` |

Concurrency note: 4 VMs × 1 PE under SpaceShared ⇒ at most 4 Cloudlets execute concurrently; the rest queue. This is the capacity bound that produces rising completion times under elevated load.

## Workload (verified against source)
Defined in `defaultsWorkload()` and generated in `runOnce()`.

| Parameter | Value | Source |
|---|---|---|
| Normal task count | 100 | `scenarios()` S1/S3 at `:33,:35` |
| Elevated task count | 500 | `scenarios()` S2/S4/S5 at `:34,:36,:37` |
| Arrival window (duration) | 60 s | `defaultsWorkload` at `:213` |
| Base task length | 10000 MI | `:213` |
| Random seed | 42 | `:213`; `new Random(seed)` at `:159` |
| Arrival spacing | uniform: `arrival = n·duration/(taskCount−1)` | `:161` |
| First / last arrival | t = 0 / t = 60 s | from `:161` (n=0 → 0; n=taskCount−1 → duration) |
| Task-length variation | seeded ±10%: `length·(0.9 + rand·0.2)`, floor 1000 MI | `:163` |

Note: the seed controls **task-length variation only**; arrival times are a deterministic function of count and window (seed-independent).

## Admission control — Token Bucket (verified against source)
Implemented in `TokenBucket.java`; applied as a deterministic pre-pass in `runOnce()` **before** `simulation.start()`.

| Property | Behaviour | Source |
|---|---|---|
| Initial tokens | starts **full** (= capacity) | `TokenBucket.java:14` |
| Refill | continuous/lazy vs. simulated time: `tokens = min(capacity, tokens + elapsed·refillPerSecond)` | `TokenBucket.java:20` |
| Cost per admitted task | 1 token | `bucket.tryAcquire(a.time(), 1.0)` at `CloudSimSimulationService.java:169`; `tokens -= cost` at `TokenBucket.java:23` |
| Evaluation time | at each task's **simulated arrival timestamp** | `:169` |
| Rejected tasks | **never submitted** to the broker (only admitted Cloudlets are built and `submitCloudletList`-ed) | `:169–178` |
| Default capacity / refill | 50 / 5 | `defaultsSecurity()` at `:214` |

**Closed-form admission (saturated demand):** `admitted = min(generated, capacity + refill × window)`, verified empirically in testing (S4: `min(500, 50+5·60)=350`).

### S5 threshold sweep configurations
`int[][] settings = {{25, 2}, {50, 5}, {100, 10}}` at `CloudSimSimulationService.java:61` — three independent simulation passes:

| Configuration | Capacity | Refill (tokens/s) | Predicted admitted (of 500) |
|---|---:|---:|---:|
| Low | 25 | 2 | 145 |
| Medium (primary) | 50 | 5 | 350 |
| High | 100 | 10 | 500 |

## Scenario matrix (verified against source `scenarios()`)
| Scenario | Workload | Tasks | Protection | Threshold sweep | Source |
|---|---|---:|---|---|---|
| S1 | Normal | 100 | Disabled | No | `:33` |
| S2 | Elevated | 500 | Disabled | No | `:34` |
| S3 | Normal | 100 | Enabled | No | `:35` |
| S4 | Elevated | 500 | Enabled | No | `:36` |
| S5 | Elevated | 500 | Enabled | Yes (25/2, 50/5, 100/10) | `:37` |

Protection is **scenario-derived** (`protectedMode = scenario.protectedMode()` at `:58`); the request's `security.enabled` field is not consulted (known limitation). S1/S2 share identical workload+infrastructure with S3/S4 respectively — the only intended difference is admission protection.

## Metrics (verified against source `MetricsCalculator.java`)
- completed = count of SUCCESS Cloudlets; unfinished = `max(0, admitted − completed − failed)`; failed = CloudSim-reported FAILED.
- average / P95 / maximum completion time: over **completed tasks only**; P95 = nearest-rank `ceil(n·0.95)−1`.
- makespan = `maxFinish − minAdmittedArrival` (0 if none complete).
- throughput = `completed / makespan` (0-safe).
- completion rate = `100·completed/admitted` (0 if admitted=0); rejection % = `100·rejected/generated`.
- Multi-trial: mean, sample standard deviation (n−1), 95% CI via Student-t (df 1–19 table, 1.96 beyond), `ciAvailable` = (n ≥ 2).
- Invariants (enforced in `runTrials`, `:93–95`): `generated = admitted + rejected`; `completed + unfinished + failed = admitted`.

## Verification outcome
**All stated baseline values match the actual source exactly — no discrepancies found.** No simulation logic, scenario definitions, metric formulas, or admission-control behaviour were modified while producing this document.
