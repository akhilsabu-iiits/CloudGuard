# CloudGuard – Cloud Security Simulation Platform

> **M.Tech Cyber Security research project** — _Performance Evaluation of Cloud Services Under Simulated DoS Workloads_
>
> A **CloudSim Plus** simulation of computational task workloads and Token Bucket admission control. Cloudlets represent computational tasks; completion time is **simulated** Cloudlet finish − arrival time. This is **not** real network-layer DoS/DDoS testing (see [Important research scope](#important-research-scope)).

CloudGuard is a browser-based research platform for evaluating cloud task execution under controlled synthetic workload pressure. The Java backend uses CloudSim Plus; the browser UI is served by the same Spring Boot application. A hosted deployment means end users only need a modern browser.

## Application Screenshots

A visual tour of the running CloudGuard interface (light theme), captured from the live application at `http://localhost:8080`.

| | |
|:---:|:---:|
| ![CloudGuard Overview](docs/images/cloudguard-overview.png) | ![Infrastructure Configuration](docs/images/cloudguard-infrastructure.png) |
| **Overview dashboard** — latest-run KPIs (completed, rejected %, avg completion, protection) and the five research scenarios. | **Infrastructure** — the fixed datacenter/VM baseline (2 hosts × 4 PE, 4 VMs) shared across experiments. |
| ![Workload Studio](docs/images/cloudguard-workload.png) | ![Security Policy](docs/images/cloudguard-security-policy.png) |
| **Workload Studio** — 100/500-task workloads over a 60 s window with seeded ±10% task-length variation. | **Security Policy** — Token Bucket admission control (capacity / refill per simulated second). |
| ![Scenario Manager](docs/images/cloudguard-scenario-manager.png) | ![Results & Analytics](docs/images/cloudguard-results-analytics.png) |
| **Scenario Manager** — select and run S1–S5 (protected vs unprotected, plus the threshold sweep). | **Results & Analytics** — KPI cards and task-disposition donut (shown: S4 — 500 generated, 350 admitted, 150 rejected = 30%). |
| ![Experiment History](docs/images/cloudguard-experiment-history.png) | |
| **Experiment History** — saved runs, reopen for analysis, and per-task CSV export. | |

## Technology Stack

| Layer | Technology |
|---|---|
| Language / runtime | Java 17 |
| Backend framework | Spring Boot 3.4.5 (REST API, validation, dependency injection) |
| Simulation engine | CloudSim Plus 8.5.7 (discrete-event cloud simulation) |
| Persistence | H2 file-backed database (JPA / Hibernate); request + result stored as JSON |
| Frontend | Vanilla JavaScript, HTML, CSS single-page app (SVG analytics, CSV export) |
| Build / deploy | Maven; Docker; local at `http://localhost:8080` |
| Testing | JUnit (metrics + Token Bucket); headless-Chrome UI verification |

## System Architecture

![CloudGuard system architecture](docs/architecture/cloudguard-architecture.png)

_Text view of the same flow:_

```
Browser SPA (JavaScript / HTML / CSS)
        │  REST  (/api/*)
Spring Boot controller ──► CloudSim Plus simulation service
        │                       ├─ Token Bucket admission (deterministic pre-pass, before simulation.start())
        │                       └─ MetricsCalculator (completion time, makespan, throughput, multi-trial stats)
        └──► H2 file database (persists request + result JSON)
```

Admission control is applied as a deterministic pre-pass over the arrival schedule, so **rejected tasks are never submitted** to CloudSim Plus. Admitted Cloudlets execute on the VMs (SpaceShared), metrics are computed over completed tasks, and results are persisted to H2 and rendered as SVG analytics.

## Current implementation

- Responsive browser UI with overview, infrastructure, workload, security, scenario, analytics and history pages.
- Spring Boot REST API.
- CloudSim Plus 8.5.7 simulation dependency, pinned for reproducibility.
- S1–S5 experiment definitions.
- Deterministic Token Bucket admission control evaluated against simulated arrival time.
- Controlled uniform task arrivals using Cloudlet submission delays.
- Task-level completion-time results, rejected-workload percentage, throughput and makespan.
- S5 threshold sweep (25/2, 50/5, 100/10 capacity/refill-per-simulated-second).
- Repeated trials and H2 file-backed experiment history.
- CSV export of task-level results.
- Docker image definition for deployment.

## Important research scope

CloudSim Plus simulates cloud infrastructure and Cloudlet execution. This project models DoS-like workload pressure as synthetic task arrivals and models protection as a task-admission policy. It does not generate real HTTP traffic, packets, volumetric DDoS traffic, or measure a production service's network latency. Do not describe the results as proof of real-world DDoS prevention.

The current workload generator uses uniformly spaced arrivals across the selected workload window, with seeded ±10% task-length variation. This is an initial reproducible workload model. If the research design later requires Poisson or burst arrivals, those should be added as explicit workload models and evaluated separately.

Repeated trials use the same uniformly spaced, deterministic arrival times for every trial; only the ±10% task-length randomness changes, because the trial index is added to the base random seed (`seed + trial index`). As a result, admission decisions (and therefore admitted/rejected counts) do not vary across trials — trials capture task-length variance only, not arrival variance. Reported trial metrics are averaged across the trials.

## Scenarios

| Scenario | Tasks | Admission policy | Purpose |
|---|---:|---|---|
| S1 | 100 | Disabled | Normal-load baseline |
| S2 | 500 | Disabled | Elevated-load baseline |
| S3 | 100 | Enabled | Protection overhead under normal load |
| S4 | 500 | Enabled | Protection under elevated load |
| S5 | 500 | Enabled; 3 thresholds | Policy threshold comparison |

S1–S5 use the same infrastructure profile unless a user intentionally changes it. The same task-arrival window and task-length configuration should be retained for matched comparisons.

Admission control is **scenario-driven**: S1/S2 run unprotected and S3/S4/S5 run protected. The API request field `Security.enabled` does **not** currently toggle protection — the selected scenario determines whether the Token Bucket is applied. S5 evaluates three **hardcoded** Token Bucket threshold settings (capacity / refill-per-simulated-second): 25/2, 50/5 and 100/10; the request-supplied capacity and refill values are not used for the S5 sweep.

## Token Bucket Admission Control

Protection is a **Token Bucket** admission policy, evaluated against **simulated** arrival time as a deterministic pre-pass *before* the CloudSim run:

- The bucket starts **full** (= capacity) and **refills** at a fixed rate per simulated second, up to capacity.
- Each generated task costs **one token**: a task is **admitted** if a token is available at its arrival time, otherwise **rejected** and never submitted to CloudSim Plus.
- Under saturated demand, `admitted ≈ min(generated, capacity + refill × window)` — e.g. 50 / 5 over a 60 s window admits `min(500, 350) = 350`.

Admission control **sheds computational workload; it does not increase physical infrastructure capacity.** Lower completion times for admitted tasks come at the cost of rejecting incoming tasks. S5 compares three capacity/refill settings — 25/2, 50/5 and 100/10.

## Metrics

Task counters (reported for the main result and for each S5 variant):

- **generatedTasks** – total synthetic tasks created for the workload.
- **admittedTasks** – tasks allowed through Token Bucket admission control and submitted to CloudSim Plus.
- **rejectedTasks** – generated tasks rejected by Token Bucket admission control (never submitted to CloudSim Plus).
- **completedTasks** – admitted tasks whose Cloudlets completed successfully in CloudSim.
- **unfinishedTasks** – admitted tasks that neither completed nor were explicitly failed (for example, still queued or resource-starved when the simulation ended).
- **failedTasks** – only tasks CloudSim explicitly reported as failed (status `FAILED` / `FAILED_RESOURCE_UNAVAILABLE`).

Counter invariant (holds for the main result and every S5 variant):

```
completedTasks + unfinishedTasks + failedTasks = admittedTasks
```

Rejected tasks are tracked separately from the admitted-task invariant and satisfy `admittedTasks + rejectedTasks = generatedTasks`.

Timing and rate metrics:

- Rejected workload percentage = rejectedTasks / generatedTasks × 100.
- Per-task completion time = Cloudlet finish time − configured task arrival time.
- Average, P95 and maximum completion time for completed Cloudlets.
- Makespan = latest finish time (completed) − earliest admitted task arrival time; 0 if no task completes.
- Throughput = completedTasks / makespan; 0 when makespan is 0 (guarded against division by zero).

Task-level CSV currently includes completed Cloudlets; rejected and unfinished tasks are represented in aggregate counters, not as Cloudlet records.

### Resource-utilisation model (configured demand, not measured output)

Each Cloudlet is configured with separate CloudSim Plus utilisation models:

- CPU: 50%
- RAM: 5%
- Bandwidth: 5%

These are **configured utilisation demands** that drive the simulation — they describe how much of a VM's CPU, RAM and bandwidth each task requests. They are **not** measured experiment output metrics. The application does **not** currently sample or report actual host/VM CPU or RAM utilisation, and it does **not** compute a protection-overhead metric. Both are listed as future work under "Current limitations / next engineering tasks" below.

## Experimental Results

Results from the final research campaign (**20 trials per scenario**, seeds 42–61; ± = 95% confidence interval, Student-t, df = 19). All completion times are **simulated** Cloudlet finish − arrival time — not real network latency.

| Scenario | Admitted | Rejected | Rejection % | Avg completion (sim s) | Makespan (sim s) | Throughput (tasks/sim-s) |
|---|---:|---:|---:|---:|---:|---:|
| S1 = S3 | 100 | 0 | 0 | 103.608 ± 0.429 | 258.985 | 0.3861 |
| S2 | 500 | 0 | 0 | 611.868 ± 0.919 | 1279.555 | 0.3908 |
| S4 | 350 | 150 | 30 | 424.215 ± 0.743 | 899.463 | 0.3891 |
| S5 — 25/2 | 145 | 355 | 71 | 164.959 | — | 0.3835 |
| S5 — 50/5 | 350 | 150 | 30 | 424.215 | 899.463 | 0.3891 |
| S5 — 100/10 | 500 | 0 | 0 | 611.868 | 1279.555 | 0.3908 |

**Key findings.** Admission control is **transparent at normal load** (S1 ≡ S3, 0 rejection). Under elevated load it rejects 30% of tasks and cuts admitted-task average completion time by **~31%** (611.9 → 424.2 s) and makespan by **~30%**, while **throughput stays ≈ constant** (capacity-bound by the 4 VM processing elements). The S5 sweep shows a monotonic rejection↔latency trade-off (tighter capacity/refill → more rejection, faster admitted-task completion). **Admission control sheds computational workload; it does not add physical infrastructure capacity** — lower completion times for admitted tasks come at the cost of rejecting incoming tasks.

## API

- `GET /api/health`
- `GET /api/scenarios`
- `POST /api/experiments/run`
- `POST /api/experiments/suite`
- `GET /api/experiments`
- `GET /api/experiments/{id}`
- `GET /api/experiments/{id}/export.csv`

## Deployment

The browser experience requires a server. Users do not install Java, Maven, Node.js, Docker or a database locally; those are build/runtime concerns of the hosting environment.

### Build & run locally (Maven)

Requires Java 17 and Maven. From the project root:

```bash
mvn spring-boot:run          # starts the app at http://localhost:8080
# or build a jar:
mvn clean package && java -jar target/*.jar
```

Run the unit tests with `mvn test`, then open `http://localhost:8080`.

### Docker

Build and run with Docker:

```bash
docker compose up --build -d
```

Open `http://localhost:8080` for a local smoke test. For a public browser URL, deploy this Dockerfile to a container hosting service and attach persistent storage at `/app/data` (the current H2 file database). Set `PORT` to the port supplied by the host. For a multi-user production deployment, replace H2 with managed PostgreSQL and add authentication/authorization before exposing the app publicly.

## Repository Structure

```text
CloudGuard/
├── README.md
├── pom.xml · Dockerfile · compose.yaml
├── .gitignore
├── src/                          # Spring Boot + CloudSim Plus app (Java) and frontend (static/)
└── docs/
    ├── images/                   # application screenshots
    ├── architecture/             # architecture diagram (SVG + PNG)
    └── RESEARCH_CONFIGURATION.md  # frozen, source-verified baseline
```

## Project Documentation

The frozen, source-verified configuration baseline is in [`docs/RESEARCH_CONFIGURATION.md`](docs/RESEARCH_CONFIGURATION.md). The full research report, experiment data, charts, presentation and viva materials are maintained separately as part of the M.Tech submission and are not included in this repository.

## Version and licensing notes

CloudSim Plus is pinned to 8.5.7 in `pom.xml`. Maven Central lists this release as Java 17+ compatible. The current upstream repository has moved its development branch to newer Java requirements, so this project intentionally pins a released version rather than using `LATEST` or a snapshot. Review CloudSim Plus's GPL-3.0 license and include the required notices when redistributing the application.

**Note:** this repository does not yet include a top-level `LICENSE` file — add one that is compatible with CloudSim Plus's GPL-3.0 before public redistribution.

## Acknowledgements

- **CloudSim Plus** (Silva Filho et al., IFIP/IEEE IM 2017) and the original **CloudSim** toolkit (Calheiros et al., *Software: Practice and Experience*, 2011) — the simulation framework this project builds on.
- **Spring Boot** and the **H2 Database Engine** — application framework and persistence layer.

Full citations appear in the project's final research report (§References).

## Current limitations / next engineering tasks

1. Run a full Maven/Docker build and verify all CloudSim Plus API calls in the target environment.
2. Add authentication and per-user access controls before public multi-user deployment.
3. Add a durable production database such as PostgreSQL for shared hosting.
4. Add server-side job queue/cancellation and progress updates for long experiment suites.
5. Add validated CPU/RAM utilisation sampling and a protection-overhead metric and report methodology. Neither CPU/RAM utilisation measurement nor protection-overhead measurement is implemented yet; the CPU/RAM/bandwidth figures in this document are configured Cloudlet demands, not measured output.
6. Add Poisson and burst-arrival workload models if required by the approved research methodology.
7. Tests already implemented: `MetricsCalculatorTest` covers metric-formula calculations (makespan, throughput, completion time), full/partial/zero completion, explicit-failure separation and the counter invariant (completedTasks + unfinishedTasks + failedTasks = admittedTasks); `TokenBucketTest` covers admission when the bucket is full/empty, simulation-time refill and the capacity upper bound. Remaining test-coverage gaps: repeated-run (multi-trial) aggregation, scenario-level invariants (for example, unprotected scenarios rejecting no tasks), the end-to-end CloudSim `runOnce` simulation path, and REST API endpoint tests.
