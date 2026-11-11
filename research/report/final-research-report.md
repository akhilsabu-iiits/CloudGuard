# Performance Evaluation of Cloud Services Under Simulated DoS Workloads
### CloudGuard — Final Research Report (M.Tech Cyber Security)

> **Scope note (read first).** This study uses **CloudSim Plus** to simulate computational task workloads and admission control. It is **not** real network-layer DoS/DDoS testing, HTTP load testing, or packet flooding. "Completion time" means **simulated Cloudlet finish − arrival time**. Admission control **sheds computational workload**; it does not increase physical infrastructure capacity. No real-world availability or DDoS-prevention claim is made.

## Abstract
This work evaluates the performance of a simulated cloud service under normal and elevated computational task-arrival workloads and quantifies the effect of Token Bucket admission control. Using CloudSim Plus, five scenarios (S1–S5) were executed at 20 trials each on a fixed infrastructure (2 hosts, 4 VMs, SpaceShared scheduling). Under normal load (100 tasks) the admission policy was transparent: protected and unprotected runs were identical (avg completion 103.6 s, 0% rejection). Under elevated load (500 tasks) admission control rejected 30% of tasks and reduced admitted-task average completion time by ~31% (611.9 s → 424.2 s) and makespan by ~30%, while throughput remained essentially constant (~0.39 tasks/simulated-second). A Token Bucket sensitivity sweep (25/2, 50/5, 100/10) exhibited a monotonic rejection-vs-latency trade-off (71%/165 s, 30%/424 s, 0%/612 s). The results demonstrate that application-layer admission control improves admitted-task responsiveness by shedding load, not by adding capacity. All metrics satisfied the defined invariants and were independently re-validated.

## 1. Introduction
Cloud services under surges of incoming work experience increased queueing and degraded responsiveness. One mitigation is **admission control**: limiting how much work is accepted so that accepted work is served promptly. CloudGuard is a Spring Boot + CloudSim Plus platform that models this behaviour for **computational tasks** (Cloudlets) and measures the resulting performance, comparing protected and unprotected configurations.

## 2. Problem statement
When computational task-arrival pressure exceeds a cloud service's processing capacity, completion times grow as tasks queue. The problem studied is: **how does Token Bucket admission control affect task completion time, makespan, throughput, and rejection rate under normal and elevated simulated workloads, and how sensitive is this to the bucket's capacity and refill rate?**

## 3. Research objectives and questions
**Objectives:** (O1) generate normal and elevated synthetic task-arrival workloads; (O2) compare service performance with and without admission control; (O3) quantify completion time and rejected-workload percentage; (O4) characterise Token Bucket threshold sensitivity.

**Research questions:**
- RQ1: Does admission control affect performance under normal load?
- RQ2: Under elevated load, how does admission control change admitted-task completion time, makespan, throughput, and rejection rate?
- RQ3: How do Token Bucket capacity and refill rate trade off rejection against admitted-task completion time?

## 4. Background
- **CloudSim Plus [1], [2]** — a discrete-event simulation framework for cloud infrastructure, VMs, and Cloudlets (computational tasks); an actively maintained, re-engineered fork of the original CloudSim toolkit [3].
- **Token Bucket [4]** — a classic rate-limiting / traffic-policing algorithm from the leaky-bucket/token-bucket family: a bucket of tokens refills at a fixed rate up to a capacity, and each unit of work consumes a token and is admitted only if a token is available.
- **SpaceShared scheduling** — Cloudlets occupy a processing element exclusively until completion; excess Cloudlets queue.
- **Implementation stack** — Spring Boot [5] (REST backend) and the H2 database engine [6] (file-backed persistence).

## 5. Methodology
Five scenarios were run via the application's REST API, 20 trials each (seeds 42–61), on identical default infrastructure. Admission control is applied as a deterministic pre-pass over the arrival schedule before the CloudSim run; rejected tasks are never submitted. Metrics are computed over completed (SUCCESS) Cloudlets; aggregates report mean, sample standard deviation (n−1), and 95% confidence intervals (Student-t). Full configuration: `../experiments/experimental-configuration.md` and `../../docs/RESEARCH_CONFIGURATION.md`. Independent re-computation of all statistics is in `campaign-validation.md`.

## 6. System architecture
Spring Boot 3.4.5 REST backend (`ExperimentController`) over a CloudSim Plus simulation service (`CloudSimSimulationService`), a pure metrics module (`MetricsCalculator`), a Token Bucket admission module (`TokenBucket`), and an H2 file-backed store (`ExperimentEntity`/`ExperimentRepository`). A vanilla-JS single-page frontend provides scenario execution, analytics, history, and CSV export. Admission control runs before `simulation.start()`; results and per-task records are persisted as JSON.

## 7. Experimental design
| Scenario | Workload | Tasks | Protection | Trials |
|---|---|---:|---|---:|
| S1 | Normal | 100 | Disabled | 20 |
| S2 | Elevated | 500 | Disabled | 20 |
| S3 | Normal | 100 | Enabled | 20 |
| S4 | Elevated | 500 | Enabled | 20 |
| S5 | Elevated | 500 | Enabled (sweep 25/2, 50/5, 100/10) | 20 |

Controlled variable: admission protection (and, in S5, bucket capacity/refill). Fixed: infrastructure, workload size per class, 60 s window, seed base 42.

## 8. Results
(Full tables in `experiment-results.md`; charts in `../experiments/charts/`.) Headline 20-trial means (simulated seconds; ± = 95% CI):

