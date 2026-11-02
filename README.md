# Spring Boot Development Guidelines

**Reference Service**: Spotlight Service  
**Last Updated**: November 2025  
**Purpose**: Quick reference for building Spring Boot microservices

This repository is a ready-to-clone skeleton that follows the patterns below.

---

## Quick Start (this repo)

```bash
# Requires Java 21+ and Maven 3.9.6+
export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
# Prefer Homebrew OpenJDK 21 if available:
export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21}"

cd spotlight-service
mvn clean test
mvn spring-boot:run
```

- API sample: `POST /api/spotlights/{ownerId}`
- Health: `GET /actuator/health`
- Metrics: `GET /actuator/prometheus`

Package root: `com.example.spotlight` (rename to your org when cloning).

---

## Quick Start Checklist

### Project Setup
- [ ] Java 21+ installed
- [ ] Spring Boot 3.3.4+ starter dependencies added
- [ ] Maven 3.9.6+ configured
- [ ] Follow standard package structure (see below)
- [ ] Configure multiple datasources (if needed)

### Required Dependencies
- [ ] spring-boot-starter-web - REST APIs
- [ ] spring-boot-starter-data-jpa - Database access
- [ ] spring-boot-starter-actuator - Monitoring
- [ ] micrometer-registry-prometheus - Metrics
- [ ] spring-boot-starter-aop - Cross-cutting concerns
- [ ] lombok - Reduce boilerplate
- [ ] spring-boot-starter-test - Testing

### Architecture Checklist
- [ ] Controllers handle HTTP only (no business logic)
- [ ] Application services orchestrate use cases
- [ ] Domain services contain business rules
- [ ] Persistence services handle data access
- [ ] Repositories for JPA operations only
- [ ] DTOs for API requests/responses
- [ ] Entities for database mapping

### Code Quality
- [ ] Constructor injection (no @Autowired on fields)
- [ ] Use @Valid for request validation
- [ ] Custom exceptions with @ControllerAdvice handler
- [ ] Meaningful error messages with error codes
- [ ] Proper logging (INFO for success, ERROR for failures)

### Testing Requirements
- [ ] Unit tests for services (80%+ coverage)
- [ ] Integration tests for APIs
- [ ] H2 in-memory DB for tests
- [ ] Test data in src/test/resources
- [ ] Mock external dependencies

### Observability
- [ ] @Observed annotation on key methods
- [ ] Custom metrics for business events
- [ ] Request/Response logging filter
- [ ] Actuator endpoints enabled (/health, /metrics, /prometheus)
- [ ] Meaningful log messages with context

### Before Deployment
- [ ] All tests passing
- [ ] No hardcoded values (use properties)
- [ ] Environment-specific configs (qa, prod)
- [ ] Docker image builds successfully
- [ ] Health check endpoint works
- [ ] Metrics endpoint accessible

---

## Table of Contents

