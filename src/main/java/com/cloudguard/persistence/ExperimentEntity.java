package com.cloudguard.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "experiments")
public class ExperimentEntity {
    @Id private String id;
    private String scenarioId;
    private String scenarioName;
    private Instant createdAt;
    private String status;
    @Lob @Column(columnDefinition = "CLOB") private String requestJson;
    @Lob @Column(columnDefinition = "CLOB") private String resultJson;
    protected ExperimentEntity() {}
    public ExperimentEntity(String id, String scenarioId, String scenarioName, Instant createdAt, String status, String requestJson, String resultJson) {
        this.id=id; this.scenarioId=scenarioId; this.scenarioName=scenarioName; this.createdAt=createdAt; this.status=status; this.requestJson=requestJson; this.resultJson=resultJson;
    }
    public String getId(){return id;} public String getScenarioId(){return scenarioId;} public String getScenarioName(){return scenarioName;}
    public Instant getCreatedAt(){return createdAt;} public String getStatus(){return status;} public String getResultJson(){return resultJson;}
    public String getRequestJson(){return requestJson;}
}
