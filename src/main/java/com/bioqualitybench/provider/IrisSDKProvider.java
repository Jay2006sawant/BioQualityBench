package com.bioqualitybench.provider;

import com.bioqualitybench.api.IBioAPI;
import com.bioqualitybench.dto.BiometricsDto;
import com.bioqualitybench.dto.Modality;
import com.bioqualitybench.exception.BiometricException;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class IrisSDKProvider implements IBioAPI {

    private static final double MIN_SCORE = 40.0;
    private static final double MAX_SCORE = 90.0;
    private static final int FAILURE_ROLL_BOUND = 10;

    @Override
    public double getSDKScore(BiometricsDto dto) throws BiometricException {
        if (dto.getModality() != Modality.IRIS) {
            throw new BiometricException("Iris provider invoked for modality " + dto.getModality());
        }
        maybeFail("iris SDK");
        return randomScore();
    }

    private static void maybeFail(String context) throws BiometricException {
        if (ThreadLocalRandom.current().nextInt(FAILURE_ROLL_BOUND) == 0) {
            throw new BiometricException("Simulated " + context + " failure (timeout or corrupt ISO template)");
        }
    }

    private static double randomScore() {
        return MIN_SCORE + ThreadLocalRandom.current().nextDouble() * (MAX_SCORE - MIN_SCORE);
    }
}
