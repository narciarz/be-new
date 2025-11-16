# 🛡️ Macierz Kontroli Dostępu (Role-Based Access Control)

## Przegląd

Ten dokument definiuje uprawnienia dla każdej roli użytkownika w systemie Be New. System implementuje **Role-Based Access Control (RBAC)** z trzema podstawowymi rolami: `ADMIN`, `MANAGER`, i `USER`.

## Role

- **ADMIN** - Pełny dostęp do wszystkich zasobów systemu, zarządzanie użytkownikami, szablonami i procesami onboardingowymi
- **MANAGER** - Zarządzanie użytkownikami i procesami onboardingowymi w ramach swojego zespołu
- **USER** - Dostęp do własnych procesów onboardingowych, możliwość przeglądania i aktualizacji swoich zadań

## Macierz Dostępu

| Endpoint | Metoda | ADMIN | MANAGER | USER | Uwagi |
|----------|--------|-------|---------|------|-------|
| **Authentication** |
| `/auth/login` | POST | ✅ Public | ✅ Public | ✅ Public | Endpoint publiczny |
| **Users** |
| `/api/users` | GET | ✅ | ✅ | ❌ | Lista wszystkich użytkowników |
| `/api/users` | POST | ✅ | ✅ | ❌ | Tworzenie nowego użytkownika |
| `/api/users/{id}` | PUT | ✅ | ✅ | ❌ | Aktualizacja użytkownika |
| `/api/users/{id}` | DELETE | ✅ | ✅ | ❌ | Usuwanie użytkownika |
| **Templates** |
| `/api/templates` | GET | ✅ | ❌ | ❌ | Lista szablonów onboardingowych |
| `/api/templates` | POST | ✅ | ❌ | ❌ | Tworzenie nowego szablonu |
| `/api/templates/{id}` | PUT | ✅ | ❌ | ❌ | Aktualizacja szablonu |
| `/api/templates/{id}` | DELETE | ✅ | ❌ | ❌ | Usuwanie szablonu |
| **Onboarding** |
| `/api/onboarding` | GET | ✅ | ✅ | ✅ | Lista procesów onboardingowych |
| `/api/onboarding` | POST | ✅ | ✅ | ❌ | Rozpoczęcie nowego procesu |
| `/api/onboarding/{id}` | PUT | ✅ | ✅ | ✅* | Aktualizacja procesu onboardingowego |

### Uwagi do uprawnień

#### ✅* USER - Ograniczony dostęp do `PUT /api/onboarding/{id}`

USER może aktualizować **tylko własne procesy onboardingowe**. Wymaga to dodatkowej logiki biznesowej:

```java
// Pseudo-kod walidacji
if (currentUser.getRole() == UserRole.USER) {
    if (!onboarding.getUser().getId().equals(currentUser.getId())) {
        throw new ForbiddenException("User can only update their own onboarding process");
    }
}
```

#### Filtrowanie danych dla USER

Endpoint `GET /api/onboarding` dla roli USER zwraca **tylko procesy przypisane do tego użytkownika**:

```java
// Pseudo-kod filtrowania
if (currentUser.getRole() == UserRole.USER) {
    return onboardingRepository.findByUserId(currentUser.getId());
}
```

## Implementacja w Spring Security

### SecurityConfig

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            // Public endpoints
            .requestMatchers("/auth/login", "/swagger-ui/**", "/v3/api-docs/**")
                .permitAll()
            
            // User management - ADMIN & MANAGER only
            .requestMatchers("/api/users/**")
                .hasAnyRole("ADMIN", "MANAGER")
            
            // Templates - ADMIN only
            .requestMatchers("/api/templates/**")
                .hasRole("ADMIN")
            
            // Onboarding - Authenticated users (fine-grained in service layer)
            .requestMatchers("/api/onboarding/**")
                .hasAnyRole("ADMIN", "MANAGER", "USER")
            
            // All other requests require authentication
            .anyRequest().authenticated()
        );
    
    return http.build();
}
```

### Method-Level Security (Future Enhancement)

Dla bardziej szczegółowej kontroli dostępu można użyć `@PreAuthorize`:

```java
@PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #id == authentication.principal.id)")
public OnboardingDto updateOnboarding(UUID id, OnboardingUpdateDto dto) {
    // Implementation
}
```

## Kody odpowiedzi HTTP

| Kod | Znaczenie | Kiedy używać |
|-----|-----------|--------------|
| `200 OK` | Sukces | Request zakończony pomyślnie |
| `201 Created` | Zasób utworzony | POST zakończony sukcesem |
| `400 Bad Request` | Błąd walidacji | Nieprawidłowe dane wejściowe |
| `401 Unauthorized` | Brak uwierzytelnienia | Brak/nieprawidłowy token JWT |
| `403 Forbidden` | Brak uprawnień | Użytkownik nie ma wymaganych uprawnień |
| `404 Not Found` | Zasób nie istnieje | Zasób o podanym ID nie został znaleziony |
| `500 Internal Server Error` | Błąd serwera | Nieoczekiwany błąd aplikacji |

## Testowanie uprawnień

### Test Suite dla RBAC

Każdy endpoint powinien mieć testy weryfikujące:

1. ✅ **Pozytywny przypadek** - uprawniony użytkownik ma dostęp
2. ❌ **401 Unauthorized** - brak tokenu JWT
3. ❌ **403 Forbidden** - token poprawny, ale brak uprawnień
4. ✅ **Role-specific logic** - specyficzna logika dla każdej roli (np. USER widzi tylko swoje dane)

### Przykład testu

```java
@Test
void shouldReturn403WhenUserTriesToAccessTemplates() throws Exception {
    String userToken = generateTokenForRole(UserRole.USER);
    
    mockMvc.perform(get("/api/templates")
            .header("Authorization", "Bearer " + userToken))
        .andExpect(status().isForbidden());
}
```

## Historia zmian

| Data | Autor | Zmiany |
|------|-------|--------|
| 2025-11-16 | System | Utworzenie dokumentu macierzy uprawnień |

## Przyszłe rozszerzenia

- [ ] Implementacja **method-level security** (`@PreAuthorize`, `@PostAuthorize`)
- [ ] Dodanie roli `SUPER_ADMIN` z możliwością zarządzania systemem
- [ ] Implementacja **resource-based access control** (np. MANAGER widzi tylko swojego zespołu)
- [ ] Audit log dla wszystkich operacji CRUD
- [ ] Rate limiting per role (np. USER - 100 req/min, ADMIN - 1000 req/min)

