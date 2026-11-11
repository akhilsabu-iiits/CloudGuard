# CloudGuard — Academic-Readiness Audit

**Project:** Performance Evaluation of Cloud Services Under Simulated DoS Workloads (M.Tech Cyber Security)
**Audit type:** read-only verification of the final-campaign research deliverables against the raw experimental data and the implemented CloudSim Plus model.
**Method:** every reported figure was independently recomputed from `raw/S1–S5.json`; statistics were re-derived; methodology claims were checked against the source baseline (`docs/RESEARCH_CONFIGURATION.md`).

## Severity summary
| # | Audit area | Verdict |
|---|---|---|
| 1 | Reported results vs raw data | **PASSED** |
| 2 | Statistics & confidence intervals | **PASSED** |
| 3 | S1/S3 and S2/S4 methodological equivalence | **PASSED** |
| 4 | Token Bucket analysis | **PASSED** |
| 5 | Methodology vs implemented simulation | **PASSED** |
| 6 | Conclusions supported by results | **PASSED** |
| 7 | Limitations sufficiency | **PASSED** |
| 8 | References genuine & complete | **MAJOR** |
| 9 | Logical structure & academic writing | **PASSED** (one Minor) |
| 10 | Presentation outline clarity | **PASSED** |
| — | Simulated-vs-real distinction / no DDoS-prevention claim | **PASSED** |

**No Critical findings. One Major (references). A small number of Minor items.**

---

## 1. Reported results vs raw data — PASSED
Every value in `experiment-results.md`, `comparative-analysis.md`, `summary.json`, and the charts was recomputed from `raw/`:
- S1/S3: avg 103.60763, P95 189.25913, Max 199.95375, makespan 258.98450, throughput 0.38615 — match.
- S2: avg 611.86824, P95 1155.89123, Max 1219.72349, makespan 1279.55515, throughput 0.39077 — match.
- S4 and S5(50/5): avg 424.21467, P95 795.43364, makespan 899.46345, throughput 0.38913 — match.
- S5 variants: 145/355 (71%), 350/150 (30%), 500/0 (0%), avg 164.959/424.215/611.868 — match.
All rejection %, completion rate, and invariants (`gen=adm+rej`, `comp+unf+fail=adm`) hold. **No discrepancy found.** (Note: S4 and S5-main are numerically identical because both are the 50/5 protected-elevated configuration with the same seed base — this is the expected deterministic outcome of two separate runs, not a copy error.)

## 2. Statistics & confidence intervals — PASSED
Independent recomputation of mean, sample standard deviation (n−1), and 95% CI (Student-t, df = 19, t = 2.093) matched the reported `statistics` **exactly** for all five scenarios (e.g., S4 avg 424.214675 ± 0.743271; S2 611.868241 ± 0.918781). `ciAvailable = true`, `samples = 20` throughout. The report correctly treats the non-overlapping S2/S4 CIs as *supporting evidence*, **not** a formal significance test — methodologically honest. ✅

## 3. S1/S3 and S2/S4 equivalence — PASSED
Both pairs used identical defaults (infrastructure, 60 s window, 10000 MI base length, seed base 42, 20 trials); only admission protection differs. S1 and S3 produce **identical** outputs (avg 103.60763 both; same seeds 42–61), confirming apples-to-apples equivalence. S2 and S4 both generate 500 tasks at 20 trials. Correctly described in `experimental-configuration.md`. ✅

## 4. Token Bucket analysis — PASSED
The admission closed form `admitted = min(generated, capacity + refill × 60)` is verified exactly against raw: 25/2→145, 50/5→350, 100/10→700→capped 500; S4 50/5→350. The monotonic rejection↔latency trade-off (71%/165 s, 30%/424 s, 0%/612 s) is accurate, and the `100/10` = unprotected-S2 equivalence is correct. ✅

## 5. Methodology vs implemented simulation — PASSED
`experimental-configuration.md` and the report's Methodology accurately describe the implementation: SpaceShared (≤4 concurrent), utilisation CPU 50/RAM 5/BW 5, uniform arrivals t=0…60, seeded ±10% length (seed affects length only), Token Bucket starts full and refills vs simulated time as a **pre-pass before `simulation.start()`**, rejected tasks never submitted, metrics over completed tasks. All match `docs/RESEARCH_CONFIGURATION.md` and source. ✅

