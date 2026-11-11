# CloudGuard Final Campaign — Experimental Configuration

This document records the configuration used for the final 20-trial campaign. The full, source-verified platform baseline is in [`docs/RESEARCH_CONFIGURATION.md`](../../docs/RESEARCH_CONFIGURATION.md) and is **not restated in full here**; this file records only the campaign-specific choices and references the frozen values.

## Platform (frozen baseline — verified from source, no discrepancies)
- Infrastructure: 2 hosts × 4 PE @ 2000 MIPS, 16384 MB host RAM; 4 VMs × 1 PE @ 2000 MIPS, 4096 MB VM RAM, 10000 BW; `CloudletSchedulerSpaceShared` (≤4 concurrent Cloudlets).
- Cloudlet utilisation: CPU 50%, RAM 5%, BW 5%.
- Workload: normal = 100 tasks, elevated = 500 tasks; 60 s arrival window; uniform arrivals t = 0 … 60; seeded ±10% task-length variation (base 10000 MI).
- Admission: Token Bucket starts full, refills continuously vs simulated time, 1 token per admitted task, evaluated at each task's arrival timestamp **before** `simulation.start()`; rejected tasks never submitted.
- Source: `CloudSimSimulationService` (`defaultsInfra`:212, `runOnce`:135–207, scenarios:31–39), `TokenBucket`, `MetricsCalculator`.

## Campaign parameters
| Parameter | Value |
|---|---|
| Trials per scenario | **20** |
| Trial seeds | 42, 43, …, 61 (base 42 + trial index) |
| Scenarios | S1, S2, S3, S4, S5 |
| Infrastructure / workload / duration | defaults (identical across all scenarios) |
| Execution | sequential, one `POST /api/experiments/run` per scenario, body `{"scenarioId":"S<n>","trials":20}` |
| S5 configurations | 25/2, 50/5, 100/10 (each an independent pass) |

## Equivalence (as required)
Because every scenario used the **same defaults** (infrastructure, 60 s window, 10000 MI base length, seed base 42, trials = 20):
- **S1 and S3** are equivalent in infrastructure, workload, trial count and seed — the only difference is admission protection (disabled vs enabled).
- **S2 and S4** are likewise equivalent — only admission protection differs.
- **S5** evaluates 25/2, 50/5, 100/10 independently on the same workload/infrastructure.

For a given seed, S1 and S3 (and S2 and S4) generate an **identical** set of task arrivals and lengths before admission; admission then filters, so the comparison is apples-to-apples.

## Reproducibility
The simulation is deterministic given (scenario, infrastructure, workload, seed). Re-running any scenario with the same parameters reproduces the same per-trial results (trial `t` uses seed `42 + t`). No simulation logic, scenario definitions, metric formulas, or admission-control behaviour were modified during the campaign.

## Not modified
- Database schema, existing records (10 preserved), simulation engine, Token Bucket, metric formulas, scenario definitions, workload parameters.
- No deployment; no Git staging/commit/push; reset endpoint never called.
