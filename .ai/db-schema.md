# Schemat Bazy Danych PostgreSQL - Be New

## Przegląd

Schemat bazy danych dla aplikacji Be New - systemu zarządzania onboardingiem pracowników. Projekt oparty jest na PostgreSQL 16 i Spring Boot 3, z użyciem UUID jako kluczy głównych oraz strategią denormalizacji dla wersjonowania szablonów.

## Kluczowe Decyzje Projektowe

- **Klucze główne**: UUID generowane przez Spring Boot (`@GeneratedValue(strategy = GenerationType.UUID)`)
- **Bezpieczeństwo**: Brak Row-Level Security (RLS) - filtrowanie danych w logice aplikacji
- **Integralność**: Brak natywnych ENUM i CHECK constraints - walidacja w aplikacji Spring
- **Wersjonowanie**: Denormalizacja - kopiowanie zadań z szablonów do wdrożeń
- **Postęp**: Obliczany w aplikacji, przechowywany w zdenormalizowanych licznikach
- **Audyt**: Zarządzany przez Spring Data JPA Auditing
- **Usuwanie**: ON DELETE RESTRICT dla wszystkich kluczy obcych

---

## 1. Role Bazy Danych

```sql
-- Rola właściciela schematu (do migracji DDL)
CREATE ROLE app_owner WITH LOGIN PASSWORD 'secure_password_owner';

-- Rola aplikacji Spring Boot (tylko DML)
CREATE ROLE app_spring WITH LOGIN PASSWORD 'secure_password_app';

-- Tworzenie schematu
CREATE SCHEMA IF NOT EXISTS benew AUTHORIZATION app_owner;

-- Uprawnienia dla app_spring
GRANT USAGE ON SCHEMA benew TO app_spring;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA benew TO app_spring;
ALTER DEFAULT PRIVILEGES IN SCHEMA benew 
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_spring;
```

---

## 2. Tabele

### 2.1. app_user

Przechowuje wszystkich użytkowników systemu (Administrator, Menedżer, Pracownik). Hierarchia Menedżer-Pracownik jest reprezentowana przez rekurencyjny klucz obcy `manager_id`.

```sql
CREATE TABLE benew.app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    position_name VARCHAR(50),
    manager_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    
    CONSTRAINT fk_app_user_manager 
        FOREIGN KEY (manager_id) 
        REFERENCES benew.app_user(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT uk_app_user_email 
        UNIQUE (email)
);

COMMENT ON TABLE benew.app_user IS 
    'Przechowuje wszystkich użytkowników systemu z trzema rolami: ADMIN, MANAGER, USER';
COMMENT ON COLUMN benew.app_user.role IS 
    'Wartości: ADMIN, MANAGER, USER - walidowane w aplikacji Spring';
COMMENT ON COLUMN benew.app_user.manager_id IS 
    'Rekurencyjne odniesienie do menedżera użytkownika. NULL dla Administratorów i Menedżerów bez przełożonych';
COMMENT ON COLUMN benew.app_user.position_name IS 
    'Nazwa stanowiska - używana do przypisania odpowiedniego szablonu wdrożenia';
```

### 2.2. template

Nagłówek szablonu checklisty onboardingowej przypisanej do konkretnego stanowiska.

```sql
CREATE TABLE benew.template (
    id UUID PRIMARY KEY,
    position_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    
    CONSTRAINT uk_template_position_name 
        UNIQUE (position_name)
);

COMMENT ON TABLE benew.template IS 
    'Nagłówki szablonów checklist dla różnych stanowisk';
COMMENT ON COLUMN benew.template.position_name IS 
    'Unikalna nazwa stanowiska. Aplikacja normalizuje (trim + toLowerCase) przed zapisem';
```

### 2.3. template_task

Zadania wzorcowe wchodzące w skład szablonu checklisty.

