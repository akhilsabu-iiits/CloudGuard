# CloudGuard Final Campaign — Experiment Results

**Campaign:** 5 scenarios × **20 trials each** (trial seeds 42–61), executed sequentially via `POST /api/experiments/run`.
**Framing:** All completion-time figures are **simulated Cloudlet completion time** (finish − arrival time in CloudSim Plus), **not** real HTTP/network latency. Values are 20-trial aggregates; `±` is the 95% confidence interval (Student-t, df = 19, t = 2.093). Independent recomputation of the statistics matched the API exactly (see `campaign-validation.md`).

## Experiment IDs (new records; existing 10 preserved → DB total 15)
| Scenario | Experiment ID | Workload | Protection |
|---|---|---|---|
| S1 | `aa1e37f3-24ae-4853-bc51-cbaf12344864` | Normal, 100 | Disabled |
| S2 | `35e590e4-12a1-4630-8c05-08dfb0584fe6` | Elevated, 500 | Disabled |
| S3 | `fe654860-39ff-45d3-a271-e7b4feffe96c` | Normal, 100 | Enabled |
| S4 | `fbfe5029-f53d-4e60-a6fa-94b6b4a0aa9f` | Elevated, 500 | Enabled |
| S5 | `999f0e20-45e6-4533-ab1c-96ff84df6ae2` | Elevated, 500 | Threshold sweep |

## Aggregate results (mean over 20 trials)
| Metric | S1 | S2 | S3 | S4 | S5 (main, 50/5) |
|---|---:|---:|---:|---:|---:|
| Generated | 100 | 500 | 100 | 500 | 500 |
| Admitted | 100 | 500 | 100 | 350 | 350 |
| Rejected | 0 | 0 | 0 | 150 | 150 |
| Rejection % | 0.0 | 0.0 | 0.0 | 30.0 | 30.0 |
| Completed | 100 | 500 | 100 | 350 | 350 |
| Unfinished / Failed | 0 / 0 | 0 / 0 | 0 / 0 | 0 / 0 | 0 / 0 |
| Completion rate % | 100.0 | 100.0 | 100.0 | 100.0 | 100.0 |
| **Avg completion (sim s)** | **103.608 ± 0.429** | **611.868 ± 0.919** | **103.608 ± 0.429** | **424.215 ± 0.743** | **424.215 ± 0.743** |
| P95 completion — mean of per-trial P95 (sim s)¹ | 189.259 | 1155.891 | 189.259 | 795.434 | 795.434 |
| Max completion — mean of per-trial maxima (sim s)¹ | 199.954 | 1219.723 | 199.954 | 839.536 | 839.536 |
| Makespan (sim s) | 258.985 ± 1.021 | 1279.555 ± 2.207 | 258.985 ± 1.021 | 899.463 ± 1.826 | 899.463 ± 1.826 |
| Throughput (tasks/sim s) | 0.3861 ± 0.0015 | 0.3908 ± 0.0007 | 0.3861 ± 0.0015 | 0.3891 ± 0.0008 | 0.3891 ± 0.0008 |

¹ Max is the **cross-trial mean of per-trial maxima** (see limitations). P95 is likewise a cross-trial mean; no separate CI is produced for P95/Max by the implementation.

## S5 Token Bucket threshold sweep (three independent admission passes, 500 elevated tasks)
| Configuration (cap/refill) | Admitted | Rejected | Rejection % | Completed | Avg completion (sim s) | P95 (sim s) | Throughput |
|---|---:|---:|---:|---:|---:|---:|---:|
| 25 / 2 | 145 | 355 | 71.0 | 145 | 164.959 | 299.159 | 0.3835 |
| 50 / 5 | 350 | 150 | 30.0 | 350 | 424.215 | 795.434 | 0.3891 |
| 100 / 10 | 500 | 0 | 0.0 | 500 | 611.868 | 1155.891 | 0.3908 |

Admission closed form (saturated demand) `admitted = min(generated, capacity + refill × 60)` holds exactly: 145, 350, 700→cap 500.

## Observations (raw)
- **S1 ≡ S3** (identical to 3 decimals): at normal load the default bucket (effective capacity 50 + 5·60 = 350) admits all 100 tasks, so protection is transparent and rejects nothing.
- **S2 vs S4:** admission control rejects 30% of elevated load; admitted-task average completion time falls 611.868 → 424.215 s (−30.7%) and makespan 1279.555 → 899.463 s, while throughput is essentially unchanged (0.3908 → 0.3891, −0.4%).
- **S5 sweep:** monotonic trade-off — tighter capacity/refill ⇒ more rejection (71→30→0%) and lower admitted-task completion time (165→424→612 s); throughput stays ≈0.38–0.39.
- All invariants hold for every trial; completed = admitted and unfinished = failed = 0 throughout (the simulation runs all admitted Cloudlets to completion).

## Artifacts
- Raw API responses: `../experiments/results/raw/S1.json … S5.json`
- Per-task CSV exports: `../experiments/exports/S1-tasks.csv … S5-tasks.csv` (rows = completed tasks: 100/500/100/350/350)
- Charts: `../experiments/charts/` (SVG) and `../experiments/charts/png/` (PNG)
- Machine-readable summary: `../experiments/results/summary.json`
