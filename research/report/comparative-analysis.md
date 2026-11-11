# CloudGuard Final Campaign — Comparative Analysis

All figures are **simulated Cloudlet completion times** (finish − arrival in CloudSim Plus), aggregated over 20 trials; `±` is the 95% CI (Student-t, df = 19). This is a computational task-workload/admission-control simulation — **not** real network-layer DoS/DDoS, HTTP latency, or packet-flood measurement.

## A. Normal workload — S1 (unprotected) vs S3 (protected)
| Metric | S1 | S3 | Δ |
|---|---:|---:|---:|
| Admitted / Rejected | 100 / 0 | 100 / 0 | 0 |
| Avg completion (sim s) | 103.608 ± 0.429 | 103.608 ± 0.429 | 0.0% |
| Makespan (sim s) | 258.985 | 258.985 | 0.0% |
| Throughput | 0.3861 | 0.3861 | 0.0% |

**Finding:** at 100 tasks the default Token Bucket (effective admission `50 + 5×60 = 350` ≥ 100) admits every task, so S1 and S3 are **identical**. Admission control is **transparent at normal load** — it imposes no rejection and no measurable overhead. The policy is correctly sized to pass the normal workload.

## B. Elevated workload — S2 (unprotected) vs S4 (protected)
| Metric | S2 | S4 | Δ (S4 vs S2) |
|---|---:|---:|---:|
| Admitted / Rejected | 500 / 0 | 350 / 150 | −150 admitted, +30% rejected |
| Rejection % | 0.0 | 30.0 | +30.0 pp |
| Avg completion (sim s) | 611.868 ± 0.919 | 424.215 ± 0.743 | **−30.7%** |
| P95 completion (sim s) | 1155.891 | 795.434 | −31.2% |
| Makespan (sim s) | 1279.555 ± 2.207 | 899.463 ± 1.826 | −29.7% |
| Throughput (tasks/sim s) | 0.3908 ± 0.0007 | 0.3891 ± 0.0008 | **−0.4%** |

**Finding:** admission control sheds 30% of the elevated workload. For the **admitted** tasks, average and P95 completion time fall by ~31% and makespan by ~30%, because fewer tasks compete for the 4-PE service capacity and queue depth is lower. **Throughput is essentially unchanged** (the two 95% CIs nearly touch and the difference is <0.5%). The S2 and S4 avg-completion 95% CIs are **non-overlapping** (611.868 ± 0.919 vs 424.215 ± 0.743), which is consistent with a real, reproducible difference rather than trial noise — though this is a confidence-interval observation, **not** a formal significance test.

## C. Token Bucket sensitivity — S5 (25/2, 50/5, 100/10)
| Config (cap/refill) | Admitted | Rejection % | Avg completion (sim s) | P95 (sim s) | Throughput |
|---|---:|---:|---:|---:|---:|
| 25 / 2 | 145 | 71.0 | 164.959 | 299.159 | 0.3835 |
| 50 / 5 | 350 | 30.0 | 424.215 | 795.434 | 0.3891 |
| 100 / 10 | 500 | 0.0 | 611.868 | 1155.891 | 0.3908 |

**Finding:** a clean **monotonic trade-off**. Tighter capacity/refill admits fewer tasks (145 → 350 → 500) and rejects more (71% → 30% → 0%), while the admitted tasks complete faster (165 → 424 → 612 s). The `100/10` configuration admits everything and reproduces the unprotected S2 behaviour (611.868 s). Throughput varies only within ~0.38–0.39 across all three configurations.

## Discussion

**Impact of elevated computational workload.** Raising the task count 5× (100→500) over the same 60 s window raises sustained arrival pressure far beyond the 4-PE concurrency. Admitted-task average completion time rises from ~104 s (S1) to ~612 s (S2) and makespan from ~259 s to ~1280 s. The impact manifests as **queueing latency**, not task failure: every admitted Cloudlet still completes (unfinished = failed = 0).

**Impact of admission control.** Admission control (S4, S5) reduces the number of admitted tasks and therefore the queue depth, cutting admitted-task completion time by ~31% at the 50/5 setting. It is **transparent** when demand is below the bucket's effective admission (normal load, S3).

**Rejection vs admitted-task completion-time trade-off.** Lower completion times for admitted tasks are **bought by rejecting incoming tasks**: S4 improves admitted-task latency by ~31% at the cost of rejecting 30% of the workload; S5 `25/2` improves it further (~165 s) but rejects 71%. There is no free lunch — the policy trades workload volume for per-task responsiveness.

**Effect of Token Bucket capacity and refill.** Admitted volume follows `min(generated, capacity + refill × window)`. Capacity sets the initial burst allowance; refill sets the sustained admission rate. Together they place the operating point on the rejection/latency curve (S5).

**Why throughput stays similar while completion times differ.** Throughput = completed / makespan is bounded by the **physical service capacity** (4 VM PEs), which every saturated configuration drives to ~0.38–0.39 tasks/sim-s. Admission control changes **how many tasks wait and for how long** (completion time, queue depth), not the rate at which the VMs retire work. Admission control **sheds computational workload; it does not increase physical infrastructure capacity.**

## Note on S4 and the primary S5 result
S4 and the **primary** S5 result both use the Token Bucket **50/5** configuration on equivalent workload, infrastructure, trials and seed. Their aggregate results are therefore **expected to match by construction** (the simulation is deterministic given these settings), and they do (avg 424.215 s, makespan 899.463 s, etc.). This is a consistency check, not duplicated data: S4 is the stand-alone protected elevated scenario, while **S5 is primarily a threshold-sensitivity experiment** evaluating three independent admission configurations (25/2, 50/5, 100/10) — it is **not** an independent fifth protection configuration. The 50/5 pass is simply the shared operating point between the two.

## Academic caveats
- Figures are simulated Cloudlet completion times, not real HTTP/network latency or availability.
- No claim of real-world DDoS prevention is made; this models admission-based load shedding for computational tasks.
- No formal statistical-significance test (e.g., t-test p-value) was run; the non-overlapping 95% CIs are reported as supporting evidence only.
