# API Endpoint Implementation Plan: Templates

## 1. Przegląd punktu końcowego

Grupa punktów końcowych dla zarządzania szablonami checklisty onboardingowej. Szablony są związane z pozycjami stanowiskowymi (np. "Software Engineer") i zawierają zestaw zadań, które są kopiowane do procesu onboardingowego podczas tworzenia nowego pracownika.

**Kluczowe założenia projektowe:**
- Każdy szablon jest powiązany z unikalną nazwą stanowiska (positionName)
- Nazwa stanowiska jest normalizowana (trim + toLowerCase) dla spójności
- Unikalność jest wymuszana poprzez funkcjonalny indeks w bazie danych (case-insensitive)
- Szablony używają denormalizacji wersjonowania - zadania są KOPIOWANE do onboarding_task, więc zmiany w szablonie nie wpływają na aktywne procesy
- Usunięcie szablonu wymaga uprzedniego usunięcia wszystkich powiązanych zadań (ON DELETE RESTRICT)

**Punkty końcowe do implementacji:**
- `GET /templates` - Lista wszystkich szablonów z paginacją i filtrowaniem
- `GET /templates/{templateId}` - Pobranie szczegółów pojedynczego szablonu
- `POST /templates` - Utworzenie nowego szablonu
- `PUT /templates/{templateId}` - Aktualizacja istniejącego szablonu
- `DELETE /templates/{templateId}` - Usunięcie szablonu

---

## 2. Szczegóły żądania

### GET /templates

**Metoda HTTP:** GET

**Struktura URL:** `/templates`

**Parametry:**
- **Opcjonalne parametry zapytania:**
  - `page` (Integer): Numer strony (domyślnie 0)
  - `size` (Integer): Rozmiar strony (domyślnie 20, max 100)
  - `sort` (String): Pole sortowania z kierunkiem (np. `positionName,asc` lub `createdAt,desc`)
  - `positionName` (String): Filtrowanie po nazwie stanowiska (case-insensitive, partial match)

**Request Body:** Brak

**Przykładowe wywołanie:**
```
GET /templates?page=0&size=20&sort=positionName,asc
GET /templates?positionName=engineer
```

---

### GET /templates/{templateId}

**Metoda HTTP:** GET

**Struktura URL:** `/templates/{templateId}`

**Parametry:**
- **Wymagane parametry ścieżki:**
  - `templateId` (UUID): Identyfikator szablonu

**Request Body:** Brak

**Przykładowe wywołanie:**
```
GET /templates/123e4567-e89b-12d3-a456-426614174000
```

---

### POST /templates

**Metoda HTTP:** POST

**Struktura URL:** `/templates`

**Parametry:** Brak parametrów ścieżki ani zapytania

**Request Body:**
```json
{
  "positionName": "Software Engineer"
}
```

**Walidacja:**
- `positionName`: @NotBlank, @Size(max=50)

**Przykładowe wywołanie:**
```
POST /templates
Content-Type: application/json

{
  "positionName": "Software Engineer"
}
```

---

### PUT /templates/{templateId}

**Metoda HTTP:** PUT

**Struktura URL:** `/templates/{templateId}`

**Parametry:**
- **Wymagane parametry ścieżki:**
  - `templateId` (UUID): Identyfikator szablonu do aktualizacji

**Request Body:**
```json
{
  "positionName": "Senior Software Engineer"
}
```

**Walidacja:**
- `positionName`: @Size(max=50) (opcjonalne)

**Uwaga:** Wszystkie pola w request body są opcjonalne. Aktualizowane są tylko dostarczone wartości (partial update).

**Przykładowe wywołanie:**
```
PUT /templates/123e4567-e89b-12d3-a456-426614174000
Content-Type: application/json

{
  "positionName": "Senior Software Engineer"
}
```

---

### DELETE /templates/{templateId}

**Metoda HTTP:** DELETE

**Struktura URL:** `/templates/{templateId}`

**Parametry:**
- **Wymagane parametry ścieżki:**
  - `templateId` (UUID): Identyfikator szablonu do usunięcia

**Request Body:** Brak

**Przykładowe wywołanie:**
```
DELETE /templates/123e4567-e89b-12d3-a456-426614174000
```

---

## 3. Wykorzystywane typy

### Encje (Entity)

**Template** (`/models/Template.java`) - **ISTNIEJE**
```java
@Entity
@Table(name = "template", schema = "benew")
public class Template {
    private UUID id;
    private String positionName;  // NOT NULL, UNIQUE (case-insensitive)
    private OffsetDateTime createdAt;  // Managed by @CreatedDate
    private OffsetDateTime updatedAt;  // Managed by @LastModifiedDate
}
```

### DTOs

**CreateTemplateRequestDto** (`/models/dto/CreateTemplateRequestDto.java`) - **ISTNIEJE**
```java
public class CreateTemplateRequestDto {
    @NotBlank(message = "Position name is required")
    @Size(max = 50, message = "Position name must not exceed 50 characters")
    private String positionName;
}
```

**UpdateTemplateRequestDto** (`/models/dto/UpdateTemplateRequestDto.java`) - **ISTNIEJE**
```java
public class UpdateTemplateRequestDto {
    @Size(max = 50, message = "Position name must not exceed 50 characters")
    private String positionName;  // Optional - tylko podane wartości są aktualizowane
}
```

**TemplateResponseDto** (`/models/dto/TemplateResponseDto.java`) - **ISTNIEJE**
```java
public class TemplateResponseDto {
    private UUID id;
    private String positionName;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

**ErrorResponseDto** (`/models/dto/ErrorResponseDto.java`) - **ISTNIEJE**
```java
public record ErrorResponseDto(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationError> validationErrors
) {}
```

### Wyjątki do utworzenia

Wszystkie wyjątki powinny dziedziczyć po `RuntimeException` i znajdować się w pakiecie `/exceptions/`.

**TemplateNotFoundException** - **DO UTWORZENIA**
```java
public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(UUID templateId) {
        super("Template not found with id: " + templateId);
    }
}
```

**DuplicatePositionNameException** - **DO UTWORZENIA**
```java
public class DuplicatePositionNameException extends RuntimeException {
    public DuplicatePositionNameException(String positionName) {
        super("Template with position name '" + positionName + "' already exists");
    }
}
```

**TemplateDeletionException** - **DO UTWORZENIA**
```java
public class TemplateDeletionException extends RuntimeException {
    public TemplateDeletionException(UUID templateId, long taskCount) {
        super("Cannot delete template with id: " + templateId + 
              ". Template has " + taskCount + " associated task(s). " +
              "Please delete all template tasks first.");
    }
    
    public TemplateDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Repository do utworzenia

**TemplateRepository** (`/repositories/TemplateRepository.java`) - **DO UTWORZENIA**
```java
@Repository
public interface TemplateRepository extends JpaRepository<Template, UUID> {
    
    /**
     * Find template by position name (case-insensitive).
     */
    Optional<Template> findByPositionNameIgnoreCase(String positionName);
    
    /**
     * Check if template exists by position name (case-insensitive).
     */
    boolean existsByPositionNameIgnoreCase(String positionName);
    
    /**
     * Find templates by position name containing (case-insensitive, partial match).
     */
    Page<Template> findByPositionNameContainingIgnoreCase(String positionName, Pageable pageable);
}
```

### Mapper do utworzenia

**TemplateMapper** (`/services/mappers/TemplateMapper.java`) - **DO UTWORZENIA**
```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TemplateMapper {
    
    TemplateResponseDto toResponseDto(Template template);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Template toEntity(CreateTemplateRequestDto dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateTemplateRequestDto dto, @MappingTarget Template template);
}
```

### Service do utworzenia

**TemplateService** (`/services/TemplateService.java`) - **DO UTWORZENIA**

Wzorowany na `UserService`, z następującymi kluczowymi metodami:
- `getAllTemplates(Pageable pageable): Page<TemplateResponseDto>`
- `getTemplatesByPositionName(String positionName, Pageable pageable): Page<TemplateResponseDto>`
- `getTemplateById(UUID templateId): TemplateResponseDto`
- `createTemplate(CreateTemplateRequestDto dto): TemplateResponseDto`
- `updateTemplate(UUID templateId, UpdateTemplateRequestDto dto): TemplateResponseDto`
- `deleteTemplate(UUID templateId): void`
- `normalizePositionName(String positionName): String` (private helper)

### Controller do utworzenia

**TemplateController** (`/controllers/TemplateController.java`) - **DO UTWORZENIA**

Wzorowany na `UserController`, implementujący wszystkie 5 endpointów REST API.

---

## 4. Szczegóły odpowiedzi

### GET /templates

**Status Code:** 200 OK

**Response Body:**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "positionName": "software engineer",
      "createdAt": "2025-11-13T10:30:00Z",
      "updatedAt": "2025-11-13T10:30:00Z"
    },
    {
      "id": "223e4567-e89b-12d3-a456-426614174001",
      "positionName": "product manager",
      "createdAt": "2025-11-13T11:00:00Z",
      "updatedAt": "2025-11-13T11:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 100,
  "last": false,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 20,
  "first": true,
  "empty": false
}
```

**Uwaga:** Odpowiedź wykorzystuje standardową strukturę `Page<T>` Spring Data, zawierającą metadane paginacji.

---

### GET /templates/{templateId}

**Status Code:** 200 OK

**Response Body:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "positionName": "software engineer",
  "createdAt": "2025-11-13T10:30:00Z",
  "updatedAt": "2025-11-13T10:30:00Z"
}
```

**Błędy:**
- **404 Not Found** - Szablon nie istnieje

---

### POST /templates

**Status Code:** 201 Created

**Response Headers:**
- `Location: /templates/{newTemplateId}`

**Response Body:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "positionName": "software engineer",
  "createdAt": "2025-11-13T10:30:00Z",
  "updatedAt": "2025-11-13T10:30:00Z"
}
```

**Błędy:**
- **400 Bad Request** - Walidacja nie powiodła się (brak positionName, przekroczono limit znaków, duplikat nazwy stanowiska)

---

### PUT /templates/{templateId}

**Status Code:** 200 OK

**Response Body:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "positionName": "senior software engineer",
  "createdAt": "2025-11-13T10:30:00Z",
  "updatedAt": "2025-11-13T14:45:00Z"
}
```

**Błędy:**
- **400 Bad Request** - Walidacja nie powiodła się (przekroczono limit znaków, duplikat nazwy stanowiska)
- **404 Not Found** - Szablon nie istnieje

---

### DELETE /templates/{templateId}

**Status Code:** 204 No Content

**Response Body:** Brak (pusty)

**Błędy:**
- **400 Bad Request** - Nie można usunąć szablonu z powiązanymi zadaniami
- **404 Not Found** - Szablon nie istnieje

---

### Struktura błędów (wszystkie endpointy)

**400 Bad Request (Walidacja):**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for 1 field(s)",
  "path": "/templates",
  "validationErrors": [
    {
      "field": "positionName",
      "message": "Position name is required"
    }
  ]
}
```

**400 Bad Request (Duplikat):**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Template with position name 'software engineer' already exists",
  "path": "/templates"
}
```

**400 Bad Request (Usunięcie z zadaniami):**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot delete template with id: 123e4567-e89b-12d3-a456-426614174000. Template has 5 associated task(s). Please delete all template tasks first.",
  "path": "/templates/123e4567-e89b-12d3-a456-426614174000"
}
```

**404 Not Found:**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Template not found with id: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/templates/123e4567-e89b-12d3-a456-426614174000"
}
```

**500 Internal Server Error:**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact support if the problem persists.",
  "path": "/templates/123e4567-e89b-12d3-a456-426614174000"
}
```

---

## 5. Przepływ danych

### POST /templates - Tworzenie szablonu

```
1. Client → Controller: POST /templates + CreateTemplateRequestDto
2. Controller: Walidacja @Valid (Bean Validation)
3. Controller → Service: createTemplate(dto)
4. Service: Normalizacja positionName (trim + toLowerCase)
5. Service: Sprawdzenie unikalności positionName (existsByPositionNameIgnoreCase)
   - Jeśli duplikat → throw DuplicatePositionNameException
6. Service → Mapper: toEntity(dto)
7. Mapper → Service: Template entity (bez ID, timestamps)
8. Service: Ustawienie znormalizowanej positionName
9. Service → Repository: save(template)
10. Repository → Database: INSERT INTO template (...)
11. Database → Repository: Template z wygenerowanym ID i timestamps
12. Repository → Service: Saved Template
13. Service → Mapper: toResponseDto(template)
14. Mapper → Service: TemplateResponseDto
15. Service → Controller: TemplateResponseDto
16. Controller → Client: HTTP 201 Created + TemplateResponseDto + Location header
```

**Punkty transakcyjne:**
- Metoda `createTemplate` w service jest oznaczona `@Transactional`
- W przypadku wyjątku (np. duplikat podczas save), transakcja jest cofana

---

### GET /templates - Lista szablonów

```
1. Client → Controller: GET /templates?page=0&size=20&sort=positionName,asc
2. Controller: Parsowanie parametrów do Pageable
3. Controller → Service: getAllTemplates(pageable) lub getTemplatesByPositionName(name, pageable)
4. Service → Repository: findAll(pageable) lub findByPositionNameContainingIgnoreCase(name, pageable)
5. Repository → Database: SELECT * FROM template WHERE ... ORDER BY ... LIMIT ... OFFSET ...
6. Database → Repository: List<Template> + metadata paginacji
7. Repository → Service: Page<Template>
8. Service → Mapper: toResponseDto(template) dla każdego elementu
9. Mapper → Service: List<TemplateResponseDto>
10. Service: Przekształcenie do Page<TemplateResponseDto>
11. Service → Controller: Page<TemplateResponseDto>
12. Controller → Client: HTTP 200 OK + Page<TemplateResponseDto>
```

**Optymalizacje:**
- Read-only transaction: `@Transactional(readOnly = true)`
- Database wykorzystuje indeksy na positionName dla szybkich wyszukiwań
- Paginacja ogranicza ilość danych (default size=20, max size=100)

---

### GET /templates/{templateId} - Pojedynczy szablon

```
1. Client → Controller: GET /templates/123e4567-e89b-12d3-a456-426614174000
2. Controller: Parsowanie UUID z path variable
3. Controller → Service: getTemplateById(templateId)
4. Service → Repository: findById(templateId)
5. Repository → Database: SELECT * FROM template WHERE id = ?
6. Database → Repository: Optional<Template>
7. Repository → Service: Optional<Template>
8. Service: Sprawdzenie Optional
   - Jeśli pusty → throw TemplateNotFoundException
