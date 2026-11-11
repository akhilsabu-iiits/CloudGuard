# CloudGuard Final Campaign — Validation Report

Independent verification of the five 20-trial experiments against the API responses. All metrics are **simulated Cloudlet completion times** (finish − arrival), not real network latency.

## S1 — id `aa1e37f3-24ae-4853-bc51-cbaf12344864` (trials=20)
- Aggregate invariants: gen==adm+rej **True**, comp+unf+fail==adm **True**
- Per-trial invariant (all 20 trials): **True**; seeds 42…61 no-duplicates **True**
- Stats (avg completion): recompute mean=103.607633 sd(n-1)=0.916114 ci95(t=2.093,df=19)=0.428749; API mean=103.607633 sd=0.916114 ci95=0.428749 samples=20 ciAvailable=True → **MATCH True**
- Formulas: completionRate=100.0000% rejection=0.0000% throughput=0.386123 → **MATCH True**

## S2 — id `35e590e4-12a1-4630-8c05-08dfb0584fe6` (trials=20)
- Aggregate invariants: gen==adm+rej **True**, comp+unf+fail==adm **True**
- Per-trial invariant (all 20 trials): **True**; seeds 42…61 no-duplicates **True**
- Stats (avg completion): recompute mean=611.868241 sd(n-1)=1.963169 ci95(t=2.093,df=19)=0.918781; API mean=611.868241 sd=1.963169 ci95=0.918781 samples=20 ciAvailable=True → **MATCH True**
- Formulas: completionRate=100.0000% rejection=0.0000% throughput=0.390761 → **MATCH True**

## S3 — id `fe654860-39ff-45d3-a271-e7b4feffe96c` (trials=20)
- Aggregate invariants: gen==adm+rej **True**, comp+unf+fail==adm **True**
- Per-trial invariant (all 20 trials): **True**; seeds 42…61 no-duplicates **True**
- Stats (avg completion): recompute mean=103.607633 sd(n-1)=0.916114 ci95(t=2.093,df=19)=0.428749; API mean=103.607633 sd=0.916114 ci95=0.428749 samples=20 ciAvailable=True → **MATCH True**
- Formulas: completionRate=100.0000% rejection=0.0000% throughput=0.386123 → **MATCH True**

## S4 — id `fbfe5029-f53d-4e60-a6fa-94b6b4a0aa9f` (trials=20)
- Aggregate invariants: gen==adm+rej **True**, comp+unf+fail==adm **True**
- Per-trial invariant (all 20 trials): **True**; seeds 42…61 no-duplicates **True**
- Stats (avg completion): recompute mean=424.214675 sd(n-1)=1.588156 ci95(t=2.093,df=19)=0.743271; API mean=424.214675 sd=1.588156 ci95=0.743271 samples=20 ciAvailable=True → **MATCH True**
- Formulas: completionRate=100.0000% rejection=30.0000% throughput=0.389121 → **MATCH True**

## S5 — id `999f0e20-45e6-4533-ab1c-96ff84df6ae2` (trials=20)
- Aggregate invariants: gen==adm+rej **True**, comp+unf+fail==adm **True**
- Per-trial invariant (all 20 trials): **True**; seeds 42…61 no-duplicates **True**
- Stats (avg completion): recompute mean=424.214675 sd(n-1)=1.588156 ci95(t=2.093,df=19)=0.743271; API mean=424.214675 sd=1.588156 ci95=0.743271 samples=20 ciAvailable=True → **MATCH True**
- Formulas: completionRate=100.0000% rejection=30.0000% throughput=0.389121 → **MATCH True**

- S5 variants (independent passes):
  - Capacity 25 / refill 2: gen(adm+rej)=500 adm=145 rej=355 (71.0%) comp=145 avg=164.96s invariant_gen500=True
  - Capacity 50 / refill 5: gen(adm+rej)=500 adm=350 rej=150 (30.0%) comp=350 avg=424.21s invariant_gen500=True
  - Capacity 100 / refill 10: gen(adm+rej)=500 adm=500 rej=0 (0.0%) comp=500 avg=611.87s invariant_gen500=True

## Verdict
- **All scenarios validated: True**
- Rejected tasks are never submitted to CloudSim (only admitted Cloudlets are built/submitted — source `CloudSimSimulationService.runOnce`), so rejected tasks have no completion time and are excluded from average/P95/maximum (computed over completed/SUCCESS Cloudlets only).
- Zero-denominator handling verified: completionRate=0 when admitted=0, throughput=0 when makespan=0 (source `MetricsCalculator`).

## Aggregation limitations (documented, not altered)
- Across trials, `completedTasks` is the **rounded mean** over trials, clamped to admitted; `unfinishedTasks`/`failedTasks` are then derived so the invariant holds exactly (source `runTrials`).
- `maxCompletionTimeSeconds` reported at the main level is the **mean of per-trial maxima** (avg of `OneRun::max`), not a single global maximum.
- Per-task `tasks[]` (used by CSV export) reflect the **last trial** only; aggregate statistics use all trials via `trialSummaries` + the `statistics` map.
- Completion-time `statistics` are provided for averageCompletionTime, completedTasks, completionRatePercent, makespan, throughput; P95 is reported as a cross-trial mean (no separate CI for P95).