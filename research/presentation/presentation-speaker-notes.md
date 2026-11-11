# CloudGuard — Presentation Speaker Notes

Slide-by-slide speaking guidance for `CloudGuard_Final_MTech_Presentation.pptx` (15 slides, ~12–15 min). Reminder: this is a CloudSim Plus simulation — completion time is simulated Cloudlet finish−arrival, not real network latency; admission control sheds load, it does not add capacity.

## Slide 1 — Title

Good morning/afternoon. I'm presenting my M.Tech Cyber Security project, 'Performance Evaluation of Cloud Services Under Simulated DoS Workloads'. Set expectations immediately: this is a SIMULATION study built on CloudSim Plus. I model computational tasks (Cloudlets) and an admission-control policy — I do NOT send real network traffic or perform real DDoS testing. Over the next ~12 minutes I'll cover the problem, the CloudGuard system I built, the experimental design, and the measured results from a 20-trial-per-scenario campaign, then limitations and conclusions. [Note: student name and institution are left blank because they were not present in the project files; fill these in before the defence.] Transition: let me start with the context.

## Slide 2 — Introduction

Cloud platforms run many computational tasks on shared, finite resources. When arrival pressure spikes, tasks compete for a limited number of processing elements and completion times grow. One well-known mitigation is admission control — accept less work so that what you accept finishes quickly. I study this with CloudSim Plus, which simulates datacenters, VMs and 'Cloudlets' — units of computational work. Be explicit with examiners: everything here is simulated. A Cloudlet abstracts an application task; completion time is the simulated finish-minus-arrival time, not measured HTTP or network latency. 'DoS-like' means a sustained high task-arrival rate, not a packet flood. Transition: so what exactly is the problem?

## Slide 3 — Problem Statement & Motivation

The core problem: once arrival pressure exceeds the service capacity, queueing dominates and completion times climb. My research question is to quantify how Token Bucket admission control changes four things — completion time, makespan, throughput and rejection rate — under both normal and elevated load, and how sensitive that is to the bucket's capacity and refill rate. Motivation is three-fold: understand the rejection-versus-latency trade-off; build a reproducible deterministic testbed; and be precise that admission control sheds workload — it never increases the number of VMs or CPUs. Transition: let me state the objectives and research questions formally.

## Slide 4 — Research Objectives & Questions

Four objectives: O1 generate normal and elevated workloads; O2 compare protected vs unprotected; O3 measure completion time and rejection percentage; O4 characterise how the bucket's capacity and refill rate behave. These map to three research questions — RQ1 about normal load, RQ2 about elevated load across four metrics, and RQ3 about the capacity/refill trade-off. Each result slide later answers one of these directly. Transition: here's the system I built to answer them.

## Slide 5 — CloudGuard Architecture

This is the actual CloudGuard architecture. A vanilla-JavaScript single-page app drives everything through a Spring Boot REST API. The controller calls the simulation service, which builds the CloudSim Plus model — datacenter, hosts, VMs with SpaceShared scheduling — and the Cloudlets. Crucially, the Token Bucket runs as a deterministic pre-pass BEFORE the simulation starts: rejected tasks are never submitted to the broker. The MetricsCalculator computes completion time, makespan, throughput and the multi-trial statistics, and results are persisted as JSON in an H2 file database. Examiner tip: if asked 'where does admission happen?' — answer: before simulation.start(), over the arrival schedule, using the same arrival timestamps. Transition: the technology stack behind this.

## Slide 6 — Technology Stack

The stack is deliberately standard and reproducible: Java 17, Spring Boot 3.4.5 for the REST backend, and CloudSim Plus 8.5.7 as the simulation engine. Persistence is an H2 file database via JPA, storing both the request and the full result as JSON so every experiment is fully reproducible and exportable. The frontend is a dependency-light vanilla-JS single-page app with SVG charts and CSV export. Maven builds it; it runs locally on port 8080. Unit tests cover the pure metric and Token Bucket math. If asked why CloudSim Plus over CloudSim: it's a maintained, re-engineered fork with cleaner object-oriented design. Transition: now the methodology.

## Slide 7 — Research Methodology

Methodology: controlled simulation experiments through the application's own REST API — five scenarios, twenty trials each, with trial seeds 42 through 61. The independent variable is admission protection; in S5 it's the bucket's capacity and refill. Everything else is held fixed, so S1-versus-S3 and S2-versus-S4 are clean comparisons. The model is deterministic: a given scenario and seed always reproduce the same result. I measure average, P95 and maximum completion time, makespan, throughput, completion rate and rejection percentage, all over completed tasks. Across the twenty trials I report mean, sample standard deviation with the n-minus-one denominator, and a 95% confidence interval using Student's t with 19 degrees of freedom. Every invariant was checked and every statistic was independently recomputed and matched exactly. Transition: the fixed infrastructure and workload.

## Slide 8 — Infrastructure & Workload Configuration

The infrastructure is fixed and was verified directly from the source code. Two hosts, each with four processing elements at 2000 MIPS and 16 GB RAM; four VMs, each with one PE, also at 2000 MIPS and 4 GB RAM. The scheduler is SpaceShared, so at most four Cloudlets execute at once — that four-PE concurrency is the capacity bound that drives all the queueing behaviour you'll see. Cloudlets use 50% CPU, 5% RAM and 5% bandwidth. The workload: 100 tasks for normal, 500 for elevated, both arriving uniformly over a 60-second window from t=0 to t=60, with a seeded plus-or-minus-ten-percent variation in task length. Examiner tip: the seed varies task LENGTH only; arrival timing is deterministic. Transition: the five scenarios.