9. Service → Mapper: toResponseDto(template)
10. Mapper → Service: TemplateResponseDto
11. Service → Controller: TemplateResponseDto
12. Controller → Client: HTTP 200 OK + TemplateResponseDto
```

---

### PUT /templates/{templateId} - Aktualizacja szablonu

```
1. Client → Controller: PUT /templates/{id} + UpdateTemplateRequestDto
2. Controller: Walidacja @Valid (Bean Validation)
3. Controller → Service: updateTemplate(templateId, dto)
4. Service → Repository: findById(templateId)
5. Repository → Database: SELECT * FROM template WHERE id = ?
6. Database → Repository: Optional<Template>
7. Repository → Service: Optional<Template>
8. Service: Sprawdzenie Optional
   - Jeśli pusty → throw TemplateNotFoundException
9. Service: Jeśli positionName się zmienia:
   - Normalizacja nowej positionName
   - Sprawdzenie unikalności (existsByPositionNameIgnoreCase)
   - Jeśli duplikat → throw DuplicatePositionNameException
10. Service → Mapper: updateEntityFromDto(dto, template)
11. Mapper: Partial update (tylko non-null pola z DTO)
12. Service: Ustawienie znormalizowanej positionName (jeśli była podana)
13. Service → Repository: save(template)
14. Repository → Database: UPDATE template SET ... WHERE id = ?
15. Database → Repository: Updated Template (z nowym updatedAt)
16. Repository → Service: Updated Template
17. Service → Mapper: toResponseDto(template)
18. Mapper → Service: TemplateResponseDto
19. Service → Controller: TemplateResponseDto
20. Controller → Client: HTTP 200 OK + TemplateResponseDto
```

**Punkty transakcyjne:**
- Metoda `updateTemplate` w service jest oznaczona `@Transactional`
- Dirty checking: JPA automatycznie wykrywa zmiany i aktualizuje bazę

---

### DELETE /templates/{templateId} - Usunięcie szablonu

```
1. Client → Controller: DELETE /templates/123e4567-e89b-12d3-a456-426614174000
2. Controller: Parsowanie UUID z path variable
3. Controller → Service: deleteTemplate(templateId)
4. Service → Repository: existsById(templateId)
5. Repository → Database: SELECT COUNT(*) FROM template WHERE id = ?
6. Database → Repository: boolean
7. Repository → Service: boolean
8. Service: Sprawdzenie istnienia
   - Jeśli nie istnieje → throw TemplateNotFoundException
9. Service → TemplateTaskRepository: countByTemplateId(templateId)
10. TemplateTaskRepository → Database: SELECT COUNT(*) FROM template_task WHERE template_id = ?
11. Database → TemplateTaskRepository: long taskCount
12. TemplateTaskRepository → Service: taskCount
13. Service: Sprawdzenie liczby zadań
    - Jeśli taskCount > 0 → throw TemplateDeletionException(templateId, taskCount)
14. Service → Repository: deleteById(templateId)
15. Repository → Database: DELETE FROM template WHERE id = ?
16. Database: Wykonanie usunięcia (lub błąd jeśli foreign key violation)
17. Database → Repository: Success lub Exception
18. Repository → Service: Success lub Exception
19. Service: W przypadku Exception → throw TemplateDeletionException(message, exception)
20. Service → Controller: void (sukces)
21. Controller → Client: HTTP 204 No Content
```

**Punkty transakcyjne:**
- Metoda `deleteTemplate` w service jest oznaczona `@Transactional`
- Foreign key constraint ON DELETE RESTRICT zapobiega usunięciu szablonu z zadaniami
- Service sprawdza liczbę zadań przed próbą usunięcia dla przyjaznego komunikatu błędu

**Uwaga:** Aby usunąć szablon, najpierw należy usunąć wszystkie powiązane zadania poprzez endpoint `/templates/{templateId}/tasks/{taskId}`.

---

## 6. Względy bezpieczeństwa

### Uwierzytelnianie (Authentication)

**Aktualny stan:**
- Security Config obecnie ustawiony jest na `permitAll()` w profilu testowym
- Docelowo zostanie wdrożone uwierzytelnianie JWT zgodnie z planem API

**Planowane uwierzytelnianie:**
- Użytkownicy logują się przez `/auth/login`
- Otrzymują JWT token w odpowiedzi
- Kolejne żądania zawierają token w nagłówku `Authorization: Bearer <token>`
- Spring Security weryfikuje token i wyciąga informacje o użytkowniku i roli

### Autoryzacja (Authorization)

**Role-Based Access Control (RBAC):**

Zgodnie z planem API, dostęp do endpointów szablonów powinien być ograniczony do:
- **Admin**: Pełny dostęp do wszystkich operacji CRUD na szablonach

**Planowana konfiguracja Spring Security:**
```java
@PreAuthorize("hasRole('ADMIN')")
public class TemplateController {
    // wszystkie metody dostępne tylko dla ADMIN
}
```

Alternatywnie, konfiguracja w SecurityConfig:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/templates/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

### Walidacja danych wejściowych

**Bean Validation:**
- Wszystkie request DTOs używają Bean Validation annotations (`@NotBlank`, `@Size`)
- Controller używa `@Valid` do automatycznej walidacji przed wywołaniem service
- Walidacja jest wymuszona na poziomie controllera przed jakąkolwiek logiką biznesową

**Normalizacja danych:**
- Position name jest normalizowana (trim + toLowerCase) w service przed zapisem
- Zapobiega to problemom z wielkością liter i spacjami
- Unikalność jest sprawdzana po normalizacji

**SQL Injection:**
- Spring Data JPA używa prepared statements, które są odporne na SQL injection
- Wszystkie parametry są automatycznie escapowane

**XSS (Cross-Site Scripting):**
- Position name jest przechowywana jako plain text w bazie danych
- Frontend powinien odpowiednio escapować dane podczas renderowania
- Backend nie zwraca HTML, tylko JSON

### Rate Limiting

**Planowane:**
- Implementacja rate limiting na poziomie API Gateway lub Spring Filter
- Zapobieganie atakom brute-force i DoS
- Przykładowe limity:
  - GET: 100 żądań/minutę/IP
  - POST/PUT/DELETE: 20 żądań/minutę/użytkownika

### Logowanie i audyt

**Logging:**
- Wszystkie operacje są logowane z odpowiednim poziomem (debug, info, warn, error)
- Użycie SLF4J Logger zgodnie z wzorcem z UserService
- Logowanie zawiera kontekst: ID użytkownika, templateId, operację

**Audit Trail:**
- Timestamps (createdAt, updatedAt) są automatycznie zarządzane przez JPA Auditing
- Przyszła implementacja może dodać pola createdBy/updatedBy dla pełnego audytu

### HTTPS

**Wymaganie:**
- W produkcji, cała komunikacja powinna odbywać się przez HTTPS
- Konfiguracja na poziomie serwera aplikacji (np. Spring Boot Embedded Tomcat z SSL)

### CORS (Cross-Origin Resource Sharing)

**Konfiguracja:**
- Należy skonfigurować CORS w Spring Security dla dozwolonych originów frontendu
- Restrykcja tylko do production frontend domain w produkcji

---

## 7. Obsługa błędów

### Wyjątki biznesowe

Wszystkie wyjątki są obsługiwane przez `GlobalExceptionHandler` (@RestControllerAdvice) i mapowane na odpowiednie kody HTTP z `ErrorResponseDto`.

#### 1. TemplateNotFoundException
**Kiedy:** Template o podanym ID nie istnieje
**HTTP Status:** 404 Not Found
**Endpointy:** GET /templates/{id}, PUT /templates/{id}, DELETE /templates/{id}

**Przykładowa odpowiedź:**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Template not found with id: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/templates/123e4567-e89b-12d3-a456-426614174000"
}
```

