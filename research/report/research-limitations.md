# CloudGuard Final Campaign — Research Limitations

These limitations are inherent to the model and implementation. They are documented honestly; **no implementation, formula, or parameter was changed to obtain favourable results.**

## Modelling scope (academic distinction)
1. **Simulation, not real DoS/DDoS.** CloudGuard uses CloudSim Plus to simulate computational tasks (Cloudlets). It does **not** send real HTTP requests, generate network packets, saturate links, or measure real-world application availability. "DoS-like pressure" means **sustained elevated computational task-arrival pressure**.
2. **Completion time is simulated.** Reported completion time = Cloudlet **finish time − arrival time** inside the discrete-event simulation. It is not real HTTP/network latency and must not be presented as such.
3. **Admission control sheds load; it does not add capacity.** Lower admitted-task completion times come at the cost of **rejecting** incoming tasks. The policy does not increase the number of VMs/PEs or physical throughput.

## Simulation-model limitations
4. **No task loss under load.** The simulation runs every admitted Cloudlet to completion, so `unfinished = failed = 0` in all results. DoS impact appears as **latency and rejection**, not dropped/failed tasks. Scenarios therefore cannot illustrate task-failure behaviour.
5. **Deterministic uniform arrivals.** Arrivals are evenly spaced over the 60 s window (`n·duration/(taskCount−1)`); they are **not** Poisson or bursty. The seed controls only ±10% task-length variation, not arrival timing. Results reflect a sustained constant-rate workload, not traffic spikes.
6. **Admission is a pre-pass, not an in-engine event.** The Token Bucket is evaluated analytically over the arrival schedule before `simulation.start()`. It uses the same arrival timestamps as the Cloudlet submission delays, but is not driven by the CloudSim event loop.
7. **`security.enabled` is ignored by the engine.** Protection is scenario-derived (`protectedMode = scenario.protectedMode()`); the request's `enabled` flag is not consulted. (Documented known behaviour; unchanged.)

## Metric / aggregation limitations
8. **Cross-trial aggregation.** Over trials, `completedTasks` is the rounded mean clamped to admitted; `unfinished`/`failed` are then derived so the invariant holds exactly. `maxCompletionTime` at the main level is the **mean of per-trial maxima**, not a single global maximum. P95 is a **cross-trial mean** of per-trial P95; no separate CI is produced for P95 or Max.
9. **Per-task CSV = last trial only.** `tasks[]` (hence CSV export rows) reflect the **last** trial's Cloudlets; aggregate statistics use all trials via `trialSummaries` and the `statistics` map.
10. **No formal significance testing.** The implementation reports mean, sample standard deviation (n−1), and 95% CIs (Student-t). It does **not** run hypothesis tests (t-test/ANOVA). Non-overlapping 95% CIs are reported as supporting evidence only, not as a p-value-backed significance claim.

## Engineering / deployment limitations
11. **Single-node H2 file database.** Durable for local demonstration; not suitable for a durable cloud deployment without a persistent disk or managed DB. No authentication on run/history/export endpoints (acceptable for local research use).
12. **Destructive reset is loopback-restricted.** The `POST /api/experiments/reset` endpoint is confined to loopback and requires an exact phrase; it was **not** exercised in this campaign.
13. **Fixed infrastructure baseline.** All runs used the default infrastructure; infrastructure-scaling sensitivity (e.g., more VMs/PEs) was out of scope for this campaign.

## Threats to validity
- **Internal:** determinism and invariant enforcement reduce computational error; validated by independent recomputation of statistics.
- **External:** findings generalise to this CloudSim model and parameter set only; they should not be extrapolated to real cloud deployments or real DDoS scenarios.
