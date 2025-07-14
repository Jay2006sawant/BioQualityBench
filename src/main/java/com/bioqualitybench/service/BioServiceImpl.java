package com.bioqualitybench.service;

import com.bioqualitybench.api.IBioAPI;
import com.bioqualitybench.dto.BenchmarkResponse;
import com.bioqualitybench.dto.BiometricsDto;
import com.bioqualitybench.exception.BiometricException;
import com.bioqualitybench.factory.BioAPIFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BioServiceImpl {

    static final double DEFAULT_SBI_FALLBACK = 55.0;

    private final BioAPIFactory bioAPIFactory;

    public BenchmarkResponse benchmarkQuality(BiometricsDto dto) {
        long startNanos = System.nanoTime();

        IBioAPI provider = null;
        String providerName = "unresolved";
        try {
            provider = bioAPIFactory.resolve(dto);
            providerName = provider.getClass().getSimpleName();
            log.info(
                    "Resolved biometric provider for benchmarking modality={} provider={}",
                    dto.getModality(),
                    providerName);
        } catch (BiometricException e) {
            log.warn("Could not resolve SDK provider; using default SBI fallback. modality={} reason={}", dto.getModality(), e.getMessage());
            return buildFallbackResponse(dto, providerName, startNanos, e.getMessage());
        }

        Double sbiScore = dto.getQualityScore();
        double fallbackSbi = sbiScore != null ? sbiScore : DEFAULT_SBI_FALLBACK;
        if (sbiScore == null) {
            log.warn("Request missing SBI qualityScore; using default fallback value {} for audit trail", DEFAULT_SBI_FALLBACK);
        }

        double evaluated;
        try {
            evaluated = provider.getSDKScore(dto);
            log.info(
                    "SDK benchmark succeeded modality={} provider={} sdkScore={} sbiScore={}",
                    dto.getModality(),
                    providerName,
                    evaluated,
                    sbiScore);
        } catch (BiometricException ex) {
            log.warn(
                    "SDK evaluation failed; applying SBI quality fallback. modality={} provider={} reason={}",
                    dto.getModality(),
                    providerName,
                    ex.getMessage());
            evaluated = fallbackSbi;
        }

        long processingMs = elapsedMillis(startNanos);
        log.info(
                "Benchmark completed modality={} provider={} evaluatedScore={} processingTimeMs={}",
                dto.getModality(),
                providerName,
                evaluated,
                processingMs);

        return BenchmarkResponse.builder()
                .originalSbiScore(sbiScore)
                .evaluatedSdkScore(evaluated)
                .providerUsed(providerName)
                .processingTimeMs(processingMs)
                .build();
    }

    private BenchmarkResponse buildFallbackResponse(BiometricsDto dto, String providerName, long startNanos, String reason) {
        Double sbiScore = dto.getQualityScore();
        double evaluated = sbiScore != null ? sbiScore : DEFAULT_SBI_FALLBACK;
        long processingMs = elapsedMillis(startNanos);
        log.warn(
                "Benchmark short-circuited with SBI fallback modality={} providerAttempted={} evaluatedScore={} processingTimeMs={} detail={}",
                dto.getModality(),
                providerName,
                evaluated,
                processingMs,
                reason);
        return BenchmarkResponse.builder()
                .originalSbiScore(sbiScore)
                .evaluatedSdkScore(evaluated)
                .providerUsed(providerName)
                .processingTimeMs(processingMs)
                .build();
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