**Handler w GlobalExceptionHandler:**
```java
@ExceptionHandler(TemplateNotFoundException.class)
public ResponseEntity<ErrorResponseDto> handleTemplateNotFoundException(
        TemplateNotFoundException ex, HttpServletRequest request) {
    log.warn("Template not found: {}", ex.getMessage());
    
    ErrorResponseDto error = new ErrorResponseDto(
            OffsetDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
    );
    
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

---

#### 2. DuplicatePositionNameException
**Kiedy:** Próba utworzenia lub aktualizacji szablonu z nazwą stanowiska, która już istnieje
**HTTP Status:** 400 Bad Request
**Endpointy:** POST /templates, PUT /templates/{id}

**Przykładowa odpowiedź:**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Template with position name 'software engineer' already exists",
  "path": "/templates"
}
```

**Handler w GlobalExceptionHandler:**
```java
@ExceptionHandler(DuplicatePositionNameException.class)
public ResponseEntity<ErrorResponseDto> handleDuplicatePositionNameException(
        DuplicatePositionNameException ex, HttpServletRequest request) {
    log.warn("Duplicate position name error: {}", ex.getMessage());
    
    ErrorResponseDto error = new ErrorResponseDto(
            OffsetDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
    );
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

---

#### 3. TemplateDeletionException
**Kiedy:** 
- Próba usunięcia szablonu, który ma powiązane zadania (ON DELETE RESTRICT)
- Błąd bazy danych podczas usuwania

**HTTP Status:** 400 Bad Request
**Endpointy:** DELETE /templates/{id}

**Przykładowa odpowiedź (szablon z zadaniami):**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot delete template with id: 123e4567-e89b-12d3-a456-426614174000. Template has 5 associated task(s). Please delete all template tasks first.",
  "path": "/templates/123e4567-e89b-12d3-a456-426614174000"
}
```

**Handler w GlobalExceptionHandler:**
```java
@ExceptionHandler(TemplateDeletionException.class)
public ResponseEntity<ErrorResponseDto> handleTemplateDeletionException(
        TemplateDeletionException ex, HttpServletRequest request) {
    log.warn("Template deletion error: {}", ex.getMessage());
    
    ErrorResponseDto error = new ErrorResponseDto(
            OffsetDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
    );
    
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
```

---

### Walidacja Bean Validation

**Kiedy:** Request DTO nie przechodzi walidacji Bean Validation
**HTTP Status:** 400 Bad Request
**Endpointy:** POST /templates, PUT /templates/{id}

**Przykładowa odpowiedź:**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for 1 field(s)",
  "path": "/templates",
  "validationErrors": [
    {
      "field": "positionName",
      "message": "Position name is required"
    }
  ]
}
```

**Handler w GlobalExceptionHandler:** Już istnieje - `handleValidationException(MethodArgumentNotValidException ex)`

---

### Nieprawidłowe parametry zapytania

**Kiedy:** Nieprawidłowe parametry paginacji lub sortowania
**HTTP Status:** 400 Bad Request
**Endpointy:** GET /templates

**Przykładowy scenariusz:**
- Nieprawidłowy format UUID w path parameter
- Ujemny numer strony
- Nieprawidłowe pole sortowania

**Handler w GlobalExceptionHandler:** Już istnieje - `handleIllegalArgumentException(IllegalArgumentException ex)`

---

### Błędy serwera

**Kiedy:** Nieoczekiwany błąd (np. błąd bazy danych, timeout)
**HTTP Status:** 500 Internal Server Error
**Endpointy:** Wszystkie

**Przykładowa odpowiedź:**
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact support if the problem persists.",
  "path": "/templates"
}
```

**Handler w GlobalExceptionHandler:** Już istnieje - `handleGenericException(Exception ex)`

**Logowanie:**
- Błędy 5xx są logowane z pełnym stack trace dla debugowania
- Błędy 4xx są logowane jako warnings z kontekstem

---

### Macierz obsługi błędów

| Endpoint | 200/201 | 204 | 400 | 404 | 500 |
|----------|---------|-----|-----|-----|-----|
| GET /templates | ✓ Zwraca Page<TemplateResponseDto> | - | ✓ Nieprawidłowe parametry zapytania | - | ✓ Błąd bazy danych |
| GET /templates/{id} | ✓ Zwraca TemplateResponseDto | - | ✓ Nieprawidłowy UUID | ✓ Template nie istnieje | ✓ Błąd bazy danych |
| POST /templates | ✓ (201) Utworzony szablon | - | ✓ Walidacja, duplikat positionName | - | ✓ Błąd bazy danych |
| PUT /templates/{id} | ✓ Zaktualizowany szablon | - | ✓ Walidacja, duplikat positionName, nieprawidłowy UUID | ✓ Template nie istnieje | ✓ Błąd bazy danych |
| DELETE /templates/{id} | - | ✓ Usunięto | ✓ Template ma zadania, nieprawidłowy UUID | ✓ Template nie istnieje | ✓ Błąd bazy danych |

---

## 8. Rozważania dotyczące wydajności

### Indeksy bazy danych

**Istniejące indeksy (z migracji Liquibase):**

1. **Indeks funkcjonalny na positionName (case-insensitive):**
   ```sql
   CREATE UNIQUE INDEX idx_template_position_name_lower 
   ON benew.template (LOWER(TRIM(position_name)));
   ```
   - Wymusza unikalność nazw stanowisk (case-insensitive)
   - Przyspiesza wyszukiwanie po positionName
   - Używany przez: `findByPositionNameIgnoreCase`, `existsByPositionNameIgnoreCase`