```sql
CREATE TABLE benew.template_task (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    task_order INTEGER NOT NULL,
    owner_role VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    
    CONSTRAINT fk_template_task_template 
        FOREIGN KEY (template_id) 
        REFERENCES benew.template(id) 
        ON DELETE RESTRICT
);

COMMENT ON TABLE benew.template_task IS 
    'Zadania wzorcowe w szablonie. Kopiowane do onboarding_task przy tworzeniu wdrożenia';
COMMENT ON COLUMN benew.template_task.task_order IS 
    'Kolejność wyświetlania zadań w checkliście';
COMMENT ON COLUMN benew.template_task.owner_role IS 
    'Rola odpowiedzialna za wykonanie zadania: MANAGER lub USER';
```

### 2.4. onboarding_process

Reprezentuje aktywny lub zarchiwizowany proces wdrożenia pracownika.

```sql
CREATE TABLE benew.onboarding_process (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    manager_id UUID NOT NULL,
    source_template_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_tasks_count INTEGER NOT NULL DEFAULT 0,
    completed_tasks_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    
    CONSTRAINT fk_onboarding_process_user 
        FOREIGN KEY (user_id) 
        REFERENCES benew.app_user(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT fk_onboarding_process_manager 
        FOREIGN KEY (manager_id) 
        REFERENCES benew.app_user(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT fk_onboarding_process_template 
        FOREIGN KEY (source_template_id) 
        REFERENCES benew.template(id) 
        ON DELETE RESTRICT
);

COMMENT ON TABLE benew.onboarding_process IS 
    'Proces wdrożenia pracownika. Tworzone automatycznie przy zakładaniu konta użytkownika';
COMMENT ON COLUMN benew.onboarding_process.status IS 
    'Wartości: ACTIVE, ARCHIVED - walidowane w aplikacji Spring';
COMMENT ON COLUMN benew.onboarding_process.source_template_id IS 
    'Odniesienie do szablonu, z którego skopiowano zadania (dla celów audytowych)';
COMMENT ON COLUMN benew.onboarding_process.total_tasks_count IS 
    'Denormalizowana liczba wszystkich zadań - aktualizowana przez Spring';
COMMENT ON COLUMN benew.onboarding_process.completed_tasks_count IS 
    'Denormalizowana liczba ukończonych zadań - aktualizowana przez Spring';
```

### 2.5. onboarding_task

Skopiowane zadania dla konkretnego procesu wdrożenia.

```sql
CREATE TABLE benew.onboarding_task (
    id UUID PRIMARY KEY,
    onboarding_process_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    task_order INTEGER NOT NULL,
    owner_role VARCHAR(50) NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    
    CONSTRAINT fk_onboarding_task_process 
        FOREIGN KEY (onboarding_process_id) 
        REFERENCES benew.onboarding_process(id) 
        ON DELETE RESTRICT
);

COMMENT ON TABLE benew.onboarding_task IS 
    'Zadania skopiowane z template_task do konkretnego wdrożenia. Zmiany w szablonie nie wpływają na te zadania';
COMMENT ON COLUMN benew.onboarding_task.is_completed IS 
    'Status ukończenia zadania - ustawiany przez właściciela (Menedżer lub Pracownik)';
COMMENT ON COLUMN benew.onboarding_task.owner_role IS 
    'Rola odpowiedzialna za wykonanie zadania: MANAGER lub USER';
```

---

## 3. Indeksy

### 3.1. Indeksy na Kluczach Obcych (dla wydajności JOIN)

```sql
-- app_user
CREATE INDEX idx_app_user_manager_id 
    ON benew.app_user(manager_id) 
    WHERE manager_id IS NOT NULL;

-- template_task
CREATE INDEX idx_template_task_template_id 
    ON benew.template_task(template_id);

-- onboarding_process
CREATE INDEX idx_onboarding_process_user_id 
    ON benew.onboarding_process(user_id);

CREATE INDEX idx_onboarding_process_manager_id 
    ON benew.onboarding_process(manager_id);

CREATE INDEX idx_onboarding_process_template_id 
    ON benew.onboarding_process(source_template_id);

-- onboarding_task
CREATE INDEX idx_onboarding_task_process_id 
    ON benew.onboarding_task(onboarding_process_id);
```