1. [Project Structure](#1-project-structure)
2. [Architecture Layers](#2-architecture-layers)
3. [Code Patterns](#3-code-patterns)
4. [Configuration](#4-configuration)
5. [Database](#5-database)
6. [API Design](#6-api-design)
7. [Testing](#7-testing)
8. [Observability](#8-observability)

---

## 1. Project Structure

### Standard Package Layout

```
com.example.<domain>.<service>/
├── <Service>Application.java       # Main class
├── config/                          # All configurations
├── controller/                      # REST endpoints
├── service/
│   ├── application/                # Orchestration layer
│   ├── domain/                     # Business logic
│   ├── persistence/                # Data access
│   └── external/                   # External APIs
├── repository/                      # JPA repositories
├── domain/                          # Entities (primary DB)
├── dto/                            # Request/Response objects
├── mapper/                         # Entity ↔ DTO conversion
├── exceptions/                     # Custom exceptions
└── metrics/                        # Custom metrics
```

### Resource Files

```
src/main/resources/
├── application.properties          # Default config
├── application-qa.properties       # QA overrides
└── application-prod.properties     # Prod overrides

src/test/resources/
├── application-test.properties
├── h2-init.sql                     # Test data
└── expected-responses/             # Test fixtures
```

**Key Points:**
- One package per responsibility
- Separate DB entities by datasource
- Keep resources organized by environment

---

## 2. Architecture Layers

### Responsibilities

| Layer | What It Does | What It Doesn't Do |
|-------|--------------|-------------------|
| *Controller* | Handle HTTP, validate input, return responses | Business logic, data access |
| *Application Service* | Orchestrate use cases, manage transactions | Direct database access |
| *Domain Service* | Core business rules and logic | Know about HTTP or databases |
| *Persistence Service* | Abstract data access, build queries | Business decisions |
| *Repository* | JPA operations, custom queries | Any business logic |

### Quick Rules
- Controllers delegate to application services
- Application services call domain services
- Domain services don't know about databases
- Only persistence services call repositories
- Each layer only talks to the layer below

---

## 3. Code Patterns

### Controller Pattern

```java
@RestController
@RequestMapping("/api/spotlights")
public class SpotlightController {

    private final ApplySpotlightService service;

    public SpotlightController(ApplySpotlightService service) {
        this.service = service;
    }

    @Observed(name = "apply.spotlights")
    @PostMapping("/{ownerId}")
    public ResponseEntity<SpotlightResponse> apply(
            @PathVariable Long ownerId,
            @RequestBody @Valid SpotlightRequest request) {

        var response = service.applySpotlight(request, ownerId);
        return ResponseEntity.ok(response);
    }
}
```

**Controller Checklist:**
- [ ] Use @RestController
- [ ] Constructor injection (no field injection)
- [ ] @Valid on request bodies
- [ ] @Observed for metrics
- [ ] No business logic
- [ ] Return ResponseEntity<?>

### Service Pattern

```java
@Service
@Transactional
public class ApplySpotlightService {

    private final SpotlightDomainService domainService;
    private final SpotlightPersistenceService persistenceService;
    private final SpotlightMapper mapper;

    public SpotlightResponse applySpotlight(SpotlightRequest request, Long ownerId) {
        // 1. Business logic
        var spotlight = domainService.calculateSpotlight(ownerId, request);

        // 2. Save
        var saved = persistenceService.save(spotlight);

        // 3. Map and return
        return mapper.toResponse(saved);
    }
}
```

**Service Checklist:**
- [ ] Use @Service or @Component
- [ ] Add @Transactional if modifying data
- [ ] Keep methods focused (single responsibility)
- [ ] Handle exceptions with custom types
- [ ] Log important operations

### DTO Pattern

```java
@Data
@Builder
public class SpotlightRequest {

    @NotNull(message = "Resource code is required")
    private String resourceCode;

    @NotEmpty(message = "At least one highlight required")
    private List<String> highlights;

    @Min(value = 1, message = "Position must be positive")
    private Integer position;
}
```

**DTO Checklist:**
- [ ] Use Lombok (@Data, @Builder)
- [ ] Add validation annotations
- [ ] Include meaningful error messages
- [ ] Keep DTOs simple (no logic)
- [ ] Separate request and response DTOs

### Entity Pattern

```java
@Entity
@Table(name = "SPOTLIGHT_LOG")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotlightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "RESOURCE_CODE", nullable = false, length = 50)
    private String resourceCode;

    @Column(name = "APPLIED_DATE")
    private LocalDateTime appliedDate;

    @PrePersist
    protected void onCreate() {
        appliedDate = LocalDateTime.now();
    }
}
```

**Entity Checklist:**
- [ ] Use @Entity and @Table
- [ ] Explicit column names match database
- [ ] Use sequences for Oracle IDs (IDENTITY ok for H2 local)
- [ ] Add @PrePersist / @PreUpdate for timestamps
- [ ] No business logic in entities

### Exception Handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        var error = ErrorResponse.builder()
            .code("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        var error = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

**Exception Handling Checklist:**
- [ ] Use @ControllerAdvice
- [ ] Create custom exception types
- [ ] Include error codes
- [ ] Log errors appropriately
- [ ] Don't expose stack traces to clients

---

## 4. Configuration

### Application Properties Template

```properties
# Application
spring.application.name=spotlight-service
server.port=8080

# Primary Database
spring.datasource.url=jdbc:oracle:thin:@//host:1521/service
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.jpa.hibernate.ddl-auto=none

# Secondary Database - if needed
spring.datasource.secondary.url=jdbc:oracle:thin:@//host:1521/secondary
spring.datasource.secondary.username=${SECONDARY_USERNAME}
spring.datasource.secondary.password=${SECONDARY_PASSWORD}

# Actuator
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=always

# Logging
logging.level.com.example=INFO
logging.level.org.springframework.web=DEBUG
```

**Configuration Checklist:**
- [ ] Use environment variables for secrets
- [ ] Separate configs per environment
- [ ] Enable actuator endpoints
- [ ] Set appropriate log levels
- [ ] Never commit passwords

### Multi-Database Setup

```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.service.secondaryrepository",
    entityManagerFactoryRef = "secondaryEntityManager",
    transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryDatabaseConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean secondaryEntityManager(
            @Qualifier("secondaryDataSource") DataSource dataSource) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.service.secondarydomain");
        em.setPersistenceUnitName("secondary");
        return em;
    }

    @Bean
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

---

## 5. Database

### Entity Best Practices
- Use explicit @Table and @Column names
- Match database naming exactly (Oracle uppercase)
- Use sequences for IDs: @SequenceGenerator
- Add @PrePersist for created dates
- Add @PreUpdate for modified dates

### Repository Best Practices

```java
public interface SpotlightLogRepository extends JpaRepository<SpotlightLog, Long> {

    Optional<SpotlightLog> findByResourceCode(String resourceCode);

    @Query("SELECT s FROM SpotlightLog s WHERE s.ownerId = :ownerId ORDER BY s.appliedDate DESC")
    List<SpotlightLog> findByOwnerId(@Param("ownerId") Long ownerId);
}
```

**Repository Checklist:**
- [ ] Extend JpaRepository&lt;Entity, ID&gt;
- [ ] Use method naming for simple queries
- [ ] Use @Query for complex queries
- [ ] Always use @Param annotations
- [ ] Return Optional&lt;&gt; for single results

---

## 6. API Design

- Prefer resource-oriented paths: `/api/spotlights/{ownerId}`
- Use DTOs only at the API boundary (never expose entities)
- Validate with Jakarta Validation + `@Valid`
- Return consistent error payloads via `@ControllerAdvice`

---

## 7. Testing

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SpotlightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldApplySpotlight() throws Exception {
        var request = """
            {
              "resourceCode": "RES-001",
              "highlights": ["Premium", "Featured"],
              "position": 1
            }
            """;

        mockMvc.perform(post("/api/spotlights/100")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resourceCode").value("RES-001"));
    }
}
```

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class ApplySpotlightServiceTest {

    @Mock
    private SpotlightPersistenceService persistenceService;

    @InjectMocks
    private ApplySpotlightService service;

    @Test
    void shouldApplySpotlight() {
        // Given / When / Then — mock collaborators, assert outcome
    }
}
```

**Testing Checklist:**
- [ ] Unit tests for each service method
- [ ] Integration tests for each API endpoint
- [ ] Use H2 for test database
- [ ] Mock external dependencies
- [ ] Test happy path and error cases
- [ ] Aim for 80%+ code coverage

---

## 8. Observability

### Metrics

```java
@Component
public class SpotlightMetrics {

    private final Counter appliedCounter;

    public SpotlightMetrics(MeterRegistry registry) {
        this.appliedCounter = Counter.builder("spotlight.applied")
            .description("Number of spotlights applied")
            .tag("service", "spotlight-service")
            .register(registry);
    }

    public void recordApplied() {
        appliedCounter.increment();
    }
}
```

### Logging

```java
private static final Logger log = LoggerFactory.getLogger(ApplySpotlightService.class);

public void applySpotlight(String resourceCode) {
    log.info("Applying spotlight for resourceCode: {}", resourceCode);
    try {
        // Logic
        log.info("Successfully applied spotlight for resourceCode: {}", resourceCode);
    } catch (Exception e) {
        log.error("Failed to apply spotlight for resourceCode: {}", resourceCode, e);
        throw e;
    }
}
```

**Observability Checklist:**
- [ ] Add @Observed to key operations
- [ ] Create custom metrics for business events
- [ ] Log at appropriate levels (DEBUG, INFO, ERROR)
- [ ] Include context in log messages
- [ ] Enable actuator endpoints
- [ ] Configure Prometheus registry

---

## Quick Reference

### Do's
- Constructor injection
- Validate inputs with @Valid
- Use DTOs for API layer
- Handle exceptions globally
- Log important events
- Write tests
- Use transactions for writes
- Environment-specific configs

### Don'ts
- Field injection (@Autowired on fields)
- Business logic in controllers
- Hardcoded values
- Exposing entities via API
- Ignoring exceptions
- Skipping tests
- Committing secrets
- Using SELECT * queries

---

## Docker

```bash
mvn -DskipTests package
docker build -t spotlight-service:local .
docker run --rm -p 8080:8080 spotlight-service:local
```

---

## Need More Details?

Start from this skeleton, rename `com.example.spotlight` to your organization package, and iterate.

**Remember**: Start simple, follow the patterns, and iterate!