2. **Index na created_at:**
   ```sql
   CREATE INDEX idx_template_created_at ON benew.template (created_at DESC);
   ```
   - Przyspiesza sortowanie po dacie utworzenia
   - Używany przy: `?sort=createdAt,desc`

3. **Primary key (id):**
   - Automatyczny indeks na kolumnie `id`
   - Używany przez: `findById`, `existsById`, `deleteById`

**Zalecenia:**
- Bieżące indeksy są wystarczające dla typowych operacji
- Jeśli w przyszłości pojawi się filtrowanie po updatedAt, można dodać indeks

---

### Paginacja

**Implementacja:**
- Wszystkie endpointy listujące używają `Pageable` z Spring Data
- Domyślny rozmiar strony: 20 (zalecane)
- Maksymalny rozmiar strony: 100 (do wymuszenia w controller lub konfiguracji)

**Korzyści:**
- Ogranicza ilość danych transferowanych w jednym żądaniu
- Zmniejsza obciążenie pamięci na serwerze i kliencie
- Poprawia czas odpowiedzi

**Przykładowe użycie:**
```java
@GetMapping
public ResponseEntity<Page<TemplateResponseDto>> getAllTemplates(
        @PageableDefault(size = 20, sort = "positionName") Pageable pageable) {
    // Implementation
}
```

---

### Transakcje

**Read-only transactions:**
```java
@Transactional(readOnly = true)
public Page<TemplateResponseDto> getAllTemplates(Pageable pageable) {
    // Read operations
}
```
- Optymalizuje operacje odczytu
- Informuje bazę danych, że nie będzie zapisów
- Umożliwia dodatkowe optymalizacje (np. pomijanie dirty checking w JPA)

**Write transactions:**
```java
@Transactional
public TemplateResponseDto createTemplate(CreateTemplateRequestDto dto) {
    // Write operations
}
```
- Zapewnia atomowość operacji
- Automatyczny rollback w przypadku wyjątku
- Transakcje powinny być jak najkrótsze

---

### N+1 Query Problem

**Aktualnie nie dotyczy:**
- Template entity nie ma eager relationships
- Wszystkie operacje są proste zapytania bez join

**Przyszłe rozważania:**
- Jeśli w przyszłości będziemy zwracać template wraz z listą zadań, należy użyć `@EntityGraph` lub fetch join
- Przykład:
  ```java
  @Query("SELECT t FROM Template t LEFT JOIN FETCH t.tasks WHERE t.id = :id")
  Optional<Template> findByIdWithTasks(@Param("id") UUID id);
  ```

---

### Caching

**Obecnie nieimplementowany:**
- Template data może być często odczytywana (szczególnie przy tworzeniu użytkowników)
- Dane rzadko się zmieniają

**Przyszła implementacja:**
```java
@Cacheable(value = "templates", key = "#templateId")
public TemplateResponseDto getTemplateById(UUID templateId) {
    // Implementation
}

@CacheEvict(value = "templates", key = "#templateId")
public TemplateResponseDto updateTemplate(UUID templateId, UpdateTemplateRequestDto dto) {
    // Implementation
}

@CacheEvict(value = "templates", allEntries = true)
public TemplateResponseDto createTemplate(CreateTemplateRequestDto dto) {
    // Implementation
}
```

**Cache strategy:**
- Spring Cache abstraction z Redis lub Caffeine jako provider
- TTL: 1 godzina dla pojedynczych templates
- Cache dla list templates może mieć krótszy TTL (5-10 minut)

---

### Connection Pooling

**HikariCP (domyślny w Spring Boot):**
- Już skonfigurowany przez Spring Boot
- Optymalizacje w application.yml:
  ```yaml
  spring:
    datasource:
      hikari:
        maximum-pool-size: 10  # Dostosować do obciążenia
        minimum-idle: 5
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
  ```

---

### Monitoring i metryki

**Zalecenia:**
- Spring Boot Actuator dla health checks i metryki
- Micrometer dla zbierania metryk (czasy odpowiedzi, liczba żądań)
- Logging performance-critical operations

**Kluczowe metryki do monitorowania:**
- Średni czas odpowiedzi per endpoint
- Liczba żądań per endpoint
- Ratio błędów 4xx/5xx
- Liczba aktywnych połączeń do bazy danych
- Cache hit/miss ratio (po implementacji cache)

---

## 9. Etapy wdrożenia

Implementacja powinna być wykonana w kolejności zapewniającej testowanie na każdym etapie. Każdy krok powinien być committowany osobno.

---

### Krok 1: Utworzenie wyjątków biznesowych

**Pliki do utworzenia:**
- `/benew-services/src/main/java/com/narciarz/benew/exceptions/TemplateNotFoundException.java`
- `/benew-services/src/main/java/com/narciarz/benew/exceptions/DuplicatePositionNameException.java`
- `/benew-services/src/main/java/com/narciarz/benew/exceptions/TemplateDeletionException.java`

**Akcje:**
1. Utworzyć `TemplateNotFoundException` - extends RuntimeException
   - Konstruktor przyjmujący UUID templateId
   - Message format: "Template not found with id: {templateId}"

2. Utworzyć `DuplicatePositionNameException` - extends RuntimeException
   - Konstruktor przyjmujący String positionName
   - Message format: "Template with position name '{positionName}' already exists"

3. Utworzyć `TemplateDeletionException` - extends RuntimeException
   - Konstruktor #1: (UUID templateId, long taskCount)
   - Message format: "Cannot delete template with id: {templateId}. Template has {taskCount} associated task(s). Please delete all template tasks first."
   - Konstruktor #2: (String message, Throwable cause) dla ogólnych błędów usuwania

**Weryfikacja:**
- Kompilacja bez błędów
- Wszystkie wyjątki dziedziczą po RuntimeException

---

### Krok 2: Rozszerzenie GlobalExceptionHandler

**Plik do edycji:**
- `/benew-services/src/main/java/com/narciarz/benew/exceptions/GlobalExceptionHandler.java`

**Akcje:**
1. Dodać handler dla `TemplateNotFoundException`
   - Mapowanie na HTTP 404 Not Found
   - Logowanie na poziomie WARN
   - Wzorować na `handleUserNotFoundException`

2. Dodać handler dla `DuplicatePositionNameException`
   - Mapowanie na HTTP 400 Bad Request
   - Logowanie na poziomie WARN
   - Wzorować na `handleDuplicateEmailException`

3. Dodać handler dla `TemplateDeletionException`
   - Mapowanie na HTTP 400 Bad Request
   - Logowanie na poziomie WARN
   - Wzorować na `handleUserDeletionException`

**Weryfikacja:**
- Kompilacja bez błędów
- Wszystkie handlery zwracają `ResponseEntity<ErrorResponseDto>`

---

### Krok 3: Utworzenie TemplateRepository