### 3.2. Indeksy na Kolumnach Wyszukiwania

```sql
-- Wyszukiwanie użytkowników po email (case-insensitive)
CREATE UNIQUE INDEX idx_app_user_email_lower 
    ON benew.app_user(LOWER(email));

-- Wyszukiwanie użytkowników po nazwisku i imieniu
CREATE INDEX idx_app_user_last_name 
    ON benew.app_user(last_name);

CREATE INDEX idx_app_user_first_name 
    ON benew.app_user(first_name);

-- Wyszukiwanie użytkowników po stanowisku
CREATE INDEX idx_app_user_position_name 
    ON benew.app_user(position_name) 
    WHERE position_name IS NOT NULL;

-- Wyszukiwanie szablonów po stanowisku (case-insensitive)
CREATE UNIQUE INDEX idx_template_position_name_lower 
    ON benew.template(LOWER(position_name));

-- Filtrowanie procesów po statusie
CREATE INDEX idx_onboarding_process_status 
    ON benew.onboarding_process(status);
```

### 3.3. Indeksy Kompozytowe

```sql
-- Dashboard menedżera: lista aktywnych procesów z postępem
CREATE INDEX idx_onboarding_process_manager_status 
    ON benew.onboarding_process(manager_id, status);

-- Sortowanie zadań w checkliście
CREATE INDEX idx_template_task_template_order 
    ON benew.template_task(template_id, task_order);

CREATE INDEX idx_onboarding_task_process_order 
    ON benew.onboarding_task(onboarding_process_id, task_order);
```

---

## 4. Relacje Między Tabelami

### Diagram Relacji (ERD w notacji tekstowej)

```
app_user (1) ──< (N) app_user [manager_id]
    │                   │
    │ user_id           │ manager_id
    │                   │
    └──> (1) onboarding_process (N) <───┘
              │           │
              │           │ source_template_id
              │           │
              │           └──> (1) template
              │                     │
              │ onboarding_process_id   │ template_id
              │                     │
              └──> (N) onboarding_task   │
                                    └──> (N) template_task
```

### Kardynalność

| Relacja | Typ | Opis |
|---------|-----|------|
| app_user.manager_id → app_user.id | 1:N (opcjonalna) | Pracownik ma jednego Menedżera, Menedżer może mieć wielu Pracowników |
| onboarding_process.user_id → app_user.id | N:1 | Proces należy do jednego Użytkownika, Użytkownik może mieć wiele procesów |
| onboarding_process.manager_id → app_user.id | N:1 | Proces jest nadzorowany przez jednego Menedżera, Menedżer nadzoruje wiele procesów |
| onboarding_process.source_template_id → template.id | N:1 | Proces powstał z jednego Szablonu, Szablon może być źródłem wielu procesów |
| onboarding_task.onboarding_process_id → onboarding_process.id | N:1 | Zadanie należy do jednego Procesu, Proces zawiera wiele Zadań |
| template_task.template_id → template.id | N:1 | Zadanie wzorcowe należy do jednego Szablonu, Szablon zawiera wiele Zadań |

---

## 5. Polityki Bezpieczeństwa (Row-Level Security)

**Decyzja**: Całkowita rezygnacja z RLS dla MVP.

Cała odpowiedzialność za filtrowanie danych i izolację między użytkownikami spoczywa na logice aplikacji Spring Boot. Przykłady:

- Menedżer widzi tylko swoich pracowników: filtrowanie w zapytaniu JPA `WHERE manager_id = :currentUserId`
- Pracownik widzi tylko swoje zadania: filtrowanie w zapytaniu JPA `WHERE user_id = :currentUserId`
- Administrator ma dostęp do wszystkich danych

Strategia ta upraszcza implementację MVP i przenosi logikę autoryzacji do warstwy aplikacji, gdzie jest łatwiejsza do testowania i debugowania.

---

## 6. Dodatkowe Uwagi

### 6.1. Wersjonowanie Szablonów

