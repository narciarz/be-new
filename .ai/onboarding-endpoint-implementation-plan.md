# API Endpoint Implementation Plan: Onboarding Endpoint

## 1. Przegląd punktu końcowego
Endpoint umożliwia zarządzanie procesami onboardingu. Obsługuje:
- Pobieranie listy istniejących procesów onboardingu.
- Pobieranie szczegółów pojedynczego procesu.
- Tworzenie nowego procesu onboardingu (automatyczne kopiowanie zadań z szablonu).
- Aktualizację statusu oraz liczników zadań (np. archiwizacja procesu).
- Usuwanie (archiwizacja) procesu.

## 2. Szczegóły żądania

### 2.1. GET /onboarding
- **Metoda HTTP**: GET
- **Struktura URL**: `/onboarding`
- **Parametry zapytania**: (opcjonalnie) filtry, paginacja (np. page, size) oraz sortowanie
- **Ciało żądania**: Brak

### 2.2. GET /onboarding/{processId}
- **Metoda HTTP**: GET
- **Struktura URL**: `/onboarding/{processId}`
- **Parametry ścieżkowe**: 
  - `processId` (wymagany) – identyfikator procesu onboardingu
- **Ciało żądania**: Brak

### 2.3. POST /onboarding
- **Metoda HTTP**: POST
- **Struktura URL**: `/onboarding`
- **Parametry**: Brak
- **Ciało żądania (JSON)**:
  - `userId` (wymagany): UUID użytkownika
  - `managerId` (wymagany): UUID menedżera
  - `sourceTemplateId` (wymagany): UUID szablonu, z którego kopiowane są zadania

### 2.4. PUT /onboarding/{processId}
- **Metoda HTTP**: PUT
- **Struktura URL**: `/onboarding/{processId}`
- **Parametry ścieżkowe**:
  - `processId` (wymagany): identyfikator procesu do aktualizacji
- **Ciało żądania (JSON)**:
  - `status` (opcjonalny): np. "ARCHIVED"
  - `totalTasksCount` (opcjonalny): liczba wszystkich zadań
  - `completedTasksCount` (opcjonalny): liczba zakończonych zadań

### 2.5. DELETE /onboarding/{processId}
- **Metoda HTTP**: DELETE
- **Struktura URL**: `/onboarding/{processId}`
- **Parametry ścieżkowe**:
  - `processId` (wymagany): identyfikator procesu
- **Ciało żądania**: Brak

## 3. Wykorzystywane typy
- **DTOs**:
  - `CreateOnboardingProcessRequestDto` – reprezentujący dane wejściowe dla POST
  - `OnboardingProcessResponseDto` – reprezentujący dane wyjściowe dla GET, POST, PUT
  - `UpdateOnboardingProcessRequestDto` – dla aktualizacji przez PUT
- **Encje/PoJo**:
  - `OnboardingProcess` – encja odzwierciedlająca proces onboardingu w bazie danych

## 4. Szczegóły odpowiedzi
- **GET /onboarding**
  - Odpowiedź: JSON Array procesów onboardingu
  - Kod statusu: 200 OK
- **GET /onboarding/{processId}**
  - Odpowiedź: Obiekt JSON ze szczegółami procesu
  - Kod statusu: 200 OK, lub 404 Not Found gdy nie istnieje
- **POST /onboarding**
  - Odpowiedź: Obiekt JSON reprezentujący utworzony proces
  - Kod statusu: 201 Created, lub 400 Bad Request dla błędów walidacji
- **PUT /onboarding/{processId}**
  - Odpowiedź: Zaktualizowany obiekt procesu
  - Kod statusu: 200 OK, lub 400/404 dla błędów walidacji lub nieistniejącego zasobu
- **DELETE /onboarding/{processId}**
  - Odpowiedź: Brak treści
  - Kod statusu: 204 No Content, lub 400/404 dla nieprawidłowej operacji

## 5. Przepływ danych
1. Klient wysyła żądanie HTTP (wraz z odpowiednimi nagłówkami autoryzacji).
2. Kontroler (REST Controller) przyjmuje i waliduje dane wejściowe (używając @Valid).
3. Dane przekazywane są do warstwy service, gdzie zachodzi:
   - Walidacja logiki biznesowej.
   - Dla POST: kopiowanie zadań z szablonu (pobieranie danych z repozytorium szablonów).