**Plik do utworzenia:**
- `/benew-services/src/main/java/com/narciarz/benew/repositories/TemplateRepository.java`

**Akcje:**
1. Utworzyć interfejs `TemplateRepository` extends `JpaRepository<Template, UUID>`
2. Dodać adnotację `@Repository`
3. Zdefiniować custom query methods:
   - `Optional<Template> findByPositionNameIgnoreCase(String positionName)`
   - `boolean existsByPositionNameIgnoreCase(String positionName)`
   - `Page<Template> findByPositionNameContainingIgnoreCase(String positionName, Pageable pageable)`

**Wzór:**
- Wzorować na `UserRepository`
- Spring Data JPA automatycznie implementuje metody na podstawie nazwy

**Weryfikacja:**
- Kompilacja bez błędów
- Interfejs rozszerza JpaRepository

---

### Krok 4: Utworzenie TemplateMapper

**Plik do utworzenia:**
- `/benew-services/src/main/java/com/narciarz/benew/services/mappers/TemplateMapper.java`

**Akcje:**
1. Utworzyć interfejs `TemplateMapper`
2. Dodać adnotację `@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)`
3. Zdefiniować metody mapowania:
   - `TemplateResponseDto toResponseDto(Template template)`
   - `Template toEntity(CreateTemplateRequestDto dto)` z ignorowaniem id, timestamps
   - `void updateEntityFromDto(UpdateTemplateRequestDto dto, @MappingTarget Template template)` z ignorowaniem id, timestamps

**Wzór:**
- Wzorować na `UserMapper`
- MapStruct wygeneruje implementację w compile time

**Weryfikacja:**
- Kompilacja bez błędów (MapStruct wygeneruje implementację)
- Sprawdzić wygenerowany kod w `target/generated-sources/annotations`

---

### Krok 5: Utworzenie TemplateService

**Plik do utworzenia:**
- `/benew-services/src/main/java/com/narciarz/benew/services/TemplateService.java`

**Akcje:**
1. Utworzyć klasę `TemplateService` z adnotacją `@Service`
2. Dodać adnotację `@Transactional(readOnly = true)` na poziomie klasy
3. Dependency injection przez konstruktor:
   - `TemplateRepository templateRepository`
   - `TemplateMapper templateMapper`
   - `TemplateTaskRepository templateTaskRepository` (będzie potrzebny do sprawdzenia zadań przed usunięciem)
4. Zdefiniować Logger: `private static final Logger log = LoggerFactory.getLogger(TemplateService.class);`

5. Implementować metody:

   **getAllTemplates(Pageable pageable): Page<TemplateResponseDto>**
   - `@Transactional(readOnly = true)` (dziedziczone)
   - Log debug: "Fetching all templates with pagination: {}"
   - Wywołać `templateRepository.findAll(pageable)`
   - Mapować każdy element przez `templateMapper.toResponseDto`
   - Zwrócić `Page<TemplateResponseDto>`

   **getTemplatesByPositionName(String positionName, Pageable pageable): Page<TemplateResponseDto>**
   - `@Transactional(readOnly = true)` (dziedziczone)
   - Log debug: "Searching templates by position: {} with pagination: {}"
   - Wywołać `templateRepository.findByPositionNameContainingIgnoreCase(positionName, pageable)`
   - Mapować i zwrócić

   **getTemplateById(UUID templateId): TemplateResponseDto**
   - `@Transactional(readOnly = true)` (dziedziczone)
   - Log debug: "Fetching template by id: {}"
   - Wywołać `templateRepository.findById(templateId)`
   - Jeśli pusty: throw `TemplateNotFoundException(templateId)`
   - Mapować przez `templateMapper.toResponseDto`
   - Zwrócić

   **createTemplate(CreateTemplateRequestDto dto): TemplateResponseDto**
   - `@Transactional`
   - Log info: "Creating new template with position name: {}"
   - Normalizować positionName: `normalizePositionName(dto.getPositionName())`
   - Sprawdzić unikalność: `templateRepository.existsByPositionNameIgnoreCase(normalizedName)`
     - Jeśli istnieje: throw `DuplicatePositionNameException(normalizedName)`
   - Mapować DTO do entity: `templateMapper.toEntity(dto)`
   - Ustawić znormalizowaną positionName
   - Zapisać: `templateRepository.save(template)`
   - Log info: "Successfully created template with id: {}"
   - Mapować i zwrócić

   **updateTemplate(UUID templateId, UpdateTemplateRequestDto dto): TemplateResponseDto**
   - `@Transactional`
   - Log info: "Updating template with id: {}"
   - Pobrać template: `templateRepository.findById(templateId)`
     - Jeśli pusty: throw `TemplateNotFoundException(templateId)`
   - Jeśli positionName w DTO nie null i się zmienia:
     - Normalizować nową positionName
     - Sprawdzić unikalność (poza aktualnym templatem)
     - Jeśli duplikat: throw `DuplicatePositionNameException(normalizedName)`
   - Aktualizować entity: `templateMapper.updateEntityFromDto(dto, template)`
   - Ustawić znormalizowaną positionName (jeśli była podana)
   - Zapisać (dirty checking JPA): `templateRepository.save(template)`
   - Log info: "Successfully updated template with id: {}"
   - Mapować i zwrócić

   **deleteTemplate(UUID templateId): void**
   - `@Transactional`
   - Log info: "Attempting to delete template with id: {}"
   - Sprawdzić istnienie: `templateRepository.existsById(templateId)`
     - Jeśli nie istnieje: throw `TemplateNotFoundException(templateId)`
   - Sprawdzić liczbę zadań: `templateTaskRepository.countByTemplateId(templateId)`
     - Jeśli taskCount > 0: throw `TemplateDeletionException(templateId, taskCount)`
   - Try-catch:
     - Try: `templateRepository.deleteById(templateId)`
     - Catch Exception: throw `TemplateDeletionException("Failed to delete template: " + e.getMessage(), e)`
   - Log info: "Successfully deleted template with id: {}"

   **normalizePositionName(String positionName): String** (private)
   - Return: `positionName.trim().toLowerCase()`

**Wzór:**
- Wzorować na `UserService`
- Używać tego samego stylu logowania i obsługi błędów

**Uwaga:**
- `TemplateTaskRepository` będzie potrzebny, ale może nie istnieć jeszcze - można tymczasowo pominąć sprawdzanie zadań w deleteTemplate lub utworzyć stub

**Weryfikacja:**
- Kompilacja bez błędów
- Wszystkie metody mają odpowiednie adnotacje transakcyjne
- Logowanie jest spójne z UserService

---

### Krok 6: Utworzenie TemplateController

**Plik do utworzenia:**
- `/benew-services/src/main/java/com/narciarz/benew/controllers/TemplateController.java`

**Akcje:**
1. Utworzyć klasę `TemplateController` z adnotacjami:
   - `@RestController`
   - `@RequestMapping("/templates")`
2. Dependency injection przez konstruktor:
   - `TemplateService templateService`
3. Zdefiniować Logger: `private static final Logger log = LoggerFactory.getLogger(TemplateController.class);`

4. Implementować endpointy:

   **GET /templates**
   ```java
   @GetMapping
   public ResponseEntity<Page<TemplateResponseDto>> getAllTemplates(
           @RequestParam(required = false) String positionName,
           @PageableDefault(size = 20, sort = "positionName") Pageable pageable) {
       
       log.debug("GET /templates - positionName: {}, page: {}", positionName, pageable);
       
       Page<TemplateResponseDto> templates;
       if (positionName != null && !positionName.isBlank()) {
           templates = templateService.getTemplatesByPositionName(positionName, pageable);
       } else {
           templates = templateService.getAllTemplates(pageable);
       }
       
       return ResponseEntity.ok(templates);
   }
   ```

   **GET /templates/{templateId}**
   ```java
   @GetMapping("/{templateId}")
   public ResponseEntity<TemplateResponseDto> getTemplateById(
           @PathVariable UUID templateId) {
       
       log.debug("GET /templates/{}", templateId);
       
       TemplateResponseDto template = templateService.getTemplateById(templateId);
       return ResponseEntity.ok(template);
   }
   ```

   **POST /templates**
   ```java
   @PostMapping
   public ResponseEntity<TemplateResponseDto> createTemplate(
           @Valid @RequestBody CreateTemplateRequestDto dto) {
       
       log.info("POST /templates - positionName: {}", dto.getPositionName());
       
       TemplateResponseDto created = templateService.createTemplate(dto);
       
       // Build Location header
       URI location = ServletUriComponentsBuilder
               .fromCurrentRequest()
               .path("/{id}")
               .buildAndExpand(created.getId())
               .toUri();
       
       return ResponseEntity.created(location).body(created);
   }
   ```

   **PUT /templates/{templateId}**
   ```java
   @PutMapping("/{templateId}")
   public ResponseEntity<TemplateResponseDto> updateTemplate(
           @PathVariable UUID templateId,
           @Valid @RequestBody UpdateTemplateRequestDto dto) {
       
       log.info("PUT /templates/{}", templateId);
       
       TemplateResponseDto updated = templateService.updateTemplate(templateId, dto);
       return ResponseEntity.ok(updated);
   }
   ```

   **DELETE /templates/{templateId}**
   ```java
   @DeleteMapping("/{templateId}")
   public ResponseEntity<Void> deleteTemplate(
           @PathVariable UUID templateId) {
       
       log.info("DELETE /templates/{}", templateId);
       
       templateService.deleteTemplate(templateId);
       return ResponseEntity.noContent().build();
   }
   ```

**Wzór:**
- Wzorować na `UserController`
- Controller odpowiedzialny tylko za routing i I/O mapping, bez logiki biznesowej
- Wszystkie wyjątki są obsługiwane przez GlobalExceptionHandler

**Weryfikacja:**
- Kompilacja bez błędów
- Wszystkie endpointy mają odpowiednie adnotacje HTTP method
- @Valid jest użyte dla request bodies

---

### Krok 7: Utworzenie testów jednostkowych dla TemplateService

**Plik do utworzenia:**
- `/benew-services/src/test/java/com/narciarz/benew/services/TemplateServiceTest.java`

**Akcje:**
1. Utworzyć klasę testową z adnotacjami:
   - `@ExtendWith(MockitoExtension.class)`
2. Mock dependencies:
   - `@Mock TemplateRepository templateRepository`
   - `@Mock TemplateMapper templateMapper`
   - `@Mock TemplateTaskRepository templateTaskRepository`
   - `@InjectMocks TemplateService templateService`

3. Implementować testy dla każdej metody service:

   **getAllTemplates:**
   - Test: powinien zwrócić stronę szablonów
   - Mock: `templateRepository.findAll(pageable)` zwraca Page<Template>
   - Mock: `templateMapper.toResponseDto(template)` zwraca TemplateResponseDto
   - Assert: Zwrócona strona ma oczekiwaną zawartość

   **getTemplatesByPositionName:**
   - Test: powinien zwrócić szablony filtrowane po positionName
   - Mock odpowiednio
   - Assert: Poprawne wywołanie repository i mapping

   **getTemplateById:**
   - Test sukcesu: powinien zwrócić template gdy istnieje
   - Test błędu: powinien rzucić TemplateNotFoundException gdy nie istnieje

   **createTemplate:**
   - Test sukcesu: powinien utworzyć template z znormalizowaną positionName
   - Test błędu: powinien rzucić DuplicatePositionNameException gdy positionName już istnieje

   **updateTemplate:**
   - Test sukcesu: powinien zaktualizować template
   - Test błędu (nie znaleziono): powinien rzucić TemplateNotFoundException
   - Test błędu (duplikat): powinien rzucić DuplicatePositionNameException

   **deleteTemplate:**
   - Test sukcesu: powinien usunąć template gdy nie ma zadań
   - Test błędu (nie znaleziono): powinien rzucić TemplateNotFoundException
   - Test błędu (ma zadania): powinien rzucić TemplateDeletionException

**Wzór:**
- Wzorować na `UserServiceTest`
- Używać Mockito do mockowania zależności
- Używać AssertJ do asercji

**Weryfikacja:**
- Wszystkie testy przechodzą
- Code coverage > 80% dla TemplateService

---

### Krok 8: Utworzenie testów integracyjnych dla TemplateController

**Plik do utworzenia:**
- `/benew-services/src/test/java/com/narciarz/benew/controllers/TemplateControllerTest.java`

**Akcje:**
1. Utworzyć klasę testową z adnotacjami:
   - `@WebMvcTest(TemplateController.class)`
   - `@ActiveProfiles("test")`
2. Wstrzyknąć:
   - `@Autowired MockMvc mockMvc`
   - `@MockBean TemplateService templateService`
   - `@Autowired ObjectMapper objectMapper` (do serializacji JSON)

3. Implementować testy dla każdego endpointu:

   **GET /templates:**
   - Test: powinien zwrócić stronę szablonów z 200 OK
   - Mock: `templateService.getAllTemplates(any())` zwraca Page<TemplateResponseDto>
   - Perform: GET /templates
   - Assert: Status 200, JSON zawiera content, pageable, totalElements

   **GET /templates?positionName=engineer:**
   - Test: powinien zwrócić filtrowane szablony z 200 OK
   - Mock: `templateService.getTemplatesByPositionName(eq("engineer"), any())`
   - Perform: GET /templates?positionName=engineer
   - Assert: Status 200, poprawne wywołanie service

   **GET /templates/{id}:**
   - Test sukcesu: powinien zwrócić template z 200 OK
   - Test błędu: powinien zwrócić 404 gdy template nie istnieje
   - Mock odpowiednio (success vs throw TemplateNotFoundException)

   **POST /templates:**
   - Test sukcesu: powinien utworzyć template z 201 Created i Location header
   - Test błędu walidacji: powinien zwrócić 400 gdy positionName jest pusty
   - Test błędu duplikatu: powinien zwrócić 400 gdy positionName już istnieje

   **PUT /templates/{id}:**
   - Test sukcesu: powinien zaktualizować template z 200 OK
   - Test błędu walidacji: powinien zwrócić 400 gdy positionName przekracza limit
   - Test błędu 404: powinien zwrócić 404 gdy template nie istnieje

   **DELETE /templates/{id}:**
   - Test sukcesu: powinien usunąć template z 204 No Content
   - Test błędu 404: powinien zwrócić 404 gdy template nie istnieje
   - Test błędu 400: powinien zwrócić 400 gdy template ma zadania