Strategia denormalizacji:
1. Przy tworzeniu `onboarding_process`, aplikacja Spring kopiuje wszystkie zadania z `template_task` do `onboarding_task`
2. Kolumna `source_template_id` przechowuje odniesienie do szablonu źródłowego (tylko dla audytu)
3. Zmiany w `template` lub `template_task` **NIE wpływają** na istniejące `onboarding_process` i `onboarding_task`

### 6.2. Obliczanie Postępu

Aplikacja Spring jest odpowiedzialna za:
1. Aktualizację liczników `total_tasks_count` i `completed_tasks_count` w `onboarding_process`
2. Obliczanie procentu postępu: `(completed_tasks_count / total_tasks_count) * 100`
3. Aktualizację liczników przy każdej zmianie statusu zadania (`is_completed`)

Korzyści tej strategii:
- Szybki odczyt dashboardu menedżera (bez kosztownych COUNT())
- Logika w jednym miejscu (łatwiejsze testowanie)
- Brak triggerów PostgreSQL (prostszy schemat)

### 6.3. Normalizacja Danych

Aplikacja Spring jest odpowiedzialna za normalizację przed zapisem i wyszukiwaniem:

```java
// Przykład normalizacji
String normalizedPosition = positionName.trim().toLowerCase();
```

Dotyczy to pól:
- `app_user.email` (LOWER)
- `app_user.position_name` (LOWER)
- `template.position_name` (LOWER)

### 6.4. Audyt i Timestamps

Wszystkie tabele zawierają kolumny audytowe:
- `created_at TIMESTAMPTZ NOT NULL`
- `updated_at TIMESTAMPTZ NOT NULL`

Zarządzane przez Spring Data JPA:
```java
@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;
```

### 6.5. Strategia Usuwania

Wszystkie klucze obce używają `ON DELETE RESTRICT`. Aplikacja Spring musi obsłużyć logikę usuwania:

**Przykłady scenariuszy:**
- Usunięcie Menedżera z przypisanymi Pracownikami → **Błąd** (najpierw przepisać pracowników do innego menedżera)
- Usunięcie Użytkownika z aktywnym wdrożeniem → **Błąd** (najpierw zakończyć/zarchiwizować wdrożenie)
- Usunięcie Szablonu używanego w aktywnych wdrożeniach → **Błąd** (relacja `source_template_id`)
- Usunięcie Szablonu → **Musi najpierw usunąć** wszystkie `template_task` dla tego szablonu

### 6.6. Import CSV

Dla funkcji jednorazowego importu szablonów z CSV, aplikacja Spring:
1. Parsuje plik CSV
2. Waliduje dane (tytuł zadania, opis, kolejność, owner_role)
3. Tworzy transakcję obejmującą:
   - `INSERT INTO template`
   - `INSERT INTO template_task` (dla każdego wiersza)
4. W przypadku błędu - rollback całej transakcji

### 6.7. Wydajność dla Dużej Liczby Użytkowników

Schemat jest zaprojektowany z myślą o skalowalności:
- **UUID**: rozproszone klucze (brak wąskich gardeł na sekwencjach)
- **Indeksy B-tree**: wszystkie klucze obce i kolumny wyszukiwania
- **Denormalizacja liczników**: brak kosztownych agregacji przy odczycie
- **Partycjonowanie** (przyszłość): `onboarding_task` i `onboarding_process` mogą być partycjonowane po `created_at` gdy dane rosną

### 6.8. Migracje Schematu

Zalecane narzędzie: **Flyway** lub **Liquibase**

Struktura migracji:
```
src/main/resources/db/migration/
├── V1__create_schema_and_roles.sql
├── V2__create_app_user_table.sql
├── V3__create_template_tables.sql
├── V4__create_onboarding_tables.sql
├── V5__create_indexes.sql
└── V6__seed_admin_user.sql
```

---

## 7. Inicjalizacja Danych (Seed)

### 7.1. Pierwszy Administrator

