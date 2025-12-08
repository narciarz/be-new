# Implementacja zarządzania użytkownikami dla roli MANAGER

## Przegląd zmian

Rozszerzono funkcjonalność systemu, aby menedżerowie mogli dodawać, edytować, przeglądać i usuwać użytkowników ze swojego zespołu, przy jednoczesnym zachowaniu pełnej kontroli administratorów nad wszystkimi użytkownikami.

## Backend - Zmiany w zabezpieczeniach

### 1. UserService - Dodano metody kontekstu bezpieczeństwa

**Plik:** `UserService.java`

Dodano dwie prywatne metody pomocnicze do ekstrakcji danych z JWT tokena:

```java
private UUID getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new IllegalStateException("No authenticated user found");
    }
    if (authentication.getPrincipal() instanceof Jwt jwt) {
        String subject = jwt.getSubject();
        return UUID.fromString(subject);
    }
    throw new IllegalStateException("Invalid authentication principal type");
}

private UserRole getCurrentUserRole() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        throw new IllegalStateException("No authenticated user found");
    }
    if (authentication.getPrincipal() instanceof Jwt jwt) {
        String role = jwt.getClaim("role");
        return UserRole.valueOf(role);
    }
    throw new IllegalStateException("Invalid authentication principal type");
}
```

### 2. UserService - Filtrowanie danych według roli

Wszystkie metody pobierające dane zostały zmodyfikowane, aby automatycznie filtrować wyniki:

#### `getAllUsers()`
- **ADMIN**: zwraca wszystkich użytkowników
- **MANAGER**: zwraca tylko użytkowników przypisanych do tego menedżera

#### `getUsersByRole()`
- **ADMIN**: zwraca wszystkich użytkowników z daną rolą
- **MANAGER**: zwraca tylko użytkowników z ich zespołu o danej roli

#### `getUsersByManager()`, `getUsersByPosition()`, `getUsersByLastName()`
- **ADMIN**: brak ograniczeń
- **MANAGER**: może wyszukiwać tylko w swoim zespole

#### `getUserById()`
- **ADMIN**: może pobrać dowolnego użytkownika
- **MANAGER**: może pobrać tylko użytkowników ze swojego zespołu (zwraca 404 dla innych)

### 3. UserService - Tworzenie użytkowników

**Metoda:** `createUser()`

Dodano walidację i automatyczne przypisanie:

```java
// MANAGER może tworzyć tylko użytkowników z rolą USER
if (currentUserRole == UserRole.MANAGER && dto.getRole() != UserRole.USER) {
    throw new IllegalArgumentException("Managers can only create users with USER role");
}

// MANAGER tworzy użytkowników przypisanych do siebie, ignorując managerId z DTO
if (currentUserRole == UserRole.MANAGER) {
    AppUser manager = userRepository.findById(currentUserId)
            .orElseThrow(() -> new IllegalStateException("Current user not found"));
    user.setManager(manager);
}
```

### 4. UserService - Aktualizacja użytkowników

**Metoda:** `updateUser()`

Dodano walidację uprawnień:

```java
// MANAGER może aktualizować tylko swoich użytkowników
if (currentUserRole == UserRole.MANAGER) {
    if (!user.getManager().getId().equals(currentUserId)) {
        throw new UserNotFoundException(userId);
    }
    
    // MANAGER nie może zmieniać roli
    if (dto.getRole() != null && dto.getRole() != user.getRole()) {
        throw new IllegalArgumentException("Managers cannot change user roles");
    }
    
    // MANAGER nie może zmieniać menedżera
    if (dto.getManagerId() != null && !dto.getManagerId().equals(user.getManager().getId())) {
        throw new IllegalArgumentException("Managers cannot reassign users to other managers");
    }
}
```

### 5. UserService - Usuwanie użytkowników

**Metoda:** `deleteUser()`

```java
// MANAGER może usuwać tylko swoich użytkowników
if (currentUserRole == UserRole.MANAGER) {
    if (!user.getManager().getId().equals(currentUserId)) {
        throw new UserNotFoundException(userId);
    }
}
```

### 6. UserRepository - Nowe metody zapytań

Dodano metody do filtrowania użytkowników menedżera:

```java
Page<AppUser> findByManagerIdAndRole(UUID managerId, UserRole role, Pageable pageable);
Page<AppUser> findByManagerIdAndPositionNameContainingIgnoreCase(UUID managerId, String positionName, Pageable pageable);
Page<AppUser> findByManagerIdAndLastNameContainingIgnoreCase(UUID managerId, String lastName, Pageable pageable);
```

### 7. UserController - Adnotacje @PreAuthorize

Dodano adnotacje zabezpieczające na poziomie kontrolera (defense in depth):

