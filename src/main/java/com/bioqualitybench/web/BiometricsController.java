package com.bioqualitybench.web;

import com.bioqualitybench.dto.BenchmarkResponse;
import com.bioqualitybench.dto.BiometricsDto;
import com.bioqualitybench.service.BioServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/biometrics")
@RequiredArgsConstructor
public class BiometricsController {

    private final BioServiceImpl bioService;

    @PostMapping("/benchmark")
    public ResponseEntity<BenchmarkResponse> benchmark(@Valid @RequestBody BiometricsDto dto) {
        return ResponseEntity.ok(bioService.benchmarkQuality(dto));
    }
}
