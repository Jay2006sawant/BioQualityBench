package com.bioqualitybench.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenchmarkResponse {

    /** Original secure biometric interface / capture quality score from the request. */
    private Double originalSbiScore;

    /** Score returned by the SDK after benchmarking, or the SBI fallback if the SDK failed. */
    private double evaluatedSdkScore;

    /** Simple name of the resolved provider implementation. */
    private String providerUsed;

    /** Wall-clock processing time for the benchmark path in milliseconds. */
    private long processingTimeMs;
}
