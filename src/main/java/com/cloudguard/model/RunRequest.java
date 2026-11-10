package com.cloudguard.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RunRequest(
    String scenarioId,
    @Valid Infrastructure infrastructure,
    @Valid Workload workload,
    @Valid Security security,
    @Min(1) @Max(20) Integer trials
) {
    public record Infrastructure(
        @Min(1) @Max(8) Integer hosts,
        @Min(1) @Max(32) Integer pesPerHost,
        @Min(100) @Max(100000) Long mipsPerPe,
        @Min(512) @Max(262144) Long ramMbPerHost,
        @Min(1) @Max(64) Integer vmCount,
        @Min(1) @Max(32) Integer pesPerVm,
        @Min(100) @Max(100000) Long vmMips,
        @Min(256) @Max(131072) Long vmRamMb
    ) {}
    public record Workload(
        @Min(1) @Max(10000) Integer taskCount,
        @Min(1) @Max(3600) Integer durationSeconds,
        @Min(1000) @Max(100000000) Long taskLengthMi,
        Long randomSeed
    ) {}
    public record Security(
        Boolean enabled,
        @Min(1) @Max(10000) Integer bucketCapacity,
        @Min(1) @Max(10000) Integer refillTokensPerSecond
    ) {}
}