## 6. Conclusions supported by results — PASSED
- "Admission transparent at normal load" ← S1≡S3 (0 rejection). ✅
- "~31% lower admitted-task completion time under elevated load" ← 611.868→424.215 (−30.7%). ✅
- "Throughput unchanged / capacity-bound" ← 0.3908 vs 0.3891 (−0.4%), explained via the 4-PE service limit. ✅
- "Sheds load, does not add capacity" — correct and repeatedly stated. ✅
No conclusion overreaches the data; no statistical-significance claim is made without the stated CI evidence.

## 7. Limitations sufficiency — PASSED
`research-limitations.md` comprehensively covers: simulation ≠ real DoS; simulated completion time; no task loss (latency+rejection only); deterministic uniform (non-Poisson/non-bursty) arrivals; admission pre-pass; `security.enabled` ignored; cross-trial aggregation caveats (Max = mean-of-maxima, P95 = cross-trial mean, CSV = last trial); no formal significance testing; single-node H2; fixed infrastructure. Threats to validity included. ✅

## 8. References genuine & complete — **MAJOR**
**File:** `final-research-report.md` §14. **Issue:** all five references are **placeholders marked `[verify]`** with incomplete bibliographic details (no confirmed authors/year/venue/pages/DOI). This is academically honest (nothing is fabricated — good) but **insufficient for submission**: an M.Tech report requires genuine, complete citations.
**Why it matters:** incomplete references fail academic formatting/originality requirements and cannot be checked by examiners.
**Recommendation (author to complete, do not fabricate):**
- CloudSim Plus — confirm: M. C. Silva Filho, R. L. Oliveira, C. C. Monteiro, P. R. M. Inácio, M. M. Freire, "CloudSim Plus: A cloud computing simulation framework…," *IFIP/IEEE IM 2017*, pp. 400–406.
- CloudSim (base) — R. N. Calheiros, R. Ranjan, A. Beloglazov, C. A. F. De Rose, R. Buyya, "CloudSim…," *Software: Practice and Experience*, 41(1):23–50, 2011.
- Token Bucket — a standard networking text (e.g., Tanenbaum & Wetherall, *Computer Networks*) with edition/pages.
- Spring Boot 3.4.5 and H2 — official docs with version and access date.
Each must be verified against the real source before submission.

## 9. Logical structure & academic writing — PASSED (one Minor)
All 15 required sections are present and well-ordered (Abstract → Future work → References); the writing is academic and the scope note is prominent.
- 🟡 **Minor (`experiment-results.md`, results table):** the "Max completion" row is a **mean of per-trial maxima** and P95 is a cross-trial mean; this is disclosed in footnote ¹ and in limitations, but the column headers themselves read as single values. *Recommend* relabelling the headers (e.g., "Max (mean of per-trial maxima, sim s)") so a skimming reader is not misled.
- 🟡 **Minor (report depth):** the report is a concise campaign report; for a full dissertation chapter, Background and Methodology would typically be expanded (more detail on CloudSim event model, scheduler internals, and prior work). Acceptable as a campaign artifact; note for the thesis version.

## 10. Presentation outline clarity — PASSED
`presentation-outline.md` has 13 slides (within 12–15), each with title, key points, a recommended chart (mapped to real files in `charts/`), and speaker notes; the narrative (motivation → design → A/B/C results → throughput explanation → limitations → conclusion) communicates the research clearly and keeps the simulation framing on every slide. ✅

## Simulated-vs-real distinction & DDoS claim — PASSED
Every document carries the scope note that completion time is **simulated Cloudlet finish−arrival**, not real HTTP/network latency, and that admission control **sheds load rather than adding capacity**. No document claims real-world DDoS-prevention effectiveness or physical capacity increase. ✅

## Minor items (consolidated)
- M-1: relabel Max/P95 columns to indicate cross-trial aggregation (`experiment-results.md`).
- M-2: optionally note explicitly that S4 and S5-main are identical by construction (both 50/5) — add one line in `experiment-results.md`/`comparative-analysis.md` to pre-empt a "duplicate data?" question.
- M-3: expand Background/Methodology for the full thesis version (`final-research-report.md`).

## Overall assessment
**READY AFTER MINOR CORRECTIONS.**
The experimental data, statistics, methodology, equivalence, Token Bucket analysis, conclusions, limitations, and academic framing are **all verified and sound** — no Critical or data-integrity issues. The one Major item (§8) is **completing the references with genuine, verified citations**, which is a bounded, author-side task that does not affect the validity of the results. Addressing the references plus the Minor labelling items (M-1, M-2) makes the deliverables submission-ready.