```sql
-- UUID musi być wygenerowany przez aplikację lub narzędzie
INSERT INTO benew.app_user (
    id, 
    email, 
    password_hash, 
    role, 
    first_name, 
    last_name, 
    position_name, 
    manager_id,
    created_at,
    updated_at
) VALUES (
    'uuid-generated-by-app',
    'admin@benew.com',
    '$2a$10$...', -- BCrypt hash
    'ADMIN',
    'System',
    'Administrator',
    NULL,
    NULL,
    NOW(),
    NOW()
);
```

---

## 8. Przykładowe Zapytania

### 8.1. Dashboard Menedżera - Lista Pracowników z Postępem

```sql
SELECT 
    u.id,
    u.first_name,
    u.last_name,
    u.position_name,
    p.status,
    p.completed_tasks_count,
    p.total_tasks_count,
    ROUND((p.completed_tasks_count::NUMERIC / NULLIF(p.total_tasks_count, 0)) * 100, 2) AS progress_percentage,
    p.created_at AS onboarding_started_at
FROM benew.onboarding_process p
JOIN benew.app_user u ON p.user_id = u.id
WHERE p.manager_id = :currentManagerId
  AND p.status = 'ACTIVE'
ORDER BY p.created_at DESC;
```

### 8.2. Szczegóły Checklisty Pracownika

```sql
SELECT 
    t.id,
    t.title,
    t.description,
    t.task_order,
    t.owner_role,
    t.is_completed
FROM benew.onboarding_task t
WHERE t.onboarding_process_id = :processId
ORDER BY t.task_order ASC;
```

### 8.3. Wyszukiwanie Szablonu po Stanowisku

```sql
SELECT 
    t.id,
    t.position_name,
    COUNT(tt.id) AS task_count
FROM benew.template t
LEFT JOIN benew.template_task tt ON t.id = tt.template_id
WHERE LOWER(t.position_name) = LOWER(:positionName)
GROUP BY t.id, t.position_name;
```

### 8.4. Sprawdzenie, czy Menedżer może być usunięty

```sql
-- Sprawdź czy ma przypisanych pracowników
SELECT COUNT(*) 
FROM benew.app_user 
WHERE manager_id = :managerId;

-- Sprawdź czy nadzoruje aktywne wdrożenia
SELECT COUNT(*) 
FROM benew.onboarding_process 
WHERE manager_id = :managerId 
  AND status = 'ACTIVE';
```

---

## 9. Podsumowanie Kluczowych Liczb

| Metryka | Wartość | Uwagi |
|---------|---------|-------|
| Liczba tabel | 5 | app_user, template, template_task, onboarding_process, onboarding_task |
| Liczba kluczy obcych | 6 | Wszystkie z ON DELETE RESTRICT |
| Liczba indeksów | 16 | FK (6) + wyszukiwanie (7) + kompozytowe (3) |
| Liczba ograniczeń UNIQUE | 3 | email, email_lower, position_name_lower |
| Maksymalna długość VARCHAR | 255 | Dla title i email |
| Typ kluczy głównych | UUID | Generowane przez Spring |

---

## 10. Checklist Implementacji

- [ ] Utworzenie ról PostgreSQL (`app_owner`, `app_spring`)
- [ ] Utworzenie schematu `benew`
- [ ] Przydzielenie uprawnień rolom
- [ ] Migracja V1: Tabela `app_user`
- [ ] Migracja V2: Tabele `template` i `template_task`
- [ ] Migracja V3: Tabele `onboarding_process` i `onboarding_task`
- [ ] Migracja V4: Wszystkie indeksy
- [ ] Migracja V5: Seed pierwszego administratora
- [ ] Konfiguracja Spring Data JPA Auditing
- [ ] Utworzenie encji JPA z adnotacjami
- [ ] Implementacja repozytoriów Spring Data
- [ ] Testy integracyjne schematu
- [ ] Dokumentacja API dla frontendu

---

**Data utworzenia**: 2025-11-03  
**Wersja**: 1.0  
**Status**: Gotowy do implementacji


