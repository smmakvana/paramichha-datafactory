# DataFactory

Test data generation for Java. Reads your Jakarta validation annotations and generates data that satisfies them — or violates each one individually.

No hardcoded strings. No helper methods. No maintenance.

---

## Install

```xml
<dependency>
    <groupId>com.paramichha</groupId>
    <artifactId>datafactory</artifactId>
    <version>0.2.0</version>
    <scope>test</scope>
</dependency>
```

Requires Java 21 and Jakarta Validation 3.x. Lombok is supported but not required.

---

## The idea

You annotate your request classes. DataFactory reads those annotations and generates values accordingly. When you add a new constraint, the generated data automatically reflects it on the next run.

```java
public record UserRequest(
    @NotBlank @Email @Size(max = 100) String email,
    @NotNull @Min(18) @Max(120)       Integer age,
    @NotBlank @Size(min = 2, max = 50) String name
) {}
```

```java
// One fully valid instance — real values, not "test1@test.com"
UserRequest valid = DataFactory.of(UserRequest.class).valid();
// → UserRequest[email=alice.johnson@example.com, age=34, name=Alice Smith]

// One invalid instance per constraint — each fails exactly one rule
List<UserRequest> bad = DataFactory.of(UserRequest.class).invalidList();
// → UserRequest[email=null, age=34, name=Alice Smith]        ← @NotNull on email
//   UserRequest[email=, age=34, name=Alice Smith]            ← @NotBlank on email
//   UserRequest[email=notanemail, age=34, name=Alice Smith]  ← @Email
//   UserRequest[email=x*101, age=34, name=Alice Smith]       ← @Size(max=100)
//   UserRequest[email=alice@..., age=null, name=Alice Smith] ← @NotNull on age
//   UserRequest[email=alice@..., age=17, name=Alice Smith]   ← @Min(18)
//   ... one object per constraint per field
```

---

## Three levels of API

### Level 1 — instant values

When you just need a realistic string:

```java
String email   = DataFactory.email();
String name    = DataFactory.name();
String phone   = DataFactory.phone();
String uuid    = DataFactory.uuid();
String city    = DataFactory.city();
String company = DataFactory.company();

List<String> emails = DataFactory.emails(50);
List<String> names  = DataFactory.names(100);
```

### Level 2 — typed builders

When you need values with specific constraints or format:

```java
// String
String email   = DataFactory.string().email().valid();
String iban    = DataFactory.string().iban().valid();
String card    = DataFactory.string().creditCard().valid();
String name    = DataFactory.string().name().valid();
String ip      = DataFactory.string().ipAddress().valid();
String custom  = DataFactory.string().hint("sortCode").valid();

// Numbers
int        age    = DataFactory.integer().range(18, 65).valid();
long       id     = DataFactory.longVal().positive().toLong();
BigDecimal price  = DataFactory.decimal().range(0.01, 999.99).valid();

// Dates
LocalDate     dob      = DataFactory.date().past().valid();
LocalDateTime deadline = DataFactory.dateTime().future().valid();

// Boolean
boolean active  = DataFactory.bool().assertTrue().valid();

// Enum
Status status = DataFactory.enumOf(Status.class);
List<Status> statuses = DataFactory.enumOf(Status.class, 10);

// Bulk
List<String>  emails = DataFactory.string().email().stream(100);
List<Integer> ages   = DataFactory.integer().range(18, 65).stream(1000);
```

### Level 3 — object builders

When you have an annotated class:

```java
// One valid object
UserRequest req = DataFactory.of(UserRequest.class).valid();

// Override specific fields
UserRequest req = DataFactory.of(UserRequest.class)
                             .with("email", "fixed@company.com")
                             .with("age", 25)
                             .valid();

// Null a field
UserRequest req = DataFactory.of(UserRequest.class)
                             .withNull("address")
                             .valid();

// All constraint violations
List<UserRequest> bad = DataFactory.of(UserRequest.class).invalidList();

// Violation for one specific field
UserRequest badEmail = DataFactory.of(UserRequest.class).invalidFor("email");

// Bulk — 1000 valid objects for load testing
List<UserRequest> bulk = DataFactory.of(UserRequest.class).stream(1000);

// Boundary variants per field — for parameterized tests
Map<String, List<UserRequest>> edges = DataFactory.of(UserRequest.class).validMap();
edges.get("age"); // → [18, 69, 120, 34]  (min, midpoint, max, semantic)

// Deterministic — same seed always produces the same values
UserRequest stable = DataFactory.of(UserRequest.class).seed(42L).valid();
```

---

## Use in tests

### Validation test

```java
class UserRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        try (ValidatorFactory f = Validation.buildDefaultValidatorFactory()) {
            validator = f.getValidator();
        }
    }

    @Test
    void valid_request_passes_validation() {
        assertThat(validator.validate(DataFactory.valid(UserRequest.class))).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void each_constraint_is_enforced(UserRequest req) {
        assertThat(validator.validate(req)).isNotEmpty();
    }

    static List<UserRequest> invalidRequests() {
        return DataFactory.invalidList(UserRequest.class);
    }
}
```

### Integration test with realistic data

```java
@Test
void register_user_with_valid_data() {
    UserRequest req = DataFactory.of(UserRequest.class).valid();
    Response response = given().body(req).post("/api/users");
    assertThat(response.statusCode()).isEqualTo(201);
}

@Test
void register_user_with_invalid_email_returns_400() {
    UserRequest req = DataFactory.of(UserRequest.class).invalidFor("email");
    Response response = given().body(req).post("/api/users");
    assertThat(response.statusCode()).isEqualTo(400);
}
```

### Load test data

```java
@Test
void handles_concurrent_registrations() {
    List<UserRequest> requests = DataFactory.stream(UserRequest.class, 10_000);

    List<CompletableFuture<Response>> futures = requests.stream()
            .map(req -> CompletableFuture.supplyAsync(() ->
                    given().body(req).post("/api/users")))
            .toList();

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    futures.forEach(f -> assertThat(f.join().statusCode()).isEqualTo(201));
}
```

---

## Supported classes

DataFactory can instantiate classes built with:

- **Records** — primary constructor
- **Lombok @Value / @Builder** — builder pattern
- **Lombok @Data / @AllArgsConstructor** — all-args constructor
- **JavaBeans** — default constructor + setters

Nested domain objects annotated with `@Valid` are built recursively.
`Optional<T>` and `List<T>` fields are populated automatically.

---

## Generation mode

DataFactory runs in `PRODUCTION` mode by default. It injects safe defaults for any field that lacks annotations: `@NotNull` on objects, `@Positive` on numbers, `@NotEmpty` on collections.

Switch to `DEV` mode when a class is still being annotated:

```java
// DEV: no null constraint injection — generates all technically-valid values
UserRequest req = DataFactory.of(UserRequest.class)
                             .mode(GenerationMode.DEV)
                             .valid();
```

In DEV mode, unannotated numeric fields get the full type range in `validList()`:

```java
DataFactory.of(OrderRequest.class)
           .mode(GenerationMode.DEV)
           .validMap()
           .get("quantity");
// → [-2147483648, 0, 2147483647, 5]
```

---

## Architecture

```
com.paramichha.datafactory         ← public API
com.paramichha.datafactory.constraint  ← internals, not part of the public contract
```

---

## License

Apache 2.0
