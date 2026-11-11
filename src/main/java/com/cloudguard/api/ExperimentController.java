package com.cloudguard.api;

import com.cloudguard.model.ExperimentResult;
import com.cloudguard.model.RunRequest;
import com.cloudguard.persistence.ExperimentEntity;
import com.cloudguard.persistence.ExperimentRepository;
import com.cloudguard.service.CloudSimSimulationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ExperimentController {
    private final CloudSimSimulationService simulator;
    private final ExperimentRepository repository;
    private final ObjectMapper mapper;

    public ExperimentController(CloudSimSimulationService simulator, ExperimentRepository repository, ObjectMapper mapper) {
        this.simulator=simulator; this.repository=repository; this.mapper=mapper;
    }

    @GetMapping("/health")
    public Map<String,Object> health() { return Map.of("status","UP","application","CloudGuard","simulationEngine","CloudSim Plus 8.5.7"); }

    @GetMapping("/scenarios")
    public List<Map<String,Object>> scenarios() {
        return simulator.scenarios().stream().map(s -> Map.<String,Object>of(
            "id",s.id(),"name",s.name(),"tasks",s.tasks(),"protected",s.protectedMode(),"thresholdSweep",s.thresholdSweep())).toList();
    }

    @PostMapping("/experiments/run")
    public ExperimentResult run(@Valid @RequestBody RunRequest request) throws Exception {
        if (request.scenarioId()==null || request.scenarioId().isBlank()) throw new IllegalArgumentException("scenarioId is required");
        ExperimentResult result = simulator.run(UUID.randomUUID().toString(), request.scenarioId(), request);
        persist(result, request);
        return result;
    }

    @PostMapping("/experiments/suite")
    public List<ExperimentResult> runSuite(@Valid @RequestBody RunRequest request) throws Exception {
        List<ExperimentResult> results = new ArrayList<>();
        for (String id : List.of("S1","S2","S3","S4","S5")) {
            RunRequest suiteRequest = new RunRequest(id, request.infrastructure(),
                request.workload()==null ? null : new RunRequest.Workload(null, request.workload().durationSeconds(), request.workload().taskLengthMi(), request.workload().randomSeed()),
                request.security(), request.trials());
            ExperimentResult result = simulator.run(UUID.randomUUID().toString(), id, suiteRequest);
            persist(result, suiteRequest);
            results.add(result);
        }
        return results;
    }

    @GetMapping("/experiments")
    public List<Map<String,Object>> history() {
        return repository.findTop50ByOrderByCreatedAtDesc().stream().map(e -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id",e.getId()); m.put("scenarioId",e.getScenarioId()); m.put("scenarioName",e.getScenarioName());
            m.put("createdAt",e.getCreatedAt()); m.put("status",e.getStatus()); return m;
        }).toList();
    }

    @GetMapping("/experiments/{id}")
    public ResponseEntity<String> get(@PathVariable String id) {
        return repository.findById(id).map(e -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(e.getResultJson()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value="/experiments/{id}/export.csv", produces="text/csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable String id) throws Exception {
        ExperimentEntity e = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Experiment not found"));
        ExperimentResult r = mapper.readValue(e.getResultJson(), ExperimentResult.class);
        StringBuilder csv = new StringBuilder("task_id,arrival_time_seconds,start_time_seconds,finish_time_seconds,completion_time_seconds,status\n");
        for (ExperimentResult.TaskResult t : r.tasks()) csv.append(t.taskId()).append(',').append(t.arrivalTimeSeconds()).append(',')
            .append(t.startTimeSeconds()).append(',').append(t.finishTimeSeconds()).append(',').append(t.completionTimeSeconds()).append(',').append(t.status()).append('\n');
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=cloudguard-"+id+".csv")
            .contentType(new MediaType("text","csv", StandardCharsets.UTF_8)).body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void persist(ExperimentResult result, RunRequest request) throws Exception {
        repository.save(new ExperimentEntity(result.experimentId(), result.scenarioId(), result.scenarioName(),
            result.createdAt(), result.status(), mapper.writeValueAsString(request), mapper.writeValueAsString(result)));
    }
}
