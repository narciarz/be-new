# API Endpoint Implementation Plan: Users Endpoint

## 1. Przegląd punktu końcowego

Endpointy służą do zarządzania użytkownikami. Umożliwiają pobieranie listy użytkowników (GET /users), pobieranie szczegółowych danych pojedynczego użytkownika (GET /users/{userId}), tworzenie nowego użytkownika (POST /users), aktualizację danych (PUT /users/{userId}) oraz usuwanie/archiwizację użytkownika (DELETE /users/{userId}).

## 2. Szczegóły żądania

- **Metody HTTP**:
- GET: Pobranie listy lub pojedynczego użytkownika
- POST: Utworzenie nowego użytkownika
- PUT: Aktualizacja istniejącego użytkownika
- DELETE: Usunięcie (lub archiwizacja) użytkownika

- **Struktura URL**:
- Lista: `/users`
- Szczegóły: `/users/{userId}`

- **Parametry**:
- **GET /users**:
 - Wymagane: Brak (paginuje się domyślnie, ale page, size, sort oraz filter jako opcjonalne parametry)
- **GET /users/{userId}**:
 - Wymagane: userId w ścieżce
- **POST /users**:
 - Request Body (wymagane): email, password, firstName, lastName, positionName, role
 - Opcjonalne: managerId
- **PUT /users/{userId}**:
 - Request Body podobny do POST (pomijając password, jeśli nie jest zmieniany)
- **DELETE /users/{userId}**:
 - Wymagane: userId w ścieżce

## 3. Wykorzystywane typy

- **DTO**:
- `CreateUserRequestDto` – zawiera dane wejściowe przy tworzeniu użytkownika.
- `UpdateUserRequestDto` – dane do aktualizacji użytkownika.
- `UserResponseDto` – struktura danych zwracanych jako odpowiedź.

- **PoJo**:
- `AppUser` – encja reprezentująca użytkownika, odwzorowująca tabelę `app_user`.
- Ewentualnie dodatkowe klasy wspierające walidację lub mapowanie.

## 4. Szczegóły odpowiedzi

- **Kody statusu**:
- 200 OK – pomyślne pobranie danych lub aktualizacja
- 201 Created – pomyślne utworzenie użytkownika
- 204 No Content – pomyślne usunięcie/archiwizacja
- 400 Bad Request – nieprawidłowe dane wejściowe
- 401 Unauthorized – brak autoryzacji
- 404 Not Found – użytkownik nie istnieje
- 500 Internal Server Error – błąd serwera

- **Struktura odpowiedzi**:
- Dla GET i POST: JSON z danymi użytkownika zgodnymi z `UserResponseDto`.

## 5. Przepływ danych

1. Klient wysyła żądanie HTTP do odpowiedniego endpointu.
2. Kontroler (np. `UserController`) przyjmuje żądanie i wykonuje wstępną walidację.
3. Dane z żądania mapowane są do obiektu DTO (np. `CreateUserRequestDto` lub `UpdateUserRequestDto`).
4. Kontroler wywołuje metodę w warstwie serwisowej (np. `UserService`), która:

- Waliduje dane (np. format emaila, istnienie managerId, unikalność emaila)
- Interaguje z repozytorium (np. `UserRepository`) w celu odczytu/zapisu danych w bazie

5. Wynik operacji mapowany jest na `UserResponseDto` i zwracany jako odpowiedź.

## 6. Względy bezpieczeństwa

- Uwierzytelnianie JWT: Wszystkie endpointy z wyjątkiem logowania powinny sprawdzać poprawność tokena.
- Role-based access control: Endpointy zarządzania użytkownikami dostępne tylko dla odpowiednich ról (np. Admin, Manager).
- Walidacja danych wejściowych: Dokładna walidacja payloadu (np. sprawdzanie formatu emaila) już na poziomie kontrolera lub za pomocą anotacji.

## 7. Obsługa błędów

- Walidacja wejścia: Zwracanie 400 Bad Request z informacjami o błędach walidacji.
- Brak użytkownika: Zwracanie 404 Not Found, gdy żądany użytkownik nie istnieje.
- Błędy serwera: Logowanie wyjątków oraz zwracanie 500 Internal Server Error w razie niespodziewanych problemów.
- Autoryzacja: Jeśli token nie jest ważny lub użytkownik nie ma odpowiednich uprawnień, zwracanie 401 Unauthorized.

## 8. Rozważania dotyczące wydajności

- Paginacja: Zastosowanie paginacji, aby zmniejszyć obciążenie przy pobieraniu listy użytkowników.
- Optymalizacja zapytań: Użycie odpowiednich indeksów w tabeli `app_user` oraz unikanie nadmiernych joinów.
- Cache'owanie: Rozważenie zastosowania cache'owania dla endpointu GET /users, jeśli wystąpi taka potrzeba.

## 9. Etapy wdrożenia

1. Utworzenie lub aktualizacja klasy kontrolera (`UserController`) z odpowiednimi endpointami.
2. Implementacja DTOs oraz mapowania pomiędzy warstwą kontrolera i serwisową.
3. Rozbudowa warstwy serwisowej (`UserService`) o logikę walidacji, obsługi operacji CRUD oraz interakcji z `UserRepository`.
4. Zaimplementowanie lub aktualizacja konfiguracji bezpieczeństwa (JWT, role-based access) w Spring Security.
5. Dodanie mechanizmu walidacji, logowania błędów oraz obsługi wyjątków.
6. Testowanie jednostkowe i integracyjne endpointów.
7. Przegląd kodu oraz wdrożenie zgodnie z najlepszymi praktykami.