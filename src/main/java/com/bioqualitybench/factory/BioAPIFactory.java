package com.bioqualitybench.factory;

import com.bioqualitybench.api.IBioAPI;
import com.bioqualitybench.dto.BiometricsDto;
import com.bioqualitybench.dto.Modality;
import com.bioqualitybench.exception.BiometricException;
import com.bioqualitybench.provider.FaceSDKProvider;
import com.bioqualitybench.provider.FingerprintSDKProvider;
import com.bioqualitybench.provider.IrisSDKProvider;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Routes biometric payloads to the modality-specific SDK, similar to MOSIP device-service provider wiring.
 */
@Component
public class BioAPIFactory {

    private final Map<Modality, IBioAPI> providersByModality;

    public BioAPIFactory(
            FingerprintSDKProvider fingerprintSDKProvider,
            FaceSDKProvider faceSDKProvider,
            IrisSDKProvider irisSDKProvider) {
        this.providersByModality = new EnumMap<>(Modality.class);
        this.providersByModality.put(Modality.FINGERPRINT, fingerprintSDKProvider);
        this.providersByModality.put(Modality.FACE, faceSDKProvider);
        this.providersByModality.put(Modality.IRIS, irisSDKProvider);
    }

    public IBioAPI resolve(BiometricsDto dto) throws BiometricException {
        if (dto == null || dto.getModality() == null) {
            throw new BiometricException("Biometrics payload or modality is null");
        }
        IBioAPI provider = providersByModality.get(dto.getModality());
        if (provider == null) {
            throw new BiometricException("No SDK provider registered for modality " + dto.getModality());
        }
        return provider;
    }
}
