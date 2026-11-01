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

Requires Java 21 and Jakarta Validation 3.x. Lombok-annotated classes are supported; Lombok itself is not required.

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
UserRequest valid = DataFactory.of(UserRequest.class).valid().get();
// → UserRequest[email=alice.johnson@example.com, age=34, name=Alice Smith]

// One invalid instance per constraint — each fails exactly one rule
List<UserRequest> bad = DataFactory.of(UserRequest.class).invalidList().getAll();
// → UserRequest[email=null, age=34, name=Alice Smith]        ← @NotNull on email
//   UserRequest[email=, age=34, name=Alice Smith]            ← @NotBlank on email
//   UserRequest[email=notanemail, age=34, name=Alice Smith]  ← @Email
//   UserRequest[email=x*101, age=34, name=Alice Smith]       ← @Size(max=100)
//   UserRequest[email=alice@..., age=null, name=Alice Smith] ← @NotNull on age
//   UserRequest[email=alice@..., age=17, name=Alice Smith]   ← @Min(18)
//   ... one object per constraint per field
```

---

## Features

- **Reads Jakarta Validation annotations** — `@NotNull`, `@NotBlank`, `@NotEmpty`, `@Size`, `@Min`/`@Max`,
  `@DecimalMin`/`@DecimalMax`, `@Digits`, `@Positive*`/`@Negative*`, `@Email`, `@Pattern`, `@Past*`/`@Future*`,
  `@AssertTrue`/`@AssertFalse`, `@Null`, `@Valid`, plus Hibernate's `@Length`, `@Range`, `@URL`,
  `@CreditCardNumber`, `@ISBN`, `@EAN`, `@UUID`. The full list is `DataFactory.supportedAnnotations()`.
- **Valid and invalid data** — one valid object, or one object per constraint that breaks exactly that rule.
- **Boundary values** — min, max, midpoint and a realistic value per field; collections at their `@Size` bounds.
- **Per-field generation** — `fields()` produces values for each field without building the object, so one
  unsupported field doesn't block the rest.
- **Never throws for object generation** — results carry `SUCCESS` / `PARTIAL` / `FAILED` and an `ErrorType`
  (`INVALID_CONFIGURATION`, `UNSUPPORTED_CLASS_TYPE`, `NOT_INSTANTIABLE`, `UNKNOWN_FIELD`, …).
- **Detects impossible constraints** — `@Min(5) @Max(3)`, `@Size(min = 5, max = 3)`, `@Positive @Negative`,
  `@Null` with `@NotNull`, `@AssertTrue` with `@AssertFalse` are reported, not generated.
- **Realistic values** — names, emails, cities, IBANs and more via DataFaker; field names act as hints.
- **Deterministic** — `seed(n)` gives the same output every run; seeded per field, so adding a field doesn't
  change the others.
- **Records, Lombok, all-args constructors and JavaBeans** — nested `@Valid` objects, `List`/`Set`/`Queue`/`Map`,
  arrays, `Optional`, enums and inherited fields.

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

When you have an annotated class. Object operations **never throw** — they return a
`DataFactoryResult` (one object) or `DataFactoryResults` (many). Call `get()` / `getAll()`
when you just want the value, or branch on the status:

```java
// One valid object
UserRequest req = DataFactory.of(UserRequest.class).valid().get();

// Override specific fields
UserRequest req = DataFactory.of(UserRequest.class)
                             .with("email", "fixed@company.com")
                             .with("age", 25)
                             .valid().get();

// Null a field
UserRequest req = DataFactory.of(UserRequest.class)
                             .withNull("address")
                             .valid().get();

// All constraint violations — one object per constraint
List<UserRequest> bad = DataFactory.of(UserRequest.class).invalidList().getAll();

// Violation for one specific field
UserRequest badEmail = DataFactory.of(UserRequest.class).invalidFor("email").get();

// Bulk — 1000 valid objects for load testing
List<UserRequest> bulk = DataFactory.of(UserRequest.class).validList(1000).getAll();

// Boundary variants per field — for parameterized tests
Map<String, DataFactoryResults<UserRequest>> edges = DataFactory.of(UserRequest.class).validPerField();
edges.get("age").getAll(); // → ages [18, 69, 120, 34]  (min, midpoint, max, semantic)

