# MealPlanner Backend

Quarkus-based backend for the meal planner project. The repository is wired so you can build a runnable Docker image directly from Maven, and run tests as part of the Maven lifecycle.

## Prerequisites

- Java 21+
- Docker Engine running locally (required for image build/run)

## Development Mode
Run the application in development mode with live coding:

```bash
./mvnw quarkus:dev
```

Dev UI: http://localhost:8080/q/dev

## Unit Testing
- When Quarkus is running in dev mode, unit tests can be executed by pressing `r` in the console.
- All unit tests run with:

```bash
./mvnw test
```

## Integration Testing
Integration tests are marked with the `*IT.java` suffix (e.g., `FoodItemResourceIT.java`).

Run integration tests with:

```bash
./mvnw verify
```

They execute against the embedded H2 database and the Quarkus test runtime.

Run verify without building the Docker image (recommended if Docker is not running):

```bash
./mvnw clean verify -Dquarkus.container-image.build=false
```

## Packaging and Running the Application

Package the application:

```bash
./mvnw package
```

This produces `target/quarkus-app/quarkus-run.jar` plus dependencies under `target/quarkus-app/lib/`.

Run the packaged app:
```bash
java -jar target/quarkus-app/quarkus-run.jar
```
## Docker

### Build via Maven (recommended)

The Maven profile `docker-image` builds a container image (`local/mealplanner:latest`). It is enabled by default, so this is enough:

```bash
./mvnw clean verify
```

### Manual Docker build (alternative)

```bash
./mvnw clean package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t local/mealplanner:latest .
```

Run the container:

```bash
docker run --rm -p 8080:8080 local/mealplanner:latest
```

## Authentication (JWT)
- Login: `POST /auth/login` with JSON `{ "username": "alice", "password": "alice-secret" }`
- Registration: `POST /auth/registration` with JSON `{ "username": "alice", "email": "alice@example.com", "password": "alice-secret" }`
- Use the token for protected endpoints: `Authorization: Bearer <token>`
