# CloudGuard — One-Page Quick Revision (read just before the viva)

**Title:** Performance Evaluation of Cloud Services Under Simulated DoS Workloads
**One-line framing:** A CloudSim Plus *simulation* of computational-task admission control — completion time is simulated (finish − arrival), **not** real latency; admission control **sheds load, it does not add capacity**; no real DDoS traffic.

### Research objective
Measure how Token Bucket admission control affects completion time, makespan, throughput and rejection % under normal and elevated simulated workloads, and how sensitive this is to capacity/refill.

### Technology stack
Java 17 · Spring Boot 3.4.5 · CloudSim Plus 8.5.7 · H2 (file DB) · vanilla JS/HTML/CSS · Maven · localhost:8080.

### Architecture (one breath)
Browser SPA → Spring Boot REST API → CloudSim simulation service (+ Token Bucket pre-pass + MetricsCalculator) → H2 persistence. Rejected tasks are never submitted; results stored as JSON.

### Infrastructure (fixed baseline)
2 hosts × 4 PEs @ 2000 MIPS, 16 GB RAM · 4 VMs × 1 PE @ 2000 MIPS, 4 GB RAM, 10000 BW · **SpaceShared** (≤4 concurrent) · Cloudlet util CPU 50% / RAM 5% / BW 5%.

### Workload
Normal = **100** tasks, Elevated = **500**; **60 s** arrival window; **uniform** arrivals t=0…60; seeded **±10%** task-length variation (base 10000 MI); seed base 42; **20 trials** per scenario (seeds 42–61).

### Five scenarios
| S1 | normal, unprotected (100) | S2 | elevated, unprotected (500) |
|---|---|---|---|
| **S3** | normal, protected (100) | **S4** | elevated, protected (500) |
| **S5** | elevated, protected — Token Bucket **threshold sweep** (25/2, 50/5, 100/10) | | |
S1≡S3 and S2≡S4 differ only in admission protection.

### Token Bucket
Starts **full** (= capacity); refills at **refill**/simulated-second up to capacity; **1 token per admitted task**; checked at arrival time. **Admitted ≈ min(generated, capacity + refill × 60)**. Defaults 50/5.

### Key results (20-trial means; ± = 95% CI, t=2.093, df=19)
| Scenario | Admitted | Rejected | Rej % | Avg completion (sim s) | Makespan | Throughput |
|---|---:|---:|---:|---:|---:|---:|
| S1 = S3 | 100 | 0 | 0 | **103.608 ± 0.429** | 258.985 | 0.3861 |
| S2 | 500 | 0 | 0 | **611.868 ± 0.919** | 1279.555 | 0.3908 |
| S4 | 350 | 150 | 30 | **424.215 ± 0.743** | 899.463 | 0.3891 |
| S5 25/2 | 145 | 355 | 71 | 164.959 | — | 0.3835 |
| S5 50/5 | 350 | 150 | 30 | 424.215 | 899.463 | 0.3891 |
| S5 100/10 | 500 | 0 | 0 | 611.868 | 1279.555 | 0.3908 |
Experiment IDs: S1 `aa1e37f3` · S2 `35e590e4` · S3 `fe654860` · S4 `fbfe5029` · S5 `999f0e20`.

### Statistical methods
Mean · **sample** standard deviation (n−1) · **95% CI = mean ± t·SD/√n**, t=2.093 (Student-t, df=19) · ciAvailable needs ≥2 trials · all statistics independently recomputed and matched exactly.

### Five main findings
1. **Normal load:** admission is **transparent** — S1 ≡ S3 (bucket admits all 100).
2. **Elevated load:** admission rejects **30%** and cuts admitted-task completion time **~31%** (611.9 → 424.2 s) and makespan **~30%**.
3. **Throughput ≈ constant (~0.39)** — capacity-bound by the 4 PEs; admission changes queue depth, not processing rate.
4. **S5 trade-off:** tighter capacity/refill → more rejection, faster admitted-task completion (monotonic); 100/10 ≡ unprotected S2.
5. **Admission sheds load; it does not add capacity.** Lower completion time is bought by rejecting work.

### Five limitations
1. Simulation, **not** real DoS/DDoS or HTTP latency.
2. **No task loss** — every admitted task completes (impact = latency + rejection).
3. **Uniform** arrivals (not bursty/Poisson); seed varies length only.
4. **No formal significance test** — CIs only (non-overlapping S2/S4 CIs are *evidence*, not significance).
5. Max/P95 are **cross-trial means**; single-node H2; `security.enabled` is scenario-derived.

### Three lines you must never cross
- Don't call it real DDoS testing or real latency.
- Don't claim admission control adds capacity or improves throughput.
- Don't claim statistical significance without a formal test.
