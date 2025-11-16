# API Endpoint Implementation Plan: Onboarding Tasks Endpoint

## 1. Przegląd punktu końcowego
Endpoint obsługuje operacje na zadaniach przypisanych do danego procesu onboardingu. Umożliwia pobieranie listy zadań, pobieranie szczegółów pojedynczego zadania oraz aktualizację zadania (np. oznaczenie go jako ukończone).

## 2. Szczegóły żądania

### 2.1. GET /onboarding/{processId}/tasks
- **Metoda HTTP**: GET
- **Struktura URL**: `/onboarding/{processId}/tasks`
- **Parametry ścieżkowe**:
  - `processId` (wymagany): identyfikator procesu onboardingu
- **Ciało żądania**: Brak

### 2.2. GET /onboarding/{processId}/tasks/{taskId}
- **Metoda HTTP**: GET
- **Struktura URL**: `/onboarding/{processId}/tasks/{taskId}`
- **Parametry ścieżkowe**:
  - `processId` (wymagany): identyfikator procesu onboardingu
  - `taskId` (wymagany): identyfikator zadania
- **Ciało żądania**: Brak

### 2.3. PUT /onboarding/{processId}/tasks/{taskId}
- **Metoda HTTP**: PUT
- **Struktura URL**: `/onboarding/{processId}/tasks/{taskId}`
- **Parametry ścieżkowe**:
  - `processId` (wymagany): identyfikator procesu onboardingu
  - `taskId` (wymagany): identyfikator zadania
- **Ciało żądania (JSON)**:
  ```json
  { "isCompleted": true }
  ```

## 3. Wykorzystywane typy
- **DTOs**:
  - `OnboardingTaskResponseDto` – reprezentuje odpowiedź pojedynczego zadania
  - `UpdateOnboardingTaskRequestDto` – reprezentuje dane wejściowe dla aktualizacji zadania (np. pole `isCompleted`)
- **Encje/PoJo**:
  - `OnboardingTask` – encja zadania w procesie onboardingu

## 4. Szczegóły odpowiedzi

### GET /onboarding/{processId}/tasks
- **Odpowiedź**: JSON Array obiektów `OnboardingTaskResponseDto`
- **Kod statusu**: 200 OK
- **Błędy**: 404 Not Found, jeżeli proces o podanym identyfikatorze nie istnieje

### GET /onboarding/{processId}/tasks/{taskId}
- **Odpowiedź**: Obiekt JSON `OnboardingTaskResponseDto` zawierający szczegóły zadania
- **Kod statusu**: 200 OK
- **Błędy**: 404 Not Found, jeśli zadanie lub proces nie istnieje

### PUT /onboarding/{processId}/tasks/{taskId}
- **Odpowiedź**: Zaktualizowany obiekt `OnboardingTaskResponseDto`
- **Kod statusu**: 200 OK
- **Błędy**: 400 Bad Request (dla nieprawidłowych danych wejściowych) lub 404 Not Found (gdy zadanie/proces nie istnieje)

## 5. Przepływ danych
1. Klient wysyła żądanie z odpowiednimi nagłówkami autoryzacji.
2. Kontroler odbiera żądanie, waliduje dane wejściowe przy użyciu adnotacji @Valid.
3. Żądanie kierowane jest do warstwy serwisowej odpowiedzialnej za logikę zadaniową (np. `OnboardingTaskService`).
4. Serwis wykonuje operacje na repozytorium JPA:
   - Pobiera listę zadań lub pojedyncze zadanie
   - Aktualizuje status zadania (np. `isCompleted`)
5. Wyniki operacji są mapowane na odpowiednie DTO i zwracane przez kontroler.

## 6. Względy bezpieczeństwa
- Uwierzytelnienie żądań poprzez JWT, z obowiązkowym nagłówkiem `Authorization: Bearer <token>`
- Autoryzacja na poziomie kontrolera z wykorzystaniem Spring Security – dostęp do endpointów ograniczony do użytkowników z odpowiednimi rolami (np. Admin, Manager, User odpowiedzialny za swój proces onboardingu)
- Walidacja danych wejściowych przy użyciu adnotacji Bean Validation (@NotNull, @Valid) dla DTO.

## 7. Obsługa błędów
- Globalna obsługa wyjątków poprzez @ControllerAdvice, która przekłada wyjątki na jednolity format błędu (np. `ErrorResponseDto`)
- Rejestrowanie błędów za pomocą SLF4J
- Mapowanie wyjątków na odpowiednie kody statusu:
  - 400 Bad Request – dla błędnych danych wejściowych
  - 401 Unauthorized – gdy brak poprawnej autoryzacji
  - 404 Not Found – gdy nie znaleziono zasobu (procesu lub zadania)
  - 500 Internal Server Error – dla nieoczekiwanych błędów

## 8. Rozważania dotyczące wydajności
- Zastosowanie paginacji przy pobieraniu listy zadań, aby ograniczyć rozmiar odpowiedzi w przypadku dużej liczby zadań
- Wykorzystanie optymalizacji zapytań w repozytoriach JPA (np. @EntityGraph lub fetch join) w celu minimalizacji liczby zapytań do bazy danych
- Możliwość wdrożenia cache’u dla często pobieranych danych

## 9. Etapy wdrożenia
1. **Definicja DTO i encji**:
   - Upewnić się, że istnieją DTO: `OnboardingTaskResponseDto` oraz `UpdateOnboardingTaskRequestDto`.
   - Sprawdzić definicję encji `OnboardingTask` w kontekście relacji z `OnboardingProcess`.

2. **Implementacja serwisu**:
   - Utworzyć lub zaktualizować serwis (np. `OnboardingTaskService`) z metodami do:
     - Pobierania listy zadań dla danego procesu
     - Pobierania szczegółów pojedynczego zadania
     - Aktualizacji zadania (np. zmiana pola `isCompleted`)

3. **Implementacja kontrolera**:
   - Utworzyć lub zaktualizować kontroler REST (np. `OnboardingTaskController`), który będzie mapował żądania do serwisu
   - Używać @Valid przy przyjmowaniu danych wejściowych w metodach PUT

4. **Integracja z bazą danych**:
   - Zaktualizować repozytorium JPA dla `OnboardingTask` jeśli to konieczne
   - Testować zapytania pod kątem wydajności oraz liczby wykonanych zapytań

5. **Bezpieczeństwo**:
   - Konfiguracja Spring Security, aby dostęp do endpointów mieli wyłącznie autoryzowani użytkownicy

6. **Obsługa błędów i logowanie**:
   - Uzupełnić globalny handler wyjątków (@ControllerAdvice) o obsługę przypadków związanych z zadaniami onboardingu
   - Zapewnić rejestrowanie błędów przy każdej operacji

7. **Testowanie**:
   - Napisać testy jednostkowe i integracyjne z wykorzystaniem Spring Boot slice testing (np. @WebMvcTest dla kontrolera, @DataJpaTest dla repozytorium)
   - Przeprowadzić testy API oraz negative test cases (np. aktualizacja nieistniejącego zadania, błędne dane wejściowe)

8. **Dokumentacja i code review**:
   - Zaktualizować dokumentację API (Swagger/OpenAPI)
   - Przeprowadzić przegląd kodu oraz testy funkcjonalne przed wdrożeniem do środowiska produkcyjnego
