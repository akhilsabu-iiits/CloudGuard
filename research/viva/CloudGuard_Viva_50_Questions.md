# CloudGuard — Viva: 50 Questions & Answers

**Project:** Performance Evaluation of Cloud Services Under Simulated DoS Workloads
**Framing (keep in every answer):** CloudGuard is a **CloudSim Plus simulation** of computational tasks and admission control. Completion time = simulated Cloudlet *finish − arrival* time, **not** real HTTP/network latency. Admission control **sheds workload**; it does **not** add physical capacity. No real DDoS traffic is generated.

**Verified figures used throughout** (20 trials each; ± = 95% CI, Student-t, df=19):
- S1 = S3: 100 admitted, 0 rejected, avg **103.608 ± 0.429 s**, makespan 258.985 s, throughput 0.3861.
- S2: 500 admitted, 0 rejected, avg **611.868 ± 0.919 s**, makespan 1279.555 s, throughput 0.3908.
- S4: 350 admitted, 150 rejected (**30%**), avg **424.215 ± 0.743 s**, makespan 899.463 s, throughput 0.3891.
- S5 sweep: 25/2 → 145 adm (71%), avg 165 s · 50/5 → 350 adm (30%), avg 424 s · 100/10 → 500 adm (0%), avg 612 s.
- Experiment IDs: S1 `aa1e37f3`, S2 `35e590e4`, S3 `fe654860`, S4 `fbfe5029`, S5 `999f0e20`.

---

## Section A — Project Fundamentals (Q1–Q10)

### Q1. What is the research problem your project addresses?
**Short answer:** When computational task-arrival pressure on a cloud service exceeds its processing capacity, tasks queue and completion times rise sharply. The problem is to quantify how Token Bucket admission control changes completion time, makespan, throughput and rejection rate under normal and elevated simulated workloads.
**Detail:** CloudGuard models a fixed cloud infrastructure (2 hosts, 4 VMs, SpaceShared scheduling) and subjects it to 100-task (normal) and 500-task (elevated) workloads over a 60-second arrival window, with and without admission control, measuring the performance impact.
**Key points:** admission control, completion time, makespan, throughput, rejection %, elevated load.
**Follow-up —** *Is this a security or a performance study?* Both: it is a performance evaluation of an admission-control security/resilience mechanism (load shedding) under DoS-like computational pressure.

### Q2. What motivated you to choose this topic?
**Short answer:** Denial-of-service and overload are core cyber-security concerns, and admission control is a practical, low-cost defence. I wanted a reproducible, measurable way to study the trade-off between rejecting work and keeping accepted work responsive — without needing a real datacenter.
**Detail:** Real cloud experiments are expensive and non-repeatable; simulation gives deterministic, controllable experiments. CloudSim Plus lets me isolate one variable (admission control) and measure its effect precisely.
**Key points:** resilience, reproducibility, controllable experiments, cost of real testing.
**Follow-up —** *Why simulation over a real testbed?* Repeatability and control: a real deployment introduces noise (network, OS, co-tenants); simulation isolates the admission-control effect deterministically.

### Q3. What are your research objectives?
**Short answer:** O1 — generate normal and elevated workloads; O2 — compare performance with vs without admission control; O3 — quantify completion time and rejection %; O4 — characterise Token Bucket threshold sensitivity (capacity/refill).
**Detail:** These map to five scenarios (S1–S5) and a 20-trial campaign; each objective is answered by specific comparisons (S1 vs S3, S2 vs S4, and the S5 sweep).
**Key points:** O1–O4, protected vs unprotected, threshold sweep.
**Follow-up —** *Which objective was hardest?* O4 — isolating the capacity/refill effect required three independent admission passes on identical workloads (the S5 sweep).

### Q4. State your research questions.
**Short answer:** RQ1 — Does admission control affect performance under normal load? RQ2 — Under elevated load, how does it change completion time, makespan, throughput and rejection rate? RQ3 — How do capacity and refill rate trade off rejection against admitted-task completion time?
**Detail:** RQ1 is answered by S1≡S3; RQ2 by S2 vs S4; RQ3 by the S5 sweep.
**Key points:** RQ1 normal, RQ2 elevated, RQ3 sensitivity.
**Follow-up —** *Did you answer all three?* Yes — transparent at normal load; ~31% lower admitted-task completion time at elevated load; and a monotonic rejection↔latency trade-off across configurations.

