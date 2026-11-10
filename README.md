# CloudGuard – Cloud Security Simulation Platform

CloudGuard is a browser-based research platform for evaluating cloud task execution under controlled synthetic workload pressure. The Java backend uses CloudSim Plus; the browser UI is served by the same Spring Boot application. A hosted deployment means end users only need a modern browser.

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

Build and run with Docker:

```bash
docker compose up --build -d
```

Open `http://localhost:8080` for a local smoke test. For a public browser URL, deploy this Dockerfile to a container hosting service and attach persistent storage at `/app/data` (the current H2 file database). Set `PORT` to the port supplied by the host. For a multi-user production deployment, replace H2 with managed PostgreSQL and add authentication/authorization before exposing the app publicly.

## Version and licensing notes

CloudSim Plus is pinned to 8.5.7 in `pom.xml`. Maven Central lists this release as Java 17+ compatible. The current upstream repository has moved its development branch to newer Java requirements, so this project intentionally pins a released version rather than using `LATEST` or a snapshot. Review CloudSim Plus's GPL-3.0 license and include the required notices when redistributing the application.

## Current limitations / next engineering tasks

1. Run a full Maven/Docker build and verify all CloudSim Plus API calls in the target environment.
2. Add authentication and per-user access controls before public multi-user deployment.
3. Add a durable production database such as PostgreSQL for shared hosting.
4. Add server-side job queue/cancellation and progress updates for long experiment suites.
5. Add validated CPU/RAM utilisation sampling and a protection-overhead metric and report methodology. Neither CPU/RAM utilisation measurement nor protection-overhead measurement is implemented yet; the CPU/RAM/bandwidth figures in this document are configured Cloudlet demands, not measured output.
6. Add Poisson and burst-arrival workload models if required by the approved research methodology.
7. Tests already implemented: `MetricsCalculatorTest` covers metric-formula calculations (makespan, throughput, completion time), full/partial/zero completion, explicit-failure separation and the counter invariant (completedTasks + unfinishedTasks + failedTasks = admittedTasks); `TokenBucketTest` covers admission when the bucket is full/empty, simulation-time refill and the capacity upper bound. Remaining test-coverage gaps: repeated-run (multi-trial) aggregation, scenario-level invariants (for example, unprotected scenarios rejecting no tasks), the end-to-end CloudSim `runOnce` simulation path, and REST API endpoint tests.
