# API Endpoint Implementation Plan: Authentication Endpoint (Login)

## 1. Przegląd punktu końcowego

Endpoint służy do uwierzytelniania użytkowników w systemie. Użytkownik wysyła swoje dane logowania (email i hasło), a w odpowiedzi otrzymuje token JWT oraz szczegóły dotyczące roli użytkownika. Endpoint ten stanowi kluczowy element systemu, gdyż umożliwia dostęp do chronionych zasobów w dalszym korzystaniu z aplikacji.

## 2. Szczegóły żądania

- **Metoda HTTP**: POST
- **Struktura URL**: /auth/login
- **Parametry**: 
  - **Brak parametrów query**
- **Request Body**:
  - Wymagane pola:
    - `email`: String – adres email użytkownika
    - `password`: String – hasło użytkownika

Przykładowa struktura żądania:

```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

## 3. Wykorzystywane typy

- **DTO**:
  - `LoginRequestDto` – zawiera pola `email` oraz `password` i ewentualne walidacje (np. @Email, @NotBlank).
  - `LoginResponseDto` – zawiera token JWT, informacje o roli użytkownika i ewentualnie dodatkowe dane (np. czas ważności tokenu).
- **PoJo**:
  - Encja użytkownika (`AppUser`) – wykorzystywana do weryfikacji danych logowania oraz pobrania szczegółów użytkownika.

## 4. Szczegóły odpowiedzi

- **Sukces**:
  - Kod statusu: 200 OK
  - Treść odpowiedzi: JSON zawierający token JWT oraz szczegóły dotyczące roli użytkownika, np.

  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "role": "USER"
  }
  ```

- **Błędy**:
  - 400 Bad Request – przy nieprawidłowym formacie danych wejściowych (np. brak wymaganych pól, niepoprawny format email).
  - 401 Unauthorized – gdy podane dane logowania są niepoprawne lub użytkownik nie istnieje.
  - 500 Internal Server Error – w przypadku błędów po stronie serwera.

## 5. Przepływ danych

1. Klient wysyła żądanie POST do `/auth/login` z danymi logowania.
2. Warstwa kontrolera (AuthController) przyjmuje żądanie i przekazuje dane do warstwy serwisowej (AuthenticationService).
3. W serwisie następuje weryfikacja danych logowania: 
   - Sprawdzenie istnienia użytkownika w bazie danych (za pomocą repozytorium, np. `UserRepository`).
   - Weryfikacja poprawności hasła (np. przy użyciu BCrypt lub innej funkcji haszującej).
4. W przypadku poprawnych danych serwis generuje token JWT (przy użyciu biblioteki JWT, np. JJWT) i tworzy odpowiedź.
5. Endpunkt zwraca odpowiedź JSON z tokenem i informacjami o roli użytkownika.

## 6. Względy bezpieczeństwa

- Dane wrażliwe (np. hasło) nie są logowane.
- Token JWT powinien mieć określony czas ważności i być podpisany przy użyciu bezpiecznego algorytmu.
- Endpoint powinien być zabezpieczony przed atakami typu brute force (np. poprzez rate limiting).
- Stosowanie HTTPS do przesyłania danych.
- Walidacja danych wejściowych za pomocą adnotacji Bean Validation (@NotBlank, @Email).

## 7. Obsługa błędów

- Walidacja danych wejściowych – nieprawidłowe lub brakujące wartości zwrócą kod 400 z odpowiednim komunikatem.
- Nieprawidłowe dane logowania – zwrócenie kodu 401 Unauthorized z informacją o błędnych danych.
- Globalna obsługa wyjątków przy użyciu `@ControllerAdvice` – wszystkie nieprzewidziane błędy będą mapowane na odpowiedź 500 Internal Server Error z ujednoliconym formatem błędu (ErrorResponseDto).

## 8. Rozważania dotyczące wydajności

- Minimalizacja nakładu czasu po stronie serwera dzięki wykorzystaniu lekkich operacji na bazie danych (optymalne zapytania).
- Możliwość zastosowania cache’owania weryfikacji tokenów, jeżeli system będzie mocno obciążony.
- Użycie asynchronicznych mechanizmów w razie potrzeby skalowania w przyszłości.

## 9. Etapy wdrożenia

1. **Opracowanie DTO i encji**:
   - Utworzenie `LoginRequestDto` oraz `LoginResponseDto` (lub weryfikacja istniejących).
   - Weryfikacja, czy istnieje encja `AppUser` i repozytorium do pobierania danych.

2. **Implementacja kontrolera**:
   - Utworzenie lub rozszerzenie `AuthController` z metodą `login()` obsługującą żądania POST do `/auth/login`.
   - Dodanie walidacji danych wejściowych (@Valid) w kontrolerze.

3. **Implementacja serwisu uwierzytelniania**:
   - Implementacja metody w `AuthenticationService` do weryfikacji danych logowania.
   - Generowanie i podpisywanie tokena JWT.

4. **Integracja z warstwą bezpieczeństwa**:
   - Skonfigurowanie Spring Security, aby umożliwić dostęp do endpointu `/auth/login` bez uwierzytelnienia.
   - Upewnienie się, że pozostałe endpointy są zabezpieczone wymagając autoryzacji.

5. **Obsługa wyjątków**:
   - Implementacja globalnego exception handlera (@ControllerAdvice) do obsługi błędów i mapowania na odpowiednie kody statusu.

6. **Testy**:
   - Testy jednostkowe kontrolera i serwisu (z użyciem @WebMvcTest i @MockBean dla zależności).
   - Testy integracyjne z użyciem in-memory database, aby zweryfikować pełen przepływ logowania.

7. **Dokumentacja i code review**:
   - Aktualizacja dokumentacji API oraz przeprowadzenie code review przed wdrożeniem.