```java
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
```

Na wszystkich metodach kontrolera zarządzania użytkownikami.

### 8. GlobalExceptionHandler - Obsługa AccessDeniedException

Dodano handler dla wyjątków Spring Security:

```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
        AccessDeniedException ex, HttpServletRequest request) {
    log.warn("Access denied for request to: {} - {}", request.getRequestURI(), ex.getMessage());
    
    ErrorResponseDto error = new ErrorResponseDto(
            OffsetDateTime.now(),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            "Access denied. You don't have permission to access this resource.",
            request.getRequestURI()
    );
    
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
}
```

## Frontend - Zmiany w interfejsie

### 1. UserDialogComponent - Automatyczne ukrywanie pól

**Plik:** `user-dialog.component.ts`

Dodano logikę wykrywania roli menedżera:

```typescript
readonly currentUser = this.authService.currentUser;
readonly isManager = computed(() => this.currentUser()?.role === UserRole.MANAGER);
readonly isAdmin = computed(() => this.currentUser()?.role === UserRole.ADMIN);

// Dostępne role zależą od roli użytkownika
readonly roles = computed(() => {
  if (this.isManager()) {
    return [{ value: UserRole.USER, label: 'Użytkownik' }];
  }
  return this.allRoles;
});
```

W konstruktorze formularza:

```typescript
role: [
  { 
    value: defaultRole, 
    disabled: this.isManager() // Zablokowanie pola roli dla menedżerów
  }, 
  [Validators.required]
],
managerId: [
  {
    value: this.data.user?.managerId || null,
    disabled: this.isManager() // Zablokowanie pola menedżera
  }
]
```

### 2. UserDialogComponent - Template HTML

**Plik:** `user-dialog.component.html`

Dodano warunkowe wyświetlanie:

```html
<mat-form-field appearance="outline">
  <mat-label>Rola</mat-label>
  <mat-select formControlName="role">
    @for (role of roles(); track role.value) {
      <mat-option [value]="role.value">{{ role.label }}</mat-option>
    }
  </mat-select>
  @if (isManager()) {
    <mat-hint>Menedżerowie mogą dodawać tylko użytkowników</mat-hint>
  }
</mat-form-field>

@if (!isManager()) {
  <mat-form-field appearance="outline">
    <mat-label>Menedżer (opcjonalnie)</mat-label>
    <mat-select formControlName="managerId">
      <mat-option [value]="null">Brak menedżera</mat-option>
      @for (manager of allUsers(); track manager.id) {
        <mat-option [value]="manager.id">
          {{ manager.firstName }} {{ manager.lastName }} ({{ manager.email }})
        </mat-option>
      }
    </mat-select>
  </mat-form-field>
} @else {
  <div class="info-message">
    <mat-icon>info</mat-icon>
    <span>Użytkownik zostanie automatycznie przypisany do Ciebie jako menedżera</span>
  </div>
}
```

### 3. Routing - Dodano ścieżkę dla menedżera

**Plik:** `app.routes.ts`

```typescript
{
  path: 'manager/users',
  loadComponent: () =>
    import('../components/admin/user-management/user-management.component').then(
      (m) => m.UserManagementComponent
    ),
},
```

### 4. Dashboard - Dodano link nawigacyjny

**Plik:** `dashboard.component.html`

```html
@if (currentUser()?.role === UserRole.MANAGER) {
  <!-- ... inne linki ... -->
  <a mat-list-item routerLink="/dashboard/manager/users" routerLinkActive="active">
    <mat-icon matListItemIcon>person_add</mat-icon>
    <span matListItemTitle>Zarządzanie użytkownikami</span>
  </a>
  <!-- ... inne linki ... -->
}
```

## Bezpieczeństwo API

### Wielowarstwowa ochrona (Defense in Depth)

System implementuje wielowarstwową ochronę:

1. **Spring Security (SecurityConfig)**
   - Kontrola dostępu na poziomie URL: `/api/users/**` wymaga roli ADMIN lub MANAGER
   
2. **@PreAuthorize na kontrolerze**
   - Dodatkowa warstwa weryfikacji uprawnień na poziomie metody
   
3. **Logika biznesowa w serwisie**
   - Szczegółowa kontrola dostępu oparta na właścicielu zasobu
   - Walidacja danych wejściowych specyficznych dla roli

### Zasady bezpieczeństwa

#### MANAGER może:
- ✅ Przeglądać użytkowników ze swojego zespołu
- ✅ Dodawać nowych użytkowników z rolą USER (automatycznie przypisanych do siebie)
- ✅ Edytować dane swoich użytkowników (email, imię, nazwisko, stanowisko, hasło)
- ✅ Usuwać użytkowników ze swojego zespołu
- ✅ Filtrować/wyszukiwać w swoim zespole

