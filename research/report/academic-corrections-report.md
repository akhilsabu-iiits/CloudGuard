# CloudGuard — Academic Corrections Report

Implements the corrections from `academic-readiness-audit.md`. **Markdown documents only** — no source code, experiments, database, or Git operations.

## 1. References verified (replaces all `[verify]` placeholders in `final-research-report.md` §14)
Formatted in IEEE style; in-text markers `[1]`–`[6]` correspond to the bibliography.

| # | Reference | Verification | Identifier / URL |
|---|---|---|---|
| [1] | Silva Filho, Oliveira, Monteiro, Inácio, Freire — "CloudSim Plus…," IFIP/IEEE IM 2017, pp. 400–406 | **Verified** (title, venue, year, pages, DOI) | doi: 10.23919/INM.2017.7987304 · https://ieeexplore.ieee.org/document/7987304 |
| [2] | CloudSim Plus — official site & source repository | **Verified** (official URLs) | https://cloudsimplus.org/ · https://github.com/cloudsimplus/cloudsimplus |
| [3] | Calheiros, Ranjan, Beloglazov, De Rose, Buyya — "CloudSim…," *Software: Practice and Experience*, 41(1):23–50, 2011 | **Verified** (authors, journal, vol/issue/pages, year, DOI) | doi: 10.1002/spe.995 |
| [4] | Turner, J. S. — "New directions in communications…," *IEEE Communications Magazine*, 24(10):8–15, Oct. 1986 | **Verified** title/venue/vol/issue/pages/ISSN; cited as the foundational leaky-/token-bucket policer reference | ISSN 0163-6804 (DOI not independently confirmed — **not asserted**) |
| [5] | Spring Team (Broadcom) — "Spring Boot Reference Documentation," v3.4.5 | **Verified**: v3.4.5 Javadoc and GitHub release confirmed; general docs root confirmed | https://www.javadoc.io/doc/org.springframework.boot/spring-boot/3.4.5 · https://github.com/spring-projects/spring-boot/releases/tag/v3.4.5 · https://docs.spring.io/spring-boot/ |
| [6] | H2 Database Engine — official documentation | Official project site (canonical) | https://www.h2database.com/ |

### References that remain partially unverified (clearly labelled, not fabricated)
- **[4] Turner (1986) DOI:** the title, venue, volume(24)/issue(10)/pages(8–15)/year(1986)/ISSN were verified, but a DOI was **not** independently confirmed, so **no DOI was asserted** for this entry.
- **[5] Spring Boot 3.4.5 reference-guide URL:** the version-pinned reference page (`docs.spring.io/spring-boot/3.4.5/reference/`) was not returned in search (the 3.4.4 line was), so the entry cites the confirmed 3.4.5 **Javadoc** and **release tag** plus the versionless docs root rather than asserting an unconfirmed pinned URL.
- No author, DOI, page number, or publication detail was invented. Reference [4] is cited **only** for the admission-control (token-bucket) algorithm family, not as evidence for any cloud-simulation result.

## 2. Corrections implemented
1. **References (`final-research-report.md`):** §4 Background now carries in-text citations `[1]–[6]`; §14 replaced the five `[verify]` placeholders with the six verified IEEE-style references above. Zero `[verify]` markers remain; in-text markers correspond to the bibliography (checked).
2. **Aggregate labels (`experiment-results.md`):** the P95 and Maximum rows are relabelled "mean of per-trial P95" and "mean of per-trial maxima"; the existing footnote and limitations explanation are retained. **All numerical values unchanged.**
3. **S4 vs S5-main (`comparative-analysis.md`):** added a note that S4 and the primary S5 result both use the Token Bucket **50/5** configuration, so their results match **by construction** on equivalent settings, and clarifying that **S5 is primarily a threshold-sensitivity experiment** (25/2, 50/5, 100/10), not an independent fifth protection configuration. **No experimental results altered.**

## 3. Consistency review (§4 of the request)
- **In-text citations:** `[1]–[6]` used in-text all correspond to defined bibliography entries (verified by inspection). No orphan or dangling citations.
- **Terminology:** consistent ("simulated Cloudlet completion time," "admitted-task completion time," "sheds workload").
- **Simulated vs real distinction:** preserved on every document; the report's scope note and body state that completion time is simulated finish−arrival, **not** real HTTP/network latency.
- **No DDoS-prevention / capacity claims:** confirmed — no affirmative claim of real-world DDoS prevention or physical-capacity increase; the "sheds load, not capacity" framing is explicit.
- **No unsupported claims:** the only inferential statement (S2/S4 difference) is correctly qualified as a confidence-interval observation, not a significance test.

## 4. Numerical integrity
All headline values (103.608, 611.868, 424.215, 258.985, 899.463, 0.3908, 0.3891, 164.959, 795.434, P95/Max figures, S5 variants) remain present and unchanged in the edited documents. Only labels, citations, and explanatory prose were edited. The `raw/`, `exports/`, `charts/`, `summary.json`, and `docs/RESEARCH_CONFIGURATION.md` were **not** modified.

## 5. Safety confirmation
- **No experiment was run**; **no database operation** was performed (one read-only `GET /api/experiments` was issued solely as a safety confirmation and created/deleted nothing; the live count is driven by external activity and is irrelevant to these markdown corrections, which reference the static `raw/` snapshots).
- **No source code** (Java/JS/HTML/CSS) was modified; the frozen experimental configuration is unchanged.
- **No Git operation** (no stage/commit/push); the five `raw/` snapshots and all original campaign data are preserved.

## 6. Files modified by this correction pass
- `research/final-campaign/final-research-report.md` (§4 Background, §14 References)
- `research/final-campaign/experiment-results.md` (P95/Max row labels)
- `research/final-campaign/comparative-analysis.md` (S4/S5-main note)
- `research/final-campaign/academic-corrections-report.md` (this file, new)

## Sources consulted for reference verification
- CloudSim Plus (IM 2017): https://ieeexplore.ieee.org/document/7987304 · https://cloudsimplus.org/ · https://github.com/cloudsimplus/cloudsimplus
- CloudSim (SPE 2011, doi:10.1002/spe.995): Wiley Online Library listing
- Turner (1986) leaky/token bucket: IEEE Communications Magazine 24(10):8–15
- Spring Boot 3.4.5: https://www.javadoc.io/doc/org.springframework.boot/spring-boot/3.4.5 · https://github.com/spring-projects/spring-boot/releases/tag/v3.4.5
- H2: https://www.h2database.com/