### Q5. What is the research contribution?
**Short answer:** A reproducible, verified simulation platform and a clear quantification of the admission-control trade-off: at elevated load it cuts admitted-task completion time ~31% by shedding 30% of tasks, while throughput stays capacity-bound. It also demonstrates that admission is transparent at normal load.
**Detail:** The contribution is methodological (a deterministic, invariant-checked testbed with independently-reproduced statistics) and empirical (the measured trade-off and sensitivity curve).
**Key points:** reproducibility, verified statistics, trade-off quantification, sensitivity curve.
**Follow-up —** *Is this novel?* The admission-control concept is known; the contribution is a careful, verified, reproducible measurement of its effect in a controlled CloudSim Plus model with honest scope.

### Q6. Why is this topic relevant to Cyber Security?
**Short answer:** Availability is one of the three pillars (CIA) of security. Overload/DoS attacks threaten availability, and admission control is a defensive mechanism that preserves responsiveness for accepted work under pressure.
**Detail:** The study evaluates a resilience control (load shedding) rather than an attack — understanding its limits (it rejects work; it doesn't add capacity) is important for defenders.
**Key points:** CIA triad, availability, load shedding, resilience control.
**Follow-up —** *Where would this apply in practice?* Application-layer admission/rate-limiting gateways (API gateways, request queues) that drop or defer excess work to protect a service.

### Q7. What problem does CloudGuard specifically solve?
**Short answer:** It provides a controlled environment to measure the performance impact of Token Bucket admission control on computational workloads, isolating that single variable and reporting completion time and rejection percentage with statistical confidence.
**Detail:** It is a research/measurement tool, not a production defence: it answers "what does admission control do to performance, and by how much?" reproducibly.
**Key points:** measurement platform, isolated variable, statistical confidence.
**Follow-up —** *Is CloudGuard a product?* No — it is a research simulation and analysis dashboard for evaluating admission policies.

### Q8. What is the difference between DoS and DDoS?
**Short answer:** A DoS (Denial of Service) attack overwhelms a service from a single source; a DDoS (Distributed DoS) does so from many coordinated sources (often a botnet), making it harder to filter and far higher in volume.
**Detail:** My project simulates neither at the network level — it models the *effect* of overload (elevated computational task arrivals) on a service, not the attack traffic itself.
**Key points:** single vs distributed source, volume, botnet, network vs application layer.
**Follow-up —** *Which does CloudGuard model?* Neither as real traffic — it models computational task-arrival overload (a DoS-like load pattern) and an admission defence against it.

### Q9. What is "computational workload pressure" in your project?
**Short answer:** It is a sustained, elevated rate of computational tasks (Cloudlets) arriving at the simulated cloud — 500 tasks over 60 seconds versus 100 in the normal case — competing for a limited number of processing elements.
**Detail:** Arrivals are uniformly spaced over the window; pressure manifests as queueing (rising completion time), not packet floods. The seed only varies task length by ±10%.
**Key points:** sustained elevated arrivals, uniform spacing, queueing, Cloudlets.
**Follow-up —** *Is it bursty?* No — arrivals are deterministic and uniform; modelling bursts/Poisson is future work.

### Q10. What are the project's main limitations?
**Short answer:** It is a simulation (no real network/DDoS); there is no task loss (every admitted task completes, so impact is latency + rejection); arrivals are uniform not bursty; and I report confidence intervals but no formal significance test.
**Detail:** Also, `security.enabled` is scenario-derived; Max/P95 are cross-trial means; and the H2 store is single-node. All are documented in `research-limitations.md`.
**Key points:** simulation scope, no task loss, uniform arrivals, no significance test.
**Follow-up —** *Does this weaken the study?* No — it bounds the claims honestly; the measured trade-off within this scope is valid and reproducible.

---

## Section B — Cloud Computing & CloudSim Plus (Q11–Q20)

### Q11. What is cloud computing, briefly?
**Short answer:** On-demand delivery of shared computing resources (compute, storage, network) over the internet, typically virtualised and elastic, billed by use.
**Detail:** My project models the compute layer — datacenters, hosts, VMs and tasks — to study resource contention under load.
**Key points:** on-demand, virtualisation, elasticity, shared resources.
**Follow-up —** *Which service model does CloudGuard relate to?* Closest to IaaS/PaaS compute scheduling — VMs executing computational tasks.

### Q12. What is CloudSim, and what is CloudSim Plus?
**Short answer:** CloudSim is a Java discrete-event simulation toolkit for modelling cloud datacenters, VMs and workloads (Calheiros et al., 2011). CloudSim Plus (Silva Filho et al., 2017) is a re-engineered, actively maintained fork with cleaner object-oriented design.
**Detail:** I use CloudSim Plus 8.5.7 for its maintainability and correctness-focused API.
**Key points:** discrete-event simulation, toolkit, fork, maintainability.
**Follow-up —** *Why not a real cloud SDK?* I need deterministic, repeatable experiments that isolate admission control — a simulator provides that; a real SDK does not.

### Q13. What is a Cloudlet?
**Short answer:** In CloudSim, a Cloudlet is a unit of computational work — a task with a length in Million Instructions (MI) that executes on a VM's processing element. In my project each Cloudlet abstracts one application task/request.
**Detail:** My Cloudlets have base length 10000 MI with ±10% seeded variation, and resource-utilisation models (CPU 50%, RAM 5%, BW 5%).
**Key points:** computational task, Million Instructions, utilisation model, abstraction of a request.
**Follow-up —** *Does a Cloudlet send network packets?* No — it is a compute task; there is no real network traffic.

### Q14. What are Hosts and Virtual Machines in your model?
**Short answer:** A Host is a physical machine with CPU (processing elements), RAM, bandwidth and storage; a VM runs on a Host and executes Cloudlets. I use 2 hosts (4 PEs @2000 MIPS, 16 GB RAM each) hosting 4 VMs (1 PE @2000 MIPS, 4 GB RAM each).
**Detail:** VMs are the execution units for Cloudlets; the broker maps Cloudlets to VMs.
**Key points:** host, VM, broker, PE, MIPS, RAM.
**Follow-up —** *How many tasks run at once?* With 4 VMs × 1 PE under SpaceShared, at most 4 Cloudlets execute concurrently.

### Q15. What is a Processing Element (PE)?
**Short answer:** A PE is a single CPU core in the simulation, characterised by its speed in MIPS. Hosts and VMs are built from PEs.
**Detail:** Each host has 4 PEs; each VM has 1 PE. The total concurrency for Cloudlet execution is set by the VM PEs (4).
**Key points:** CPU core, MIPS, concurrency bound.
**Follow-up —** *Why does PE count matter?* It fixes the service capacity — the bottleneck that makes throughput roughly constant.

### Q16. What is MIPS?
**Short answer:** Millions of Instructions Per Second — the processing speed of a PE. A Cloudlet of length L (in MI) takes about L/MIPS seconds of compute on a dedicated PE.
**Detail:** With 2000 MIPS and a 10000 MI base length, nominal compute is ~5 s per task; observed completion times are far higher because tasks queue for the 4 PEs.
**Key points:** MI, MIPS, compute time = length/MIPS, queueing adds the rest.
**Follow-up —** *So why is average completion ~104–612 s, not ~5 s?* Because of queueing delay under load — most of the completion time is waiting, not computing.

### Q17. What is Cloudlet scheduling?
**Short answer:** It is the policy by which a VM allocates its PE(s) to the Cloudlets assigned to it — deciding which task runs when.
**Detail:** CloudSim offers TimeShared (time-slicing) and SpaceShared (exclusive until completion). I use SpaceShared.
**Key points:** scheduler, TimeShared vs SpaceShared.
**Follow-up —** *Why does the scheduler matter for results?* It determines queueing behaviour and therefore completion times and runtime performance.

### Q18. Why SpaceShared scheduling?
**Short answer:** SpaceShared runs one Cloudlet per PE to completion and queues the rest, which cleanly models a service with fixed concurrency and avoids the O(n²) event churn of time-slicing under heavy oversubscription.
**Detail:** It gives deterministic queueing behaviour and makes large runs finish quickly in wall-clock time (an earlier TimeShared configuration timed out on 500-task runs).
**Key points:** one task per PE, queueing, wall-clock efficiency, determinism.
**Follow-up —** *Would TimeShared change the findings?* It would change absolute completion times, but the qualitative trade-off (admission control reduces queue depth) would remain; this is future comparison work.

### Q19. What is the difference between simulation time and real time?
**Short answer:** Simulation (simulated) time is the virtual clock inside CloudSim — e.g., a task finishing at simulated t=258 s. Real (wall-clock) time is how long my computer took to run the simulation (under a second). They are unrelated.
**Detail:** All my reported completion times are *simulated* seconds; the admission Token Bucket also refills against the simulated clock, not wall-clock.
**Key points:** virtual clock, wall-clock, simulated seconds.
**Follow-up —** *Could a reader mistake these for latency?* They must not — I label every figure "simulated"; it is finish−arrival in the model, not measured network latency.

### Q20. Why was CloudSim Plus selected?
**Short answer:** It is open-source, actively maintained, and re-engineered for modularity and correctness, with a clean API for Hosts/VMs/Cloudlets/schedulers — ideal for reproducible, controlled experiments.
**Detail:** Version 8.5.7 integrates cleanly with Spring Boot (both JVM), enabling an API-driven experiment workflow.
**Key points:** maintained, modular, correctness, JVM integration.
**Follow-up —** *Any alternative considered?* CloudSim (the base), GreenCloud, iCanCloud — CloudSim Plus was chosen for its modern design and maintenance.

---

## Section C — Token Bucket & Admission Control (Q21–Q30)

### Q21. What is the Token Bucket algorithm?
**Short answer:** A rate-limiting/admission mechanism: a bucket holds tokens up to a capacity and refills at a fixed rate; each unit of work needs a token to proceed. If a token is available it is consumed and the work is admitted; otherwise the work is rejected.
**Detail:** It allows short bursts (up to capacity) while bounding the sustained admission rate (refill rate). It belongs to the leaky-/token-bucket traffic-policing family (Turner, 1986).
**Key points:** capacity, refill rate, token per unit, burst + sustained rate.
**Follow-up —** *Leaky bucket vs token bucket?* They are mirror images; token bucket permits bursts up to capacity, leaky bucket smooths output to a fixed rate.

### Q22. What does bucket capacity represent?
**Short answer:** The maximum number of tokens the bucket can hold — the burst allowance. With capacity C, up to C tasks can be admitted immediately before refill matters.
**Detail:** In my defaults C=50; in S5 it is 25, 50 and 100. Larger capacity means a larger initial burst is admitted.
**Key points:** burst allowance, maximum tokens.
**Follow-up —** *What if capacity is huge?* Then nearly everything is admitted (e.g., 100/10 admits all 500) and the policy behaves like no protection.

### Q23. What is the refill rate?
**Short answer:** The number of tokens added per simulated second (up to capacity) — the sustained admission rate. With refill R, about R tasks/second can be admitted over the long run.
**Detail:** Defaults R=5; S5 uses 2, 5, 10. Combined with capacity it determines total admitted ≈ capacity + refill × window.
**Key points:** tokens/second, sustained rate, up to capacity.
**Follow-up —** *Units?* Tokens per simulated second (the simulation clock), not per real second.

### Q24. How is a token consumed?
**Short answer:** Each admitted task consumes exactly one token (cost = 1). The remaining tokens decrease by one, and the task is submitted to CloudSim.
**Detail:** In code, `tryAcquire(arrivalTime, 1.0)` refills based on elapsed simulated time, then deducts 1 if enough tokens remain.
**Key points:** one token per task, cost=1, deduct-on-admit.
**Follow-up —** *Could a task cost more than one token?* The mechanism supports weighted cost, but this project uses cost = 1 per task.

### Q25. How is an admission decision made?
**Short answer:** At each task's simulated arrival time, the bucket first refills for the elapsed time, then checks if ≥1 token is available. If yes, admit and consume a token; if no, reject. Rejected tasks are never submitted.
**Detail:** Because arrivals are processed in increasing time order, the refill uses non-negative elapsed time and tokens are capped at capacity and never negative.
**Key points:** refill-then-check, arrival-time evaluation, admit/reject.
**Follow-up —** *Is this inside the CloudSim event loop?* No — it is a deterministic pre-pass over the arrival schedule before `simulation.start()`.

### Q26. Why does the bucket start full?
**Short answer:** Starting full models the standard token-bucket burst allowance: at t=0 the service can accept an initial burst up to its capacity before relying on the refill rate.
**Detail:** This is why admitted ≈ capacity + refill × window: the capacity term is the initial full bucket, the refill term is what accrues over the 60-second window.
**Key points:** burst allowance, initial capacity, admission formula.
**Follow-up —** *What if it started empty?* Fewer early admissions; admitted would be closer to refill × window, changing the operating point.

### Q27. Why are rejected Cloudlets not submitted to the broker?
**Short answer:** Because admission control's purpose is to *shed* excess work — a rejected task is denied service, so it never enters the simulation, never consumes a VM, and has no completion time.
**Detail:** This keeps the model faithful: rejected tasks only count toward rejection %, and completion-time statistics are computed over admitted/completed tasks only.
**Key points:** load shedding, not submitted, excluded from completion stats.
**Follow-up —** *Could rejected tasks be retried?* Not in this model; retry/backpressure is future work.

### Q28. Difference between admission control and rate limiting?
**Short answer:** They overlap: rate limiting throttles the *rate* of accepted work; admission control is the broader decision to accept or reject work (which may use a rate limiter like a token bucket). Here the token bucket is used as the admission mechanism.
**Detail:** Admission control emphasises the accept/reject decision to protect a service; rate limiting emphasises bounding throughput. The token bucket serves both roles.
**Key points:** accept/reject vs throttle, token bucket as the mechanism.
**Follow-up —** *So is this rate limiting?* It is admission control implemented with a token-bucket rate limiter.

### Q29. Why were 25/2, 50/5 and 100/10 chosen for S5?
**Short answer:** They give three well-separated operating points on the rejection curve for the 500-task elevated workload: tight (25/2 → 71% rejected), moderate (50/5 → 30%), and permissive (100/10 → 0%), so the sensitivity trend is clear.
**Detail:** They keep the capacity:refill ratio constant (12.5:1) while scaling, cleanly isolating "tighter vs looser" admission. 100/10 intentionally admits everything to reproduce the unprotected S2 baseline.
**Key points:** separated operating points, constant ratio, reproduces baseline.
**Follow-up —** *Why constant ratio?* To vary overall tightness without also changing the burst-to-rate balance — a cleaner single-factor sweep.

### Q30. What happens when the workload exceeds admission capacity?
**Short answer:** Tasks beyond the admissible amount (capacity + refill × window) are rejected. For 500 tasks with 50/5, only 350 are admitted and 150 (30%) rejected; admitted tasks then complete faster because there is less queueing.
**Detail:** This is the essence of the trade-off: exceeding capacity converts into rejections, not failures or slowdowns for admitted work.
**Key points:** excess → rejection, lower queue depth, trade-off.
**Follow-up —** *What about the rejected users in reality?* They would receive a "try later"/throttled response — availability for admitted work is preserved at their expense.

---

## Section D — Methodology & Statistics (Q31–Q40)

### Q31. Describe your experimental design.
**Short answer:** A controlled 2×2-plus-sweep design: S1/S2 unprotected and S3/S4 protected at normal/elevated load, plus S5 (threshold sweep). Each scenario runs 20 independent trials (seeds 42–61) on identical fixed infrastructure.
**Detail:** Independent variable: admission protection (and capacity/refill in S5). Controlled: infrastructure, workload size per class, 60-second window, seed base.
**Key points:** controlled experiment, 2×2 + sweep, 20 trials, isolated variable.
**Follow-up —** *How do you ensure fairness?* S1≡S3 and S2≡S4 use identical settings and seeds — only protection differs.

### Q32. Describe the five scenarios.
**Short answer:** S1 normal/unprotected (100), S2 elevated/unprotected (500), S3 normal/protected (100), S4 elevated/protected (500), S5 elevated/protected threshold sweep (25/2, 50/5, 100/10).
**Detail:** S1–S4 form the protected/unprotected comparison; S5 isolates the Token Bucket parameters.
**Key points:** S1–S5 purpose, protected vs unprotected, sweep.
**Follow-up —** *Why both S4 and S5?* S4 is a standalone protected scenario; S5 is a sensitivity study whose 50/5 pass coincides with S4 (consistency check).

### Q33. Difference between normal and elevated workloads?
**Short answer:** Normal = 100 tasks, elevated = 500 tasks, both over the same 60-second window — so elevated has 5× the arrival density and far higher queueing pressure.
**Detail:** Normal load (100) is below the effective admission capacity (350), so protection is transparent; elevated (500) exceeds it, so protection rejects the surplus.
**Key points:** 100 vs 500, same window, 5× density.
**Follow-up —** *Why these numbers?* They sit below and above the default bucket's effective admission (≈350), producing the transparent-vs-throttling contrast.

### Q34. What do you mean by uniform task arrivals?
**Short answer:** Arrivals are evenly spaced over the window: task n arrives at n × duration / (taskCount − 1), so the first is at t=0 and the last at t=60 s. They are deterministic, not random or bursty.
**Detail:** For 500 tasks that is ~0.12 s apart; for 100 tasks ~0.61 s apart.
**Key points:** even spacing, deterministic, t=0…60.
**Follow-up —** *Is uniform realistic?* It is a controlled baseline; real traffic is often bursty/Poisson — modelling that is future work.

### Q35. What does the random seed control?
**Short answer:** It seeds the pseudo-random generator that applies the ±10% variation to each task's length. The same seed reproduces the same task-length sequence, making runs deterministic.
**Detail:** Trial t uses seed 42 + t (so 42…61 across 20 trials). The seed does **not** affect arrival timing, which is purely formulaic.
**Key points:** seeds length variation only, reproducibility, seed = 42 + trial.
**Follow-up —** *Why vary length at all?* To make the workload slightly less artificial while keeping it reproducible by seed.

### Q36. Why 20 trials per scenario?
**Short answer:** Multiple trials (with different seeds) capture the variability from the ±10% task-length randomisation, letting me report a stable mean with a 95% confidence interval rather than a single possibly-atypical run. Twenty gives tight intervals while remaining fast.
**Detail:** With n=20, the Student-t critical value (df=19) is 2.093; my CIs are sub-second (e.g., S4 ±0.743 s), indicating low variability.
**Key points:** variability, stable mean, tight CI, df=19.
**Follow-up —** *Would more trials help?* Marginally — the CIs are already tight; 20 balances rigour and runtime.

### Q37. What are mean and standard deviation here?
**Short answer:** The mean is the average of a metric across the 20 trials (e.g., average completion time); the standard deviation measures how much individual trials vary around that mean.
**Detail:** I report the *sample* standard deviation and use it to build the confidence interval.
**Key points:** average, spread, per-metric aggregation.
**Follow-up —** *Which metrics get statistics?* completed tasks, completion rate, average completion time, makespan and throughput.

### Q38. Why use the *sample* standard deviation (n−1)?
**Short answer:** Because the 20 trials are a sample, not the entire population of possible runs. Dividing by n−1 (Bessel's correction) gives an unbiased estimate of the population variance.
**Detail:** Using n would underestimate variability; n−1 corrects for the one degree of freedom used by the sample mean.
**Key points:** sample vs population, Bessel's correction, unbiased estimate.
**Follow-up —** *What is the denominator here?* n−1 = 19.

### Q39. What is a 95% confidence interval and how did you compute it?
**Short answer:** It is a range that, under the method's assumptions, would contain the true mean 95% of the time across repeated sampling. I compute it as mean ± t × (sample SD / √n), with t = 2.093 (Student-t, df=19).
**Detail:** Example: S4 average completion 424.215 ± 0.743 s. I independently recomputed every CI from trial-level data and they matched the application's values exactly.
**Key points:** mean ± t·SD/√n, t=2.093, independent verification.
**Follow-up —** *What does a narrow CI imply?* Low trial-to-trial variability and a precise estimate of the mean — not, by itself, statistical significance.

### Q40. What is the Student t-distribution and what is "degrees of freedom"?
**Short answer:** The Student-t distribution is used instead of the normal when estimating a mean from a small sample with unknown variance; it has heavier tails that shrink as the sample grows. Degrees of freedom (df = n−1 = 19) parameterise it.
**Detail:** For df=19, the two-tailed 95% critical value is 2.093 (approaching 1.96 as n→∞). My implementation uses a t-table for df 1–19 and 1.96 beyond.
**Key points:** small-sample inference, heavier tails, df = n−1, t(19)=2.093.
**Follow-up —** *Why not just use 1.96?* With n=20 the t-value (2.093) is slightly larger, giving a correctly wider, more honest interval.

---

## Section E — Results, Interpretation & Challenging Questions (Q41–Q50)

### Q41. Why did S1 and S3 produce identical results?
**Short answer:** At normal load (100 tasks), the default Token Bucket admits everything — its effective admission is capacity + refill × 60 = 50 + 300 = 350, far above 100 — so zero tasks are rejected and the protected run (S3) is indistinguishable from the unprotected run (S1): both average 103.608 s.
**Detail:** With the same seeds and no rejections, the generated and admitted workloads are identical, so every metric matches to several decimals.
**Key points:** effective admission 350 ≥ 100, 0 rejection, transparent protection.
**Follow-up —** *Is that a bug?* No — it is the correct, expected result and directly answers RQ1 (admission is transparent at normal load).

### Q42. Why does S4 reject 30% of tasks?
**Short answer:** With 500 elevated tasks and the 50/5 bucket, admitted ≈ min(500, 50 + 5×60) = 350, so 150 tasks (30%) exceed the admissible amount and are rejected.
**Detail:** The 50 is the full-bucket burst allowance; the 300 is the refill accrued over the 60-second window.
**Key points:** admitted = min(gen, cap+refill·60) = 350, 150 rejected, 30%.
**Follow-up —** *Verified?* Yes — the measured admitted count is exactly 350 across all trials.

### Q43. Why did average completion time decrease (S2→S4)?
**Short answer:** Because S4 admits only 350 tasks instead of 500, there is less competition for the 4 processing elements, so queue depth is lower and admitted tasks wait less — average completion falls from 611.868 s to 424.215 s (−30.7%).
**Detail:** Most of the completion time is queueing delay; reducing the admitted count reduces that delay for the tasks that are accepted.
**Key points:** lower queue depth, less waiting, −30.7%.
**Follow-up —** *Is this a real speed-up of the hardware?* No — the hardware is unchanged; only the admitted workload (and thus waiting) is smaller.

### Q44. Why did throughput remain nearly unchanged?
**Short answer:** Throughput = completed / makespan is bounded by the fixed service capacity (4 VM PEs). Every saturated configuration drives the VMs at ~0.39 tasks/simulated-second regardless of admission, so throughput is ~constant (S2 0.3908 vs S4 0.3891).
**Detail:** Admission control changes *how many tasks wait and for how long* (completion time, queue depth), not the rate at which the VMs retire work. It sheds load; it does not add capacity.
**Key points:** capacity-bound, ~0.39, queue depth vs processing rate.
**Follow-up —** *So admission control doesn't improve throughput?* Correct — it improves admitted-task completion time, not throughput.

### Q45. Explain the S5 threshold sweep.
**Short answer:** S5 runs three independent admission configurations on the same 500-task workload: 25/2 (145 admitted, 71% rejected, avg 165 s), 50/5 (350, 30%, 424 s), and 100/10 (500, 0%, 612 s) — showing a monotonic rejection↔latency trade-off.
**Detail:** Tighter settings reject more but complete admitted tasks faster; 100/10 admits everything and matches unprotected S2.
**Key points:** three configs, monotonic trade-off, 100/10 ≡ S2.
**Follow-up —** *What does the sweep teach a practitioner?* Capacity/refill are the knobs to position a service on the rejection-vs-responsiveness curve.

### Q46. Why do S4 and S5-main produce matching results?
**Short answer:** Both use the Token Bucket 50/5 configuration on the same elevated workload with the same seeds, and the simulation is deterministic — so their aggregate results are expected to match by construction (both: 350 admitted, avg 424.215 s). It is a consistency check, not duplicated data.
**Detail:** S4 is the standalone protected scenario; S5's 50/5 is simply its middle sweep point — the shared operating point between the two.
**Key points:** same 50/5 config, determinism, consistency check.
**Follow-up —** *Doesn't that make one redundant?* No — S4 anchors the protected/unprotected comparison; S5 studies sensitivity. Their overlap validates determinism.

### Q47. Does lower completion time mean better infrastructure performance?
**Short answer:** No. The infrastructure is unchanged; lower completion time for admitted tasks comes from admitting fewer tasks (less queueing), at the cost of rejecting 30%. It is a workload-shedding effect, not a hardware improvement.
**Detail:** Throughput — the true measure of processing capacity — stays constant, confirming the hardware performs the same.
**Key points:** no hardware change, shedding not speed-up, constant throughput.
**Follow-up —** *So is it "better"?* Better *responsiveness for admitted tasks*, worse *acceptance rate* — a trade-off, not a free improvement.

### Q48. Does CloudGuard prevent real-world DDoS attacks?
**Short answer:** No. CloudGuard is a CloudSim Plus simulation of computational task admission control; it generates no real network traffic and makes no real-world DDoS-prevention claim. It studies how admission-based load shedding affects simulated performance.
**Detail:** Real DDoS defence involves network-layer filtering, scrubbing, rate limiting at the edge, etc. — outside this project's scope.
**Key points:** simulation only, no real traffic, no DDoS-prevention claim.
**Follow-up —** *Could the idea transfer?* The admission-control principle (shed excess work to protect accepted work) is used in real API gateways, but validating that is beyond this simulation.

### Q49. What would you improve in future work?
**Short answer:** Add bursty/Poisson arrival models, model task deadlines and failures (so overload can cause loss), sweep infrastructure scale, run formal significance tests (t-test/ANOVA), and compare alternative policies (leaky bucket, priority, weighted fair queueing).
**Detail:** Externalising the H2 store for durable multi-node deployment and adding integration tests are engineering follow-ups.
**Key points:** bursty arrivals, deadlines/failures, infra sweep, significance tests, alternative policies.
**Follow-up —** *Which is most impactful?* Bursty arrivals + deadlines — they would let the model show task loss, broadening the DoS realism.

### Q50. What is the most important contribution of this research?
**Short answer:** A rigorously verified, reproducible quantification of the admission-control trade-off: under elevated computational load, Token Bucket admission control reduces admitted-task completion time by ~31% by shedding ~30% of tasks, while throughput stays capacity-bound — and it is transparent at normal load.
**Detail:** Equally important is the honest scope and methodology: deterministic experiments, invariant checks, and independently-reproduced statistics.
**Key points:** verified trade-off, reproducibility, honest scope.
**Follow-up —** *One sentence to the panel?* "Admission control buys responsiveness for accepted work by rejecting excess work — it sheds load, it does not add capacity — and I measured exactly how much, reproducibly, in simulation."

---

## Gaps / not available in project materials (stated honestly)
- **No formal significance test** (t-test/ANOVA) was performed — only confidence intervals; do not claim significance.
- **No real network/latency/availability data** exists — all figures are simulated.
- **Student/institution identity** is not in the project files (left as a placeholder on the title slide).
- **No bursty/Poisson arrivals or task-failure modelling** — acknowledged as future work.
