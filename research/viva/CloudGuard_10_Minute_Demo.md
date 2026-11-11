# CloudGuard — 10-Minute Live Demonstration Script

**Goal:** walk the panel through the running CloudGuard application and the *existing* results — **without running any new experiment, creating records, or calling reset.**
**Total time:** ~10 minutes. **URL:** http://localhost:8080
**Existing experiment IDs to open (from the final campaign — do NOT re-run):**
S1 `aa1e37f3…` · S2 `35e590e4…` · S3 `fe654860…` · S4 `fbfe5029…` · S5 `999f0e20…`

> ⚠️ During the demo: **do not click "Run"** on any scenario, **do not click "Reset"/confirm**, and do not use "Run all five." Only *open* saved results, navigate, and export CSV. This keeps the database unchanged.

---

## Step 0 — Before the viva (setup, off the clock)
1. Open a terminal in the project root.
2. Start the app: `mvn -o spring-boot:run` (wait for "Started CloudGuardApplication" / "Tomcat started on port 8080").
3. Confirm health in a browser or terminal: `GET http://localhost:8080/api/health` → `{"status":"UP",...}`.
4. Open http://localhost:8080 and leave it on the **Overview** page.
5. Have the backup assets open in another window (see Backup Plan).

*Say nothing here — this is pre-viva preparation.*

## Step 1 — Start & health (0:00–0:45)
- **Do:** Show the terminal line "Tomcat started on port 8080" and the browser on the Overview page.
- **Say:** "CloudGuard is a Spring Boot application with an embedded CloudSim Plus simulation engine, running locally on port 8080. The engine status in the sidebar shows it is connected."
- **Expected:** sidebar reads "Simulation engine · Connected · CloudSim Plus 8.5.7".

## Step 2 — Dashboard & architecture (0:45–2:15)
- **Do:** Point to the seven sidebar sections (Overview, Infrastructure, Workload Studio, Security Policy, Scenario Manager, Results & Analytics, Experiment History).
- **Say:** "The vanilla-JavaScript frontend talks to a Spring Boot REST API. A request triggers the simulation service, which builds the CloudSim model, applies Token Bucket admission control as a pre-pass, runs the Cloudlets, computes metrics, and persists results to an H2 file database. Rejected tasks are never submitted."
- **Expected:** Overview shows KPI cards and a research-model note stating this is synthetic task pressure, not real DDoS.

## Step 3 — Infrastructure settings (2:15–3:00)
- **Do:** Open **Infrastructure**. Point to the fields.
- **Say:** "The fixed baseline: 2 hosts with 4 cores at 2000 MIPS and 16 GB RAM each, and 4 VMs with 1 core at 2000 MIPS and 4 GB RAM. With SpaceShared scheduling this gives at most four tasks running at once — the capacity bound behind all the queueing we'll see."
- **Expected:** fields show hosts 2, PEs/host 4, MIPS 2000, VM count 4, VM RAM 4096, etc.

## Step 4 — Workload configuration (3:00–3:45)
- **Do:** Open **Workload Studio**. Point to duration, task length, seed, trials.
- **Say:** "Normal load is 100 tasks, elevated is 500, both arriving uniformly over a 60-second window from t=0 to t=60. Task length has a seeded ±10% variation; the seed makes every run reproducible. The campaign used 20 trials per scenario."
- **Expected:** duration 60, task length 10000 MI, seed 42.

## Step 5 — Token Bucket configuration (3:45–4:30)
- **Do:** Open **Security Policy**.
- **Say:** "Admission control is a Token Bucket. It starts full at its capacity and refills at a fixed rate per simulated second; each admitted task costs one token. Default is capacity 50, refill 5. Admitted ≈ capacity + refill × 60 — so 500 elevated tasks give 350 admitted and 150 rejected."
- **Expected:** capacity 50, refill 5; explanatory note that one token = one admitted task.

## Step 6 — Scenarios S1–S5 (4:30–5:30)
- **Do:** Open **Scenario Manager**. Point to the five cards. **Do NOT run.**
- **Say:** "Five scenarios: S1/S2 unprotected at normal/elevated load, S3/S4 the same with admission control, and S5 a Token Bucket threshold sweep over 25/2, 50/5 and 100/10. S1 and S3 (and S2 and S4) use identical settings — only protection differs."
- **Expected:** cards show 100/500 tasks, Normal/Elevated, Policy Disabled/Enabled/Sweep.

