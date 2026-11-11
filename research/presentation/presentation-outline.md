# CloudGuard — Presentation Outline (13 slides)

**Title:** Performance Evaluation of Cloud Services Under Simulated DoS Workloads
**Framing reminder for every slide:** simulated Cloudlet completion time (not real HTTP/network latency); admission control sheds computational load, it does not add capacity; not real DDoS.

---

### Slide 1 — Title
- **Key points:** project title; M.Tech Cyber Security; CloudGuard (Spring Boot + CloudSim Plus); author.
- **Chart:** none (title).
- **Speaker notes:** State up front that this is a CloudSim Plus simulation of computational task workloads and admission control, not real network DoS testing.

### Slide 2 — Motivation & Problem
- **Key points:** surges of computational tasks cause queueing and slow responses; admission control as a mitigation; question: does limiting admitted work help, and at what cost?
- **Chart:** simple concept diagram (arrivals → admission gate → VMs → queue).
- **Notes:** Emphasise the trade-off framing (rejection vs latency).

### Slide 3 — Objectives & Research Questions
- **Key points:** O1 generate normal/elevated workloads; O2 compare protected/unprotected; O3 completion time & rejection %; O4 Token Bucket sensitivity. RQ1–RQ3.
- **Chart:** none (bullet list).
- **Notes:** Map each RQ to a later results slide.

### Slide 4 — Background
- **Key points:** CloudSim Plus (Cloudlets = computational tasks); Token Bucket (capacity + refill, 1 token/task); SpaceShared scheduling (≤4 concurrent).
- **Chart:** Token Bucket schematic (tokens refill, task consumes a token or is rejected).
- **Notes:** Note admission is a deterministic pre-pass before the simulation starts.

### Slide 5 — System Architecture
- **Key points:** REST backend (ExperimentController) → simulation service → Token Bucket + MetricsCalculator → H2; vanilla-JS analytics frontend.
- **Chart:** architecture / workflow diagram (reuse the Mermaid flow from the audit).
- **Notes:** Mention persistence of request+result JSON and CSV export.

### Slide 6 — Experimental Design
- **Key points:** S1–S5 matrix; 20 trials each (seeds 42–61); fixed infrastructure; controlled variable = admission protection (and S5 capacity/refill).
- **Chart:** the S1–S5 table.
- **Notes:** Stress S1≡S3 and S2≡S4 equivalence (only protection differs).

### Slide 7 — Frozen Configuration
- **Key points:** 2 hosts/4 VMs, SpaceShared; 100/500 tasks, 60 s window, seeded ±10% length; bucket starts full, refills vs simulated time; S5 = 25/2, 50/5, 100/10.
- **Chart:** compact config table.
- **Notes:** All values verified from source (docs/RESEARCH_CONFIGURATION.md); nothing tuned for results.

### Slide 8 — Result A: Normal workload (S1 vs S3)
- **Key points:** identical — 100 admitted, 0 rejected, avg 103.6 s both; admission transparent at normal load (RQ1).
- **Chart:** `mean-completion-time.svg` (highlight S1=S3 bars).
- **Notes:** Default bucket admits all 100 (effective 350 ≥ 100).

### Slide 9 — Result B: Elevated workload (S2 vs S4)
- **Key points:** 30% rejected; admitted-task avg completion 611.9 → 424.2 s (−31%); makespan −30%; throughput unchanged (RQ2).
- **Chart:** `mean-completion-time.svg` + `throughput.svg` (side by side).
- **Notes:** Error bars = 95% CI; S2/S4 CIs non-overlapping (evidence, not a significance test).

### Slide 10 — Result C: Token Bucket sensitivity (S5)
- **Key points:** monotonic trade-off: 25/2 → 71% rejected / 165 s; 50/5 → 30% / 424 s; 100/10 → 0% / 612 s (RQ3).
- **Chart:** `s5-threshold-rejection.svg` + `s5-threshold-completion.svg`.
- **Notes:** 100/10 reproduces unprotected S2 behaviour.

### Slide 11 — Why throughput stays flat
- **Key points:** throughput = completed/makespan is bounded by 4-PE service capacity; admission changes queue depth & completion time, not processing rate; sheds load ≠ adds capacity.
- **Chart:** `throughput.svg` (all scenarios ≈0.39) + `rejection-rate.svg`.
- **Notes:** This is the central academic point — avoid overclaiming capacity gains.

### Slide 12 — Validation & Limitations
- **Key points:** invariants hold every trial; stats independently re-computed (match exactly); limitations: simulated not real DoS, no task loss, uniform arrivals, aggregation caveats, no formal significance test.
- **Chart:** small "invariants ✓" summary / limitations list.
- **Notes:** Be explicit about academic scope and what is NOT claimed.

### Slide 13 — Conclusion & Future Work
- **Key points:** admission control transparent at normal load, improves admitted-task responsiveness under elevated load at the cost of rejection; throughput capacity-bound. Future: bursty arrivals, deadlines/failures, infra scaling, formal significance tests, alternative policies.
- **Chart:** one-line summary table (A/B/C findings).
- **Notes:** Close by restating the trade-off and the simulation scope.

---
**Recommended chart files (in `../experiments/charts/`):** `mean-completion-time.svg`, `p95-completion-time.svg`, `rejection-rate.svg`, `throughput.svg`, `s5-threshold-rejection.svg`, `s5-threshold-completion.svg`. All completion-time axes are labelled "seconds (simulated)".
