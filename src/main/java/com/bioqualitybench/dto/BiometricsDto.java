package com.bioqualitybench.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricsDto {

    /** Quality score from the secure biometric interface / capture device path (SBI). */
    private Double qualityScore;

    /** Last known or placeholder SDK-evaluated score; may be updated by benchmarking. */
    private Double sdkScore;

    /** ISO-encoded biometric payload, typically Base64 for transport. */
    @NotNull
    private String biometricData;

    @NotNull
    private Modality modality;
}