4. Serwis korzysta z repozytoriów Spring Data JPA do operacji na bazie danych.
5. Wynik operacji jest mapowany do odpowiedniego DTO i zwracany do kontrolera.
6. Kontroler zwraca poprawną odpowiedź HTTP z odpowiednim kodem.

## 6. Względy bezpieczeństwa
- Uwierzytelnienie żądań za pomocą JWT, gdzie każdy request musi zawierać nagłówek `Authorization: Bearer <token>`.
- Endpointy zabezpieczone na poziomie kontrolera poprzez mechanizmy Spring Security, przydzielając dostęp do endpointów jedynie dla uprawnionych ról (np. Admin, Manager).
- Walidacja danych wejściowych, by zapobiegać atakom takie jak SQL Injection oraz XSS.

## 7. Obsługa błędów
- Centralna obsługa wyjątków przy użyciu @ControllerAdvice.
- Zwracanie jednolitego formatu odpowiedzi błędu przy użyciu `ErrorResponseDto`.
- Logowanie błędów za pomocą SLF4J.
- Obsługa specyficznych scenariuszy:
  - Błędne dane wejściowe (400 Bad Request)
  - Nieautoryzowany dostęp (401 Unauthorized)
  - Brak zasobu (404 Not Found)
  - Błąd serwera (500 Internal Server Error)

## 8. Rozważania dotyczące wydajności
- Zastosowanie paginacji przy pobieraniu listy procesów, aby zapobiec problemom przy dużych zbiorach danych.
- Optymalizacja zapytań do bazy danych (użycie fetch join lub @EntityGraph).
- Implementacja cache’u dla często pobieranych danych jeżeli to zastosowalne.

## 9. Etapy wdrożenia
1. **Definicja DTO i encji**:
   - Utworzyć lub zmodyfikować DTO: `CreateOnboardingProcessRequestDto`, `OnboardingProcessResponseDto`, `UpdateOnboardingProcessRequestDto`.
   - Zapewnić, że encja `OnboardingProcess` zawiera niezbędne pola, w tym status oraz liczniki zadań.

2. **Implementacja warstwy serwisowej**:
   - Stworzyć/aktualizować `OnboardingService` z metodami:
     - `List<OnboardingProcessResponseDto> getAllProcesses(...)`
     - `OnboardingProcessResponseDto getProcessById(UUID processId)`
     - `OnboardingProcessResponseDto createProcess(CreateOnboardingProcessRequestDto request)`
     - `OnboardingProcessResponseDto updateProcess(UUID processId, UpdateOnboardingProcessRequestDto request)`
     - `void deleteProcess(UUID processId)`
   - Zaimplementować logikę kopiowania zadań z szablonu przy tworzeniu procesu nowego użytkownika.

3. **Implementacja warstwy kontrolera**:
   - Utworzyć kontroler REST (`OnboardingController`), który będzie mapował endpointy zgodnie z powyższą specyfikacją.
   - Upewnić się, że używane są adnotacje @Valid do walidacji danych wejściowych.

4. **Integracja z bazą danych**:
   - Aktualizacja repozytorium JPA dla encji `OnboardingProcess`.
   - Zaimplementować metody repozytorium wymagane przez serwis.

5. **Bezpieczeństwo**:
   - Skonfigurować Spring Security, aby endpointy były zabezpieczone za pomocą JWT.
   - Pobierać token z nagłówków i walidować autoryzację.

6. **Obsługa błędów**:
   - Utworzyć globalny handler wyjątków (@ControllerAdvice) do obsługi błędów walidacji i wyjątków biznesowych.
   - Zmapować odpowiednie wyjątki na kody błędów 400, 404, 500 itd.

7. **Testowanie**:
   - Napisać testy jednostkowe (np. przy użyciu Spring Boot slice testing – @WebMvcTest do kontrolera, @DataJpaTest dla repozytoriów).
   - Zaimplementować testy integracyjne sprawdzające pełen przepływ danych (od kontrolera, przez serwis, do bazy danych).

8. **Dokumentacja oraz review**:
   - Zaktualizować dokumentację API (np. Swagger/OpenAPI).
   - Przeprowadzić code review oraz testy funkcjonalne przed wdrożeniem do środowiska produkcyjnego.