**Wzór:**
- Wzorować na `UserControllerTest`
- Używać MockMvc do testowania HTTP requests/responses
- Używać JSONPath do asercji JSON

**Przykład testu:**
```java
@Test
void shouldReturnTemplateWhenValidId() throws Exception {
    // Given
    UUID templateId = UUID.randomUUID();
    TemplateResponseDto template = new TemplateResponseDto(
        templateId, 
        "software engineer", 
        OffsetDateTime.now(), 
        OffsetDateTime.now()
    );
    when(templateService.getTemplateById(templateId)).thenReturn(template);
    
    // When & Then
    mockMvc.perform(get("/templates/" + templateId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(templateId.toString()))
        .andExpect(jsonPath("$.positionName").value("software engineer"));
}
```

**Weryfikacja:**
- Wszystkie testy przechodzą
- Wszystkie scenariusze sukcesu i błędów są pokryte

---

### Krok 9: Testowanie manualne z użyciem curl lub Postman

**Przygotowanie:**
1. Uruchomić aplikację: `mvn spring-boot:run`
2. Sprawdzić dostępność: `curl http://localhost:8080/actuator/health`

**Scenariusze testowe:**

**1. Utworzenie szablonu (POST /templates):**
```bash
curl -X POST http://localhost:8080/templates \
  -H "Content-Type: application/json" \
  -d '{"positionName": "Software Engineer"}'
```
Oczekiwane: 201 Created, Location header, JSON z utworzonym szablonem

**2. Pobranie wszystkich szablonów (GET /templates):**
```bash
curl http://localhost:8080/templates
```
Oczekiwane: 200 OK, Page<TemplateResponseDto> z content

**3. Pobranie szablonu po ID (GET /templates/{id}):**
```bash
curl http://localhost:8080/templates/{templateId}
```
Oczekiwane: 200 OK, TemplateResponseDto

**4. Aktualizacja szablonu (PUT /templates/{id}):**
```bash
curl -X PUT http://localhost:8080/templates/{templateId} \
  -H "Content-Type: application/json" \
  -d '{"positionName": "Senior Software Engineer"}'
```
Oczekiwane: 200 OK, zaktualizowany szablon

**5. Próba duplikatu (POST /templates):**
```bash
curl -X POST http://localhost:8080/templates \
  -H "Content-Type: application/json" \
  -d '{"positionName": "Software Engineer"}'
```
Oczekiwane: 400 Bad Request, ErrorResponseDto z informacją o duplikacie

**6. Usunięcie szablonu (DELETE /templates/{id}):**
```bash
curl -X DELETE http://localhost:8080/templates/{templateId}
```
Oczekiwane: 204 No Content

**7. Próba pobrania nieistniejącego szablonu (GET /templates/{id}):**
```bash
curl http://localhost:8080/templates/00000000-0000-0000-0000-000000000000
```
Oczekiwane: 404 Not Found, ErrorResponseDto

**Weryfikacja:**
- Wszystkie scenariusze działają zgodnie z oczekiwaniami
- Sprawdzić logi aplikacji pod kątem poprawnego logowania
- Sprawdzić bazę danych (positionName jest znormalizowane)

---

### Krok 10: Dokumentacja i finalizacja

**Akcje:**
1. **Dodać JavaDoc:**
   - Upewnić się, że wszystkie publiczne metody mają JavaDoc
   - Szczególnie w Service i Controller

2. **Aktualizować README (jeśli istnieje):**
   - Dodać informację o nowych endpointach /templates
   - Przykłady użycia

3. **Sprawdzić linting:**
   - Uruchomić linter/formatter: `mvn spotless:apply` (jeśli skonfigurowany)
   - Naprawić wszystkie ostrzeżenia

4. **Code review checklist:**
   - [ ] Wszystkie testy jednostkowe przechodzą
   - [ ] Wszystkie testy integracyjne przechodzą
   - [ ] Code coverage > 80%
   - [ ] Wszystkie endpointy manualne przetestowane
   - [ ] Logowanie jest spójne i informacyjne
   - [ ] Obsługa błędów jest kompletna
   - [ ] DTOs mają Bean Validation
   - [ ] Service używa transakcji
   - [ ] Position name jest normalizowana
   - [ ] Dokumentacja jest aktualna

5. **Commit i push:**
   - Commit message: "feat: Implement Templates REST API endpoints"
   - Push do branch: `templates` (zgodnie z git status)

**Weryfikacja końcowa:**
- Cała aplikacja się buduje bez błędów: `mvn clean install`
- Wszystkie testy przechodzą
- Aplikacja uruchamia się bez błędów
- Wszystkie endpointy działają poprawnie

---

## 10. Przyszłe rozszerzenia

Po ukończeniu podstawowej implementacji, następujące funkcjonalności mogą zostać dodane:

1. **Autoryzacja:**
   - Implementacja JWT authentication
   - Ograniczenie dostępu do endpointów dla roli ADMIN
   - Spring Security configuration

2. **Caching:**
   - Redis lub Caffeine cache dla często odczytywanych szablonów
   - Cache eviction przy aktualizacji/usunięciu

3. **Auditing:**
   - Dodanie pól `createdBy` i `updatedBy` do Template entity
   - Tracking który admin utworzył/zaktualizował szablon

4. **Wersjonowanie szablonów:**
   - Historia zmian w szablonach
   - Możliwość przywrócenia poprzedniej wersji

5. **Bulk operations:**
   - Endpoint do usunięcia wielu szablonów na raz
   - CSV import dla szablonów (zgodnie z planem API)

6. **Search improvements:**
   - Full-text search po position name
   - Elasticsearch integration dla advanced search

7. **Validation enhancements:**
   - Custom validation annotation dla position name format
   - Więcej reguł biznesowych (np. limit liczby szablonów)

---

## Podsumowanie

Ten plan implementacji dostarcza kompleksowych wskazówek do wdrożenia endpointów REST API dla zarządzania szablonami onboardingowymi. Plan jest zgodny z:
- Istniejącą architekturą aplikacji (Spring Boot + PostgreSQL)
- Wzorcami z UserController i UserService
- Regułami implementacji (Spring Boot best practices, slice testing)
- Specyfikacją API z planu REST API

Implementacja powinna być wykonana krok po kroku, z testowaniem na każdym etapie. Każdy krok buduje na poprzednim, zapewniając stopniowe i bezpieczne wdrożenie funkcjonalności.