## Slide 9 — Experimental Scenarios

Five scenarios form a clean 2x2 plus a sweep. S1 and S2 are unprotected baselines at normal and elevated load. S3 and S4 add admission control at the same loads. Because S1 and S3 (and S2 and S4) use identical infrastructure, workload, trials and seed, the only difference is protection — that's what makes them fair comparisons. S5 is special: it runs three independent Token Bucket settings — 25/2, 50/5 and 100/10 — on the elevated workload to study sensitivity. It is NOT a sixth configuration; its 50/5 pass is the same operating point as S4. Transition: let me explain the Token Bucket itself.

## Slide 10 — Token Bucket Admission Control

Here's the admission mechanism. The bucket starts full at its capacity and refills at a fixed rate per simulated second, up to capacity. Each task arrival, evaluated at its simulated arrival time, checks for a token. If a token is available, the task is admitted — one token is consumed and the Cloudlet is submitted to CloudSim to execute on a VM. If not, the task is rejected and never submitted; it counts as rejected workload. The key closed form: under saturated demand, admitted equals the minimum of generated tasks and capacity plus refill times the 60-second window. This predicts 145, 350 and 500 for the three S5 settings, and the simulation matches exactly. Examiner tip: emphasise 'rejected tasks are never submitted' — so they have no completion time and don't appear in the averages. Transition: the headline results.

## Slide 11 — Normal & Elevated Workload Results

These are the central results. On the left, mean completion time; on the right, throughput — both over 20 trials with 95% confidence intervals. Under normal load, S1 and S3 are identical: 100 tasks admitted, zero rejected, average 103.6 seconds. That answers RQ1 — admission control is transparent at normal load, because the default bucket can admit all 100 tasks. Under elevated load, the story changes. S4 rejects 30% of the 500 tasks, and for the admitted tasks the average completion time drops almost 31% — from 612 to 424 seconds — and makespan drops similarly. But look at the right chart: throughput barely moves, about 0.39 tasks per simulated second in every case. That's the key insight — admission control cuts queueing delay for admitted tasks, but it does not raise the processing rate, which is bounded by the four-PE capacity. It sheds load; it does not add capacity. Transition: how sensitive is this to the bucket settings?

## Slide 12 — Token Bucket Threshold Analysis

S5 sweeps three bucket settings on the elevated workload. The left chart is rejection percentage, the right is mean completion time. It's a clean monotonic trade-off, which answers RQ3. The tightest setting, 25 over 2, admits only 145 tasks and rejects 71%, but those admitted tasks finish fastest at about 165 seconds. The middle, 50 over 5, rejects 30% at 424 seconds. The loosest, 100 over 10, admits everything, rejects nothing, and completes slowest at 612 seconds — which is exactly the unprotected S2 behaviour. So capacity and refill are the tuning knobs that place you on the rejection-versus-latency curve. There is no free lunch: faster admitted-task completion is bought by rejecting more incoming work. Transition: let me summarise the statistics and what they do and don't support.

## Slide 13 — Statistical Findings & Discussion

On the statistics: with 20 trials per scenario, the 95% confidence intervals are tight — for example S2 is 611.9 plus-or-minus 0.9 seconds and S4 is 424.2 plus-or-minus 0.7. S1 and S3 are literally identical at 103.6. The small intervals indicate low trial-to-trial variability. Importantly, the S2 and S4 intervals do not overlap, which is evidence of a genuine difference — but I am careful to call this a confidence-interval observation, not a formal significance test, because I did not run a t-test or ANOVA. Throughput stays near 0.39 everywhere, confirming it is capacity-bound; completion time, by contrast, reflects queue depth, which admission control controls. The overarching finding is the trade-off: you buy lower admitted-task completion time with higher rejection. Transition: the honest limitations.

## Slide 14 — Limitations & Future Work

Being explicit about limitations earns credibility. First and foremost, this is a simulation — no real network traffic, no real DDoS; completion time is a simulated quantity. Second, the model has no task loss: every admitted task completes, so DoS impact shows up as latency and rejection, not failures. Third, arrivals are deterministic and uniform, not bursty, and the seed only varies task length. Fourth, the reported Max and P95 are cross-trial means, and I did not run a formal significance test. Future work follows naturally: add bursty or Poisson arrivals, model deadlines and failures, sweep the infrastructure scale, add formal statistical tests, and compare alternative admission policies like leaky bucket or priority queueing. Transition: to conclude.

## Slide 15 — Conclusion & Q&A

To conclude: admission control is transparent under normal load, where S1 and S3 are identical. Under elevated load it improves admitted-task completion time by about 31% and makespan by about 30% — but it achieves this by shedding 30% of the workload, not by adding any capacity. Throughput stays capacity-bound around 0.39, and the bucket's capacity and refill rate let you tune the rejection-versus-latency trade-off, as the S5 sweep shows. Every result was verified and the statistics independently reproduced, and I kept the scope honest — this is a CloudSim simulation, not a real DDoS-prevention claim. Likely examiner questions: Why does throughput not improve? — because it's bounded by the four-PE service capacity. Is this real DDoS testing? — no, it's a computational-task admission-control simulation. Why 20 trials? — to obtain stable means and confidence intervals. Thank you; I'm happy to take questions.
