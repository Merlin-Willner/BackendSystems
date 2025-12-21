# mealplanner

Quarkus-based backend for the meal planner project. The repository is wired so you can build a runnable Docker image directly from Maven and execute tests before packaging.

## Prerequisites

- Java 21+
- Docker Engine running locally (required for image build/run)

## Run locally (dev mode)

```bash
./mvnw quarkus:dev
```

Dev UI: http://localhost:8080/q/dev

## Run tests

```bash
./mvnw clean verify
```

This executes unit and integration tests against the embedded H2 database.

## Build and run the Docker image via Maven

The Maven profile `docker-image` builds the JVM runner and container image (`mealplanner:latest`) in one step:

```bash
./mvnw clean verify -Pdocker-image
```

Run the resulting container:

```bash
docker run --rm -p 8080:8080 mealplanner:latest
```

H2 stores its files under `target/h2` inside the container (relative to `/deployments`). To keep data between runs, mount a volume:

```bash
docker run --rm -p 8080:8080 -v mealplanner-data:/deployments/target/h2 mealplanner:latest
```

## Manual Docker build (alternative)

```bash
./mvnw clean package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t mealplanner:latest .
docker run --rm -p 8080:8080 mealplanner:latest
```

The Dockerfiles under `src/main/docker` also include variants for native or legacy-jar packaging if needed.