## Step 7 — Open elevated results, S2 vs S4 (5:30–7:15)
- **Do:** Open **Results & Analytics**. In the "Selected experiment" dropdown, choose the saved **S4** run (`fbfe5029…`).
- **Say:** "Here are the five KPI cards for S4: 500 generated, 350 admitted, 150 rejected, 30% rejected workload, average completion 424 simulated seconds. The donut shows admitted vs rejected."
- **Do:** Scroll to "Protected vs unprotected — average completion time."
- **Say:** "This compares S1 vs S3 (normal) and S2 vs S4 (elevated). Normal is identical — admission is transparent. Elevated: unprotected S2 averages 612 seconds, protected S4 averages 424 — about 31% lower, because fewer tasks queue. Note the context strip confirms the two runs used matching workload and infrastructure."
- **Expected:** S2 bar ~614/612, S4 bar ~424–426; "parameters match"; completion-time methodology note ("completed tasks only; simulated seconds").

## Step 8 — Throughput & rejection insight (7:15–8:00)
- **Do:** Point to the completion-time detail table and (from the deck, if the UI lacks a throughput chart on screen) state the throughput figures.
- **Say:** "Crucially, throughput is almost identical — about 0.39 tasks per simulated second in both — because it is bounded by the four-core service capacity. Admission control reduces waiting, not the processing rate. It sheds load; it does not add capacity."
- **Expected:** detail table shows avg/P95/max/makespan/throughput for S4.

## Step 9 — S5 threshold analysis (8:00–9:00)
- **Do:** In the selector, open the saved **S5** run (`999f0e20…`). Scroll to "S5 Token Bucket threshold analysis."
- **Say:** "S5 sweeps three admission settings on the elevated workload. Rejection falls 71% → 30% → 0% as capacity/refill loosen (25/2 → 50/5 → 100/10), while admitted-task completion time rises 165 → 424 → 612 seconds. The detailed table lists all three. 100/10 admits everything and matches unprotected S2 — a clean monotonic trade-off."
- **Expected:** rejection-by-config chart (71/30/0%), admission-vs-rejection chart, and a 3-row detailed table.

## Step 10 — History & CSV export (9:00–10:00)
- **Do:** Open **Experiment History**. Show the list (newest first). Open one record via "Open →". Point to a row's **CSV** link.
- **Say:** "Every experiment is persisted with its full request and result. History lists them newest-first; each can be reopened or exported to CSV for per-task analysis. The research campaign's five experiments and their CSV exports are included in the submission."
- **Do (optional, safe):** Click a **CSV** link to show it downloads `task_id, arrival, start, finish, completion, status` rows (a read-only GET — creates nothing).
- **Say (close):** "To summarise: admission control is transparent at normal load, and under elevated load it trades 30% rejection for ~31% faster admitted-task completion, with throughput unchanged. Thank you."

---

## Expected observations (quick reference)
| Where | What to expect |
|---|---|
| S1 / S3 | 100/100/0, avg 103.6 s, identical (transparent protection) |
| S2 | 500/500/0, avg 611.9 s, throughput 0.3908 |
| S4 | 500/350/150, 30% rejected, avg 424.2 s, throughput 0.3891 |
| S5 | 25/2→145 (71%), 50/5→350 (30%), 100/10→500 (0%); avg 165/424/612 s |
| Comparison strip | "parameters match"; completion-time note present |
| History | records listed newest-first; CSV links per row |

## Backup plan (if the application fails during the viva)
1. **Primary fallback — the slide deck:** present `CloudGuard_Final_MTech_Presentation.pptx`. Slides 11–13 contain the same charts (mean, rejection, throughput, P95, S5 threshold) rendered from the exact data.
2. **Charts:** show `research/final-campaign/charts/png/*.png` directly (mean-completion-time, rejection-rate, throughput, p95-completion-time, s5-threshold-*).
3. **Raw data & CSV:** open `research/final-campaign/exports/S1–S5-tasks.csv` and `research/final-campaign/summary.json` to show the actual per-task and aggregate numbers.
4. **If the app won't start:** say "The results are deterministic and already captured; let me show them from the saved exports," then use (1)–(3). Do **not** attempt a reset or re-run under time pressure.
5. **If only the frontend misbehaves:** hit the API directly in a browser — `GET /api/experiments` (list) and `GET /api/experiments/fbfe5029-f53d-4e60-a6fa-94b6b4a0aa9f` (S4 result JSON).

**Recovery note:** nothing in this demo creates or deletes data; if a page looks stale, just refresh (Ctrl+F5). Never click Reset during the viva.
