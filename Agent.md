# Backend Systems Portfolio – Findings for MealPlanner

## Core Technology Stack
- **Languages**: Java or Kotlin only (Portfolio 03). Favor Java 17+ aligned with Quarkus defaults.
- **Framework**: Quarkus with RESTEasy for JAX‑RS (Spring explicitly disallowed).
- **Persistence**: Jakarta Persistence (JPA) with a provider such as Hibernate; model at least one 1‑to‑n relationship; ensure proper entity mappings and cascading (Unit 08, Portfolio 03).
- **Build Tooling**: Maven (preferred) or Gradle; know the Maven lifecycle (`test`, `verify`) and dependency management (Unit 07).
- **Database**: Use JPA-compatible RDBMS. H2 acceptable for dev/test; production-ready setup should target PostgreSQL or similar and expose Dockerised runtime (Portfolio 03, Unit 08).
- **Containerisation**: Provide a Docker-based startup path; ideally wire into `mvn verify` (Portfolio 03).

## Architectural Expectations
- **Hexagonal architecture** (Ports & Adapters) is mandatory (Units 02/08, Portfolio 03).
  - Separate domain, application/service, API (REST), and persistence components.
  - Communicate via interfaces; do not share implementation classes across components.
  - Start with a single use case, get lecturer approval before expanding (Portfolio 03).
- **Monolithic deployment** but architected cleanly; Microservices explicitly not required for WS25 (Unit 02).
- **Stateless API layer** to simplify scaling and enable load balancing (Unit 09).
- Align with CAP insights: design for consistency/availability trade-offs; MealPlanner likely targets CA with relational DB (Unit 03).

## API & Quality Requirements
- **RESTful API** with CRUD endpoints for core resources (FoodItem, Dish, etc.).
- **Filtering & Paging** via query parameters on collection endpoints (`page`, `size`, filter fields) (Portfolio 03).
- **Sorting**: multi-key query parameter (e.g. `sort=pricePerProtein:asc,name:asc`); ensure deterministic ordering on pagination.
- **Caching**: Implement and document strategy (`ETag`, `Cache-Control`, or server-side caching) (Portfolio 03).
- **Hypermedia/HATEOAS**: Include hyperlinks for next actions (response body or `Link` headers).
- **Error Handling**: Map validation, conflict, and not-found cases to standard HTTP status codes.
- **Authentication/Authorization**: Implement if required by domain risk; address in architecture section even if deferred (Portfolio 03).

## Persistence Layer Notes
- Leverage JPA annotations for entities and relationships; enforce validation and cascades (Unit 08).
- Decide on identifier strategy (e.g. sequence-backed `Long` or UUID/ULID) and document justification in domain model.
- Provide DAO/Repository abstractions that the domain consumes via ports.
- Cover data integrity with migration/initialisation strategy (Flyway/Liquibase optional but beneficial).

## Testing Expectations
- **Unit tests**: Cover domain services/aggregations (Portfolio 03, Unit 08).
- **Persistence tests**: Validate JPA mappings with in-memory database (Unit 08).
- **Integration tests**: Exercise REST endpoints, filtering, paging, hypermedia, and database interaction (Portfolio 03).
- Automate via Maven (`mvn test`, `mvn verify`) and include in CI pipeline.

## Deliverables Timeline
- **Portfolio 01 (Idea approval)**: 2–3 page PDF (LaTeX) with title, authors + matriculation numbers, sections:
  1. Project Overview (elevator pitch, scope, non-goals).
  2. Domain Model (UML, entity descriptions, invariants).
  3. Use Cases (4–6 detailed UC specs with priorities, flows, acceptance criteria).  
  Deadline: 31 Oct 2025 (Portfolio 01).
- **Portfolio 03 (Implementation)**:
  - Git repository with full source, tests, Docker support, README instructions.
  - 5 min screencast (MP4 ≤ 20 MB, ≥ 720p) demonstrating architecture/tests.
  - Plain text file for Moodle submission (line 1 repository URL, line 2 video URL, line 3 comma-separated matriculation numbers).
- **Portfolio 04 (Final documentation)**:
  - 4–6 page LaTeX report in English; include all authors + matriculation numbers.
  - Sections: Project Topic & Use Cases, Architecture (hexagonal interpretation), API Technology & REST, Implementation details (auth, adapters, libraries), Testing strategy, Learning outcomes.
  - Include prescribed AI-assistance acknowledgement paragraph.
  - Evaluation rubric awards up to 18+6 bonus points; focus on clarity, justification, and reflection.

## Additional Guidance from Scripts
- **Unit 01**: Solidify client-server fundamentals, synchronous vs. asynchronous communication, failure semantics (at-most-once delivery).
- **Unit 02**: Understand architectural trade-offs; hexagonal monolith chosen for course.
- **Unit 03**: CAP theorem implications for design decisions (consistency vs availability vs partition tolerance).
- **Unit 07**: Master Maven CLI usage, lifecycle phases, dependency scopes, and local cache management.
- **Unit 08**: Implement persistence via JPA/Hibernate; leverage DAOs, entity relationships, cascading, and testing.
- **Unit 09**: Plan for scalability (stateless services, database replication/sharding concepts); design with future load balancing in mind.

## Action Items for MealPlanner
- Finalise domain model (entities, relationships, invariants) adhering to hex architecture boundaries.
- Specify API contract including filtering/paging/sorting, hypermedia links, caching headers, and relationship endpoints.
- Choose identifier strategy and document rationale in LaTeX deliverables.
- Ensure build pipeline (`mvn verify`) spins up tests and Docker image; document steps in README.
- Align documentation (Portfolio 01/04) with rubric requirements and include AI usage statement.