#### MANAGER NIE może:
- ❌ Tworzyć użytkowników z rolami ADMIN lub MANAGER
- ❌ Zmieniać roli użytkowników
- ❌ Przypisywać użytkowników do innych menedżerów
- ❌ Przeglądać użytkowników spoza swojego zespołu
- ❌ Modyfikować użytkowników innych menedżerów

#### ADMIN może:
- ✅ Wszystko bez ograniczeń
- ✅ Zarządzać użytkownikami z dowolną rolą
- ✅ Przypisywać użytkowników do dowolnych menedżerów
- ✅ Zmieniać role użytkowników

## Kody odpowiedzi HTTP

| Kod | Znaczenie | Kiedy |
|-----|-----------|-------|
| 200 OK | Sukces | Poprawne pobranie/aktualizacja danych |
| 201 Created | Utworzono | Pomyślne utworzenie użytkownika |
| 204 No Content | Brak treści | Pomyślne usunięcie użytkownika |
| 400 Bad Request | Błąd walidacji | Nieprawidłowe dane, próba zmiany roli przez MANAGER |
| 401 Unauthorized | Brak autoryzacji | Brak lub nieprawidłowy token JWT |
| 403 Forbidden | Brak uprawnień | Próba dostępu do zasobów bez uprawnień |
| 404 Not Found | Nie znaleziono | Użytkownik nie istnieje lub nie należy do zespołu |
| 500 Internal Server Error | Błąd serwera | Nieoczekiwany błąd aplikacji |

## Testowanie

### Scenariusze testowe dla MANAGER

1. **Tworzenie użytkownika USER**
   - ✅ Powinno się udać
   - ✅ Użytkownik automatycznie przypisany do menedżera
   
2. **Próba utworzenia użytkownika ADMIN/MANAGER**
   - ❌ Powinno zwrócić 400 Bad Request
   
3. **Przeglądanie użytkowników**
   - ✅ Widzi tylko swoich użytkowników
   
4. **Edycja użytkownika ze swojego zespołu**
   - ✅ Może zmienić email, imię, nazwisko, stanowisko, hasło
   - ❌ Nie może zmienić roli (400 Bad Request)
   - ❌ Nie może zmienić menedżera (400 Bad Request)
   
5. **Próba edycji użytkownika innego menedżera**
   - ❌ Powinno zwrócić 404 Not Found
   
6. **Usunięcie użytkownika ze swojego zespołu**
   - ✅ Powinno się udać
   
7. **Próba usunięcia użytkownika innego menedżera**
   - ❌ Powinno zwrócić 404 Not Found

### Scenariusze testowe dla ADMIN

1. **Wszystkie operacje CRUD**
   - ✅ Bez ograniczeń na wszystkich użytkownikach

## Zgodność z wymaganiami

### Zrealizowane wymagania funkcjonalne

✅ **PRD Sekcja 3.1** - Zarządzanie użytkownikami (rola Administrator)
- Administrator zachowuje pełną kontrolę CRUD nad wszystkimi użytkownikami

✅ **PRD Rozszerzenie** - Zarządzanie użytkownikami (rola Manager)
- Menedżer może dodawać użytkowników z rolą USER
- Użytkownicy są automatycznie przypisywani do menedżera
- Menedżer może przeglądać, edytować i usuwać tylko swoich użytkowników
- Brak możliwości wyboru menedżera przez MANAGER

✅ **Bezpieczeństwo (PRD 3.6)**
- Implementacja role-based access control (RBAC)
- JWT authentication
- Wielowarstwowa ochrona API

## Pliki zmodyfikowane

### Backend
- `UserService.java` - dodano logikę filtrowania i walidacji uprawnień
- `UserRepository.java` - dodano metody zapytań dla filtrowania
- `UserController.java` - dodano adnotacje @PreAuthorize
- `GlobalExceptionHandler.java` - dodano handler AccessDeniedException

### Frontend
- `user-dialog.component.ts` - logika ukrywania pól dla menedżera
- `user-dialog.component.html` - warunkowe wyświetlanie pól
- `app.routes.ts` - dodano routing manager/users
- `dashboard.component.html` - dodano link nawigacyjny dla menedżera

## Podsumowanie

Implementacja zapewnia bezpieczne i zgodne z wymaganiami zarządzanie użytkownikami przez menedżerów, przy zachowaniu pełnej kontroli administratorów. System wykorzystuje wielowarstwową ochronę (Spring Security, @PreAuthorize, logika biznesowa) i automatyczne filtrowanie danych na podstawie kontekstu bezpieczeństwa użytkownika.
