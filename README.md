# BioQualityBench

**BioQualityBench** is a small **Spring Boot** service inspired by patterns from **MOSIP** (Modular Open Source Identity Platform) biometric device / bio-service integration: you send **ISO-style biometric payloads** (here modeled as a **Base64** string), the app **routes** them to a **modality-specific SDK provider**, and it **benchmarks** how an SDK quality score compares to the **SBI (capture) quality score**, with **fallback** when the SDK fails.

## What this project does

1. **Accepts** fingerprint, face, or iris samples as JSON (`BiometricsDto`: modality, Base64 `biometricData`, optional SBI `qualityScore`).
2. **Resolves** the correct `IBioAPI` implementation via `BioAPIFactory` (like pluggable bio providers in MOSIP).
3. **Calls** the mock SDK (`getSDKScore`) which returns a random score between **40–90** and sometimes throws `BiometricException` to simulate timeouts or bad samples.
4. **Falls back** to the SBI score (or a default **55.0** if SBI is missing) when the SDK fails, with **SLF4J** warnings for audit-style tracing.
5. **Exposes** `POST /api/v1/biometrics/benchmark` returning original SBI score, evaluated score, provider class name, and processing time.

## Tech stack

- Java **17**
- Spring Boot **3.4.x**
- Maven
- Jakarta Validation, Lombok
- JUnit **5** and **Mockito** (unit tests)

## Build and test

```bash
mvn test
mvn package -DskipTests
```

## Run

```bash
java -jar target/bio-quality-bench-0.0.1-SNAPSHOT.jar
```

Default port: **8080** (see `src/main/resources/application.properties`).

## API example

```bash
curl -s -X POST http://localhost:8080/api/v1/biometrics/benchmark \
  -H "Content-Type: application/json" \
  -d '{
    "qualityScore": 70,
    "biometricData": "QkFBQUFB",
    "modality": "FINGERPRINT"
  }'
```

Example response:

```json
{
  "originalSbiScore": 70.0,
  "evaluatedSdkScore": 82.5,
  "providerUsed": "FingerprintSDKProvider",
  "processingTimeMs": 1
}
```

`modality` must be one of: `FINGERPRINT`, `FACE`, `IRIS`.

## Package layout

| Area | Contents |
|------|----------|
| `api` | `IBioAPI` — SDK contract (`getSDKScore`) |
| `dto` | `BiometricsDto`, `BenchmarkResponse`, `Modality` |
| `exception` | `BiometricException` |
| `factory` | `BioAPIFactory` — modality → provider |
| `provider` | `FingerprintSDKProvider`, `FaceSDKProvider`, `IrisSDKProvider` (mock SDKs) |
| `service` | `BioServiceImpl` — benchmark + fallback + logging |
| `web` | `BiometricsController` — REST endpoint |

## Tests

`BioServiceImplTest` covers successful SDK scoring, SDK failure fallback, missing SBI fallback, and routing to the face provider.

## Disclaimer

This is a **learning / benchmark harness** with **mock** SDKs, not a certified biometric product or a full MOSIP deployment.
