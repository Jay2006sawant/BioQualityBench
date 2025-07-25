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

## Output when you run the project

### After `mvn test`

- Maven exits successfully (**BUILD SUCCESS**). You may see a JVM line such as `OpenJDK ... Sharing is only supported for boot loader classes` — that is a harmless warning.
- The console shows **SLF4J** lines from `BioServiceImpl`, for example:
  - `Resolved biometric provider for benchmarking modality=FINGERPRINT provider=FingerprintSDKProvider`
  - `SDK benchmark succeeded ... sdkScore=... sbiScore=...`
  - `SDK evaluation failed; applying SBI quality fallback ... reason=SDK timeout` (when the mock SDK simulates failure)
  - `Benchmark completed ... evaluatedScore=... processingTimeMs=...`

### After `java -jar target/bio-quality-bench-0.0.1-SNAPSHOT.jar`

- Spring Boot prints the **ASCII banner** and **`:: Spring Boot :: (v3.4.1)`**.
- Logs show **Tomcat started on port 8080** and **`Started BioQualityBenchApplication`**.
- If **port 8080 is already in use** (e.g. you started the JAR twice), startup fails with *Web server failed to start. Port 8080 was already in use* — stop the other process or change the port in `application.properties`.

### HTTP response from `POST /api/v1/biometrics/benchmark`

The body is always JSON with these fields:

| Field | Meaning |
|--------|--------|
| `originalSbiScore` | SBI / request `qualityScore` (may be `null` if omitted) |
| `evaluatedSdkScore` | Score from the mock SDK, **or** the SBI fallback if the SDK failed |
| `providerUsed` | Simple class name of the provider (e.g. `FingerprintSDKProvider`) |
| `processingTimeMs` | Time taken for this benchmark call |

**Example — fingerprint, SDK succeeded** (mock score is random between 40 and 90):

```json
{
  "originalSbiScore": 70.0,
  "evaluatedSdkScore": 71.33392846380087,
  "providerUsed": "FingerprintSDKProvider",
  "processingTimeMs": 1
}
```

**Example — face, mock SDK failed** (score falls back to SBI):

```json
{
  "originalSbiScore": 65.0,
  "evaluatedSdkScore": 65.0,
  "providerUsed": "FaceSDKProvider",
  "processingTimeMs": 1
}
```

Your exact `evaluatedSdkScore` will differ on each call when the SDK path succeeds, because the mock providers use random scores and occasional failures.

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
