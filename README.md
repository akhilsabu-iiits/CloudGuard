<div align="center">

# CloudGuard

### Performance Evaluation of Cloud Services Under Simulated DoS Workloads

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![CloudSim Plus](https://img.shields.io/badge/CloudSim_Plus-8.5.7-3768E8?style=flat-square)](https://cloudsimplus.org)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com)

</div>

## Overview

CloudGuard measures how a cloud service performs under normal and elevated task-arrival pressure, and how a **Token Bucket admission policy** changes that performance. A Spring Boot backend drives a CloudSim Plus simulation; a browser dashboard runs the scenarios and shows the results.

> **Simulation only.** No real network traffic is generated. A "DoS workload" is an elevated rate of computational tasks inside CloudSim Plus, and completion time is the simulated task *finish − arrival* time.

## Architecture

![CloudGuard architecture and scenario matrix](docs/architecture/scenario-architecture.png)

## Token Bucket Policy

![Token Bucket admission control mechanism](docs/architecture/token-bucket-policy.png)

The bucket holds 50 tokens and refills at 5 per simulated second. Each task costs one token: it is admitted if a token is available at its arrival time and refused otherwise.

## Scenarios

| Scenario | Workload | Tasks | Admission policy |
|:---:|---|:---:|:---:|
| **S1** | Normal | 100 | Off |
| **S2** | Elevated | 500 | Off |
| **S3** | Normal | 100 | On |
| **S4** | Elevated | 500 | On |
| **S5** | Elevated, threshold sweep | 500 each | On |

Tasks arrive evenly over a 60-second window on 2 hosts and 4 VMs (2000 MIPS, SpaceShared scheduler). Each scenario runs 20 trials. Runs with and without the policy differ only in the policy.

## Results

| Workload | Before policy | After policy | Workload rejected |
|---|:---:|:---:|:---:|
| Normal (S1 vs S3) | 103.6 s | 103.6 s | 0% |
| Elevated (S2 vs S4) | 611.9 s | 424.2 s | 30% (150 of 500) |

*Average task completion time over 20 trials.*

- Under elevated workload the policy cuts average completion time by **30.7%**.
- It does so by refusing work: **150 of 500 tasks** receive no service.
- Under normal workload the bucket never empties, so nothing changes.

## Tech Stack

| Layer | Technology |
|---|---|
| Simulation | CloudSim Plus 8.5.7 |
| Backend | Java 17 · Spring Boot 3.4.5 · REST API |
| Persistence | H2 file-backed database |
| Frontend | Vanilla JavaScript · HTML · CSS |

## Getting Started

```bash
git clone https://github.com/akhilsabu-iiits/CloudGuard.git
cd CloudGuard
```

**Maven** (Java 17 and Maven)

```bash
mvn spring-boot:run
```

**Docker**

```bash
docker compose up --build -d
```

Open the dashboard at **http://localhost:8080**.

## Team

Group 4 · M.Tech Cybersecurity · IIIT Sri City · Cloud Security course project

| Name | Roll number |
|---|---|
| Akhil Sabu | OMCS25S00005 |
| Naveen Kumar Kothi | OMCS25S00053 |
| S Vaishak | OMCS25S00070 |

## Acknowledgements

Built on [CloudSim Plus](https://cloudsimplus.org) (GPL-3.0), Spring Boot and the H2 Database Engine.
