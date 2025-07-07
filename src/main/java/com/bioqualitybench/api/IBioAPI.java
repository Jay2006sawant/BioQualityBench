package com.bioqualitybench.api;

import com.bioqualitybench.dto.BiometricsDto;
import com.bioqualitybench.exception.BiometricException;

/**
 * Pluggable biometric SDK contract, analogous to MOSIP {@code BioProvider} / device integration.
 */
public interface IBioAPI {

    double getSDKScore(BiometricsDto dto) throws BiometricException;
}