| | S1 | S2 | S3 | S4 | S5 (50/5) |
|---|---:|---:|---:|---:|---:|
| Rejection % | 0.0 | 0.0 | 0.0 | 30.0 | 30.0 |
| Avg completion | 103.608 ± 0.429 | 611.868 ± 0.919 | 103.608 ± 0.429 | 424.215 ± 0.743 | 424.215 ± 0.743 |
| Makespan | 258.985 | 1279.555 | 258.985 | 899.463 | 899.463 |
| Throughput | 0.3861 | 0.3908 | 0.3861 | 0.3891 | 0.3891 |

S5 sweep: 25/2 → 145 admitted / 71% rejected / avg 164.959 s; 50/5 → 350 / 30% / 424.215 s; 100/10 → 500 / 0% / 611.868 s.

## 9. Comparative analysis
See `comparative-analysis.md`. Summary: **A (normal, S1 vs S3):** identical — admission transparent. **B (elevated, S2 vs S4):** admission control rejects 30%, reduces admitted-task avg completion by 30.7% and makespan by 29.7%, throughput unchanged (−0.4%). **C (S5 sweep):** monotonic rejection-vs-latency trade-off.

## 10. Discussion
- **RQ1:** Under normal load admission control has no effect — the bucket admits all 100 tasks (effective admission 350 ≥ 100), so S1 ≡ S3.
- **RQ2:** Under elevated load admission control improves admitted-task responsiveness (−31% avg/P95 completion, −30% makespan) by shedding 30% of the workload; throughput is capacity-bound and unchanged.
- **RQ3:** Capacity and refill set the operating point on the rejection/latency curve (`admitted ≈ capacity + refill×window`). Tighter settings reject more but complete admitted tasks faster.
- **Throughput invariance:** throughput (completed/makespan) is limited by the 4-PE service capacity; admission control changes queue depth and completion time, not the VMs' processing rate — it sheds load, it does not add capacity.
The S2/S4 avg-completion 95% CIs are non-overlapping, consistent with a real difference (reported as evidence, not a formal significance test).

## 11. Limitations
See `research-limitations.md`. Key points: simulated (not real DoS/HTTP) workload; no task loss (impact = latency + rejection); deterministic uniform arrivals (not Poisson/bursty); cross-trial aggregation caveats for Max/P95; `security.enabled` ignored by the engine; single-node H2; no formal significance testing.

## 12. Conclusion
In this CloudSim Plus model, Token Bucket admission control is transparent under normal computational load and, under elevated load, materially improves admitted-task completion time and makespan at the explicit cost of rejecting a portion of the workload. Throughput is unaffected because it is bounded by physical service capacity. The capacity/refill parameters provide a tunable rejection-vs-latency trade-off. These results support admission-based load shedding as an effective responsiveness mechanism for admitted computational tasks — within the stated simulation scope.

## 13. Future work
- Add bursty/Poisson arrival models to study admission control under spikes.
- Model task deadlines/failures to study load-driven task loss.
- Sweep infrastructure scale (VMs/PEs) to study capacity vs admission trade-offs.
- Add formal statistical tests (t-test/ANOVA) and effect sizes.
- Compare alternative admission policies (leaky bucket, priority, weighted fair queueing).
- Externalise the H2 store for durable, multi-node deployments.

## 14. References
References verified against authoritative sources and formatted in IEEE style. No authors, DOIs, page numbers, or publication details were fabricated; the verification log (URLs/DOIs) is in `academic-corrections-report.md`.

[1] M. C. Silva Filho, R. L. Oliveira, C. C. Monteiro, P. R. M. Inácio, and M. M. Freire, "CloudSim Plus: A cloud computing simulation framework pursuing software engineering principles for improved modularity, extensibility and correctness," in *Proc. IFIP/IEEE Symp. Integrated Network and Service Management (IM)*, Lisbon, Portugal, May 2017, pp. 400–406, doi: 10.23919/INM.2017.7987304.

[2] M. C. Silva Filho et al., "CloudSim Plus," official project site and source repository. [Online]. Available: https://cloudsimplus.org/ and https://github.com/cloudsimplus/cloudsimplus

[3] R. N. Calheiros, R. Ranjan, A. Beloglazov, C. A. F. De Rose, and R. Buyya, "CloudSim: a toolkit for modeling and simulation of cloud computing environments and evaluation of resource provisioning algorithms," *Software: Practice and Experience*, vol. 41, no. 1, pp. 23–50, 2011, doi: 10.1002/spe.995.

[4] J. S. Turner, "New directions in communications (or which way to the information age?)," *IEEE Communications Magazine*, vol. 24, no. 10, pp. 8–15, Oct. 1986, ISSN 0163-6804. *(Foundational reference for the leaky-bucket / token-bucket traffic-policing family; the token bucket is the mirror of the leaky-bucket policer described here.)*

[5] Spring Team (Broadcom), "Spring Boot — Reference Documentation," version 3.4.5. [Online]. Available: https://docs.spring.io/spring-boot/ ; API docs: https://www.javadoc.io/doc/org.springframework.boot/spring-boot/3.4.5 ; release: https://github.com/spring-projects/spring-boot/releases/tag/v3.4.5

[6] H2 Database Engine, official documentation. [Online]. Available: https://www.h2database.com/

*No academic approvals or endorsements are claimed. All numerical results derive from the 20-trial campaign recorded in `experiment-results.md` and the raw API responses in `../experiments/results/raw/`. Reference [4] is cited only for the admission-control (token-bucket) algorithm family and is not presented as evidence for any cloud-simulation result.*
