package com.bioqualitybench.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bioqualitybench.dto.BenchmarkResponse;
import com.bioqualitybench.dto.BiometricsDto;
import com.bioqualitybench.dto.Modality;
import com.bioqualitybench.exception.BiometricException;
import com.bioqualitybench.factory.BioAPIFactory;
import com.bioqualitybench.provider.FaceSDKProvider;
import com.bioqualitybench.provider.FingerprintSDKProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BioServiceImplTest {

    @Mock
    private BioAPIFactory bioAPIFactory;

    private BioServiceImpl bioService;

    private BiometricsDto fingerprintDto;

    @BeforeEach
    void setUp() {
        bioService = new BioServiceImpl(bioAPIFactory);
        fingerprintDto = BiometricsDto.builder()
                .qualityScore(72.0)
                .sdkScore(null)
                .biometricData("QkFBQUFB")
                .modality(Modality.FINGERPRINT)
                .build();
    }

    @Test
    void benchmarkQuality_sdkSuccess_returnsSdkScoreAndProvider() throws Exception {
        FingerprintSDKProvider provider = spy(new FingerprintSDKProvider());
        doReturn(84.5).when(provider).getSDKScore(any(BiometricsDto.class));
        when(bioAPIFactory.resolve(fingerprintDto)).thenReturn(provider);

        BenchmarkResponse response = bioService.benchmarkQuality(fingerprintDto);

        assertThat(response.getOriginalSbiScore()).isEqualTo(72.0);
        assertThat(response.getEvaluatedSdkScore()).isEqualTo(84.5);
        assertThat(response.getProviderUsed()).isEqualTo("FingerprintSDKProvider");
        assertThat(response.getProcessingTimeMs()).isGreaterThanOrEqualTo(0L);
        verify(bioAPIFactory).resolve(fingerprintDto);
        verify(provider).getSDKScore(fingerprintDto);
    }

    @Test
    void benchmarkQuality_sdkFailure_fallsBackToSbiScore() throws Exception {
        FingerprintSDKProvider provider = spy(new FingerprintSDKProvider());
        doThrow(new BiometricException("SDK timeout")).when(provider).getSDKScore(any(BiometricsDto.class));
        when(bioAPIFactory.resolve(fingerprintDto)).thenReturn(provider);

        BenchmarkResponse response = bioService.benchmarkQuality(fingerprintDto);

        assertThat(response.getEvaluatedSdkScore()).isEqualTo(72.0);
        assertThat(response.getProviderUsed()).isEqualTo("FingerprintSDKProvider");
    }

    @Test
    void benchmarkQuality_missingSbiScore_usesDefaultFallbackOnSdkFailure() throws Exception {
        fingerprintDto.setQualityScore(null);
        FingerprintSDKProvider provider = spy(new FingerprintSDKProvider());
        doThrow(new BiometricException("SDK failure")).when(provider).getSDKScore(any(BiometricsDto.class));
        when(bioAPIFactory.resolve(fingerprintDto)).thenReturn(provider);

        BenchmarkResponse response = bioService.benchmarkQuality(fingerprintDto);

        assertThat(response.getEvaluatedSdkScore()).isEqualTo(BioServiceImpl.DEFAULT_SBI_FALLBACK);
    }

    @Test
    void benchmarkQuality_routesThroughFactoryForFaceModality() throws Exception {
        FaceSDKProvider provider = spy(new FaceSDKProvider());
        doReturn(77.0).when(provider).getSDKScore(any(BiometricsDto.class));
        BiometricsDto faceDto = BiometricsDto.builder()
                .qualityScore(80.0)
                .biometricData("FACEISO")
                .modality(Modality.FACE)
                .build();
        when(bioAPIFactory.resolve(faceDto)).thenReturn(provider);

        BenchmarkResponse response = bioService.benchmarkQuality(faceDto);

        assertThat(response.getEvaluatedSdkScore()).isEqualTo(77.0);
        assertThat(response.getProviderUsed()).isEqualTo("FaceSDKProvider");
        verify(bioAPIFactory).resolve(faceDto);
        verify(provider).getSDKScore(faceDto);
    }
}