// Deterministic — same seed always produces the same values
UserRequest stable = DataFactory.of(UserRequest.class).seed(42L).valid().get();
```

#### Handling failures

```java
DataFactoryResult<UserRequest> r = DataFactory.of(UserRequest.class).valid();
if (r.isSuccess()) {
    use(r.get());
} else {
    switch (r.errorType()) {
        case INVALID_CONFIGURATION  -> fixAnnotations(r.reason()); // e.g. @Min(5) @Max(3)
        case UNSUPPORTED_CLASS_TYPE -> skip(r.reason());           // no generator for a field type
        case NOT_INSTANTIABLE       -> provideInstance();          // interface / abstract class
        default                     -> fail(r.reason());
    }
}
```

`DataFactoryResults` reports `SUCCESS`, `PARTIAL` or `FAILED` overall, with `succeeded()` / `failed()`.

#### Per field, without building the object

```java
Map<String, DataFactoryResult<FieldCases>> fields = DataFactory.of(UserRequest.class).fields();

FieldCases age = fields.get("age").get();
age.validValues();   // [18, 120, 69, 34]
age.invalidCases();  // [Min → 17, Max → 121]
```

Each field is generated on its own: a field DataFactory can't handle fails alone and the
other fields still succeed. Collections cover their `@Size` / `@NotEmpty` boundaries.

#### Discovery

```java
DataFactory.VERSION;                 // the artifact version, e.g. "0.2.0"
DataFactory.supportedTypes();        // scalar types with a generator
DataFactory.supportedAnnotations();  // "NotNull", "Size", "Email", …
DataFactory.canHandle(MyType.class);
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
        assertThat(validator.validate(DataFactory.valid(UserRequest.class).get())).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void each_constraint_is_enforced(UserRequest req) {
        assertThat(validator.validate(req)).isNotEmpty();
    }

    static List<UserRequest> invalidRequests() {
        return DataFactory.invalidList(UserRequest.class).getAll();
    }
}
```

### Integration test with realistic data

```java
@Test
void register_user_with_valid_data() {
    UserRequest req = DataFactory.of(UserRequest.class).valid().get();
    Response response = given().body(req).post("/api/users");
    assertThat(response.statusCode()).isEqualTo(201);
}

@Test
void register_user_with_invalid_email_returns_400() {
    UserRequest req = DataFactory.of(UserRequest.class).invalidFor("email").get();
    Response response = given().body(req).post("/api/users");
    assertThat(response.statusCode()).isEqualTo(400);
}
```

### Load test data

```java
@Test
void handles_concurrent_registrations() {
    List<UserRequest> requests = DataFactory.validList(UserRequest.class, 10_000).getAll();

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
                             .valid().get();
```

In DEV mode, unannotated numeric fields get the full type range in `validPerField()`:

```java
DataFactory.of(OrderRequest.class)
           .mode(GenerationMode.DEV)
           .validPerField()
           .get("quantity")
           .getAll();
// → [-2147483648, 0, 2147483647, 5]
```

---

## Limitations

- **Requires Java 21+** and Jakarta Validation 3.x (`jakarta.*`, not `javax.*`).
- **Custom constraints are not understood.** Annotations you define with `@Constraint` — and class-level or
  cross-field rules (e.g. "end date after start date") — are ignored; generated data may violate them.
- **Validation groups are ignored.** Every constraint is applied regardless of `groups`.
- **Element annotations are not read.** In `List<@NotBlank String>`, `@NotBlank` on the element has no effect.
- **`@Pattern` covers common shapes only** (character classes, fixed lengths, typical codes). For complex
  expressions DataFactory falls back to a plausible value and logs a warning — check the output.
- **Containers stay small.** Collections cover their `@Size` bounds up to 100 elements; arrays, maps and
  `Optional` get one representative value and arrays have no `@Size` boundary cases. An element type with no
  generator (e.g. `Map<String, Object>`) gives an empty container.
- **Nested objects are tested one level at a time.** `fields()` reports the nested field's own `@NotNull`
  case; the nested class's rules come from its own `fields()`.
- **Circular references** are broken with `null` (or an empty collection) at the point the cycle repeats.
- **Bounds at the type limit.** For `@Max(127) byte` there is no "one above max", so the extra invalid case
  wraps to the type's minimum — still invalid, but for the `@Min`/range reason.
- **Unannotated fields get type-default probes** (`null`, `""`, `-1`, `false`) marked
  `annotationDriven = false`. They are suggestions, not guaranteed violations.
- **Discovery lists are maintained by hand.** `supportedTypes()` / `supportedAnnotations()` describe what is
  implemented; `canHandle()` accepts any collection without checking its element type.
- **Interfaces and abstract classes** can't be instantiated — supply a concrete value with `with(...)`.

---

## Architecture

```
com.paramichha.datafactory                 ← public API: DataFactory, DataBuilder, results,
                                             FieldCases, ConstraintCase, ErrorType, field builders
com.paramichha.datafactory.builder         ← internals
com.paramichha.datafactory.constraint      ← internals
com.paramichha.datafactory.generation      ← internals
com.paramichha.datafactory.instantiation   ← internals
```

Only the root package is part of the public contract.

---

## License

Apache 2.0
