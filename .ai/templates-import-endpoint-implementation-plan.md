# API Endpoint Implementation Plan: Import CSV Template Endpoint

## 1. Przegląd punktu końcowego
Endpoint służy do importu szablonu checklisty wraz z zadaniami poprzez przesłanie pliku CSV. Działanie to ma na celu umożliwienie administratorom szybkiego dodania nowego szablonu oraz powiązanych z nim zadań, korzystając z pliku CSV. Endpoint musi zapewnić walidację struktury pliku, autoryzację (tylko dla Adminów) oraz kompleksową obsługę błędów.

## 2. Szczegóły żądania

- **Metoda HTTP:** POST
- **Struktura URL:** /templates/import
- **Nagłówki:**
  - Content-Type: multipart/form-data
  - Authorization: Bearer [JWT] (wymagany dla administratora)
- **Parametry:**
  - **Wymagane:**
    - CSV file – plik przesłany w części multipart jako plik do importu.
  - **Opcjonalne:**
    - (Brak dodatkowych parametrów - cały payload stanowi plik CSV)
- **Request Body:** Multipart/form-data zawierający plik CSV, który powinien odpowiadać ustalonemu formatowi (np. kolumny: tytuł zadania, opis, kolejność zadania, rola właściciela).

## 3. Wykorzystywane typy

- **DTO:**
  - `TemplateResponseDto` – reprezentuje szablon checklisty.
  - `TemplateTaskResponseDto` – reprezentuje zadania powiązane ze szablonem.
  - `ErrorResponseDto` – struktura odpowiedzi w przypadku wystąpienia błędów.
- **PoJo:**
  - `Template` – encja reprezentująca szablon w bazie danych.
  - `TemplateTask` – encja reprezentująca pojedyncze zadanie powiązane z szablonem.

## 4. Przepływ danych

1. Klient wysyła żądanie POST do endpointu `/templates/import` z plikiem CSV.
2. Warstwa kontrolera odbiera żądanie, weryfikuje czy użytkownik posiada rolę Admin i waliduje plik (m.in. typ MIME, niepusty plik).
3. Kontroler deleguje przetwarzanie pliku do warstwy serwisowej.
4. Warstwa serwisowa:
   - Parsuje plik CSV przy użyciu biblioteki (np. OpenCSV lub podobnej).
   - Weryfikuje poprawność danych (sprawdzanie wymaganych kolumn, poprawność typów danych).
   - Tworzy nowy szablon (obiektu `Template`) oraz powiązane obiekty `TemplateTask`.
   - Zapisuje dane do bazy korzystając z repozytoriów JPA.
5. Po poprawnym przetworzeniu, serwis zwraca DTO z podsumowaniem importu (np. liczba dodanych zadań, identyfikator utworzonego szablonu).
6. Kontroler zwraca odpowiedź HTTP z kodem 200/201 i strukturą JSON zawierającą wyniki importu.

## 5. Względy bezpieczeństwa

- **Autoryzacja i uwierzytelnianie:** Endpoint powinien być dostępny tylko dla użytkowników z rolą Admin. Uwierzytelnianie będzie realizowane za pomocą JWT (token przesyłany w nagłówku Authorization).
- **Walidacja danych:** Walidacja pliku CSV (rozmiar, typ, struktura) przed rozpoczęciem operacji.
- **Ochrona przed atakami:** Weryfikacja rozmiaru przesyłanego pliku oraz ograniczenie liczby rekordów do przetworzenia aby uniknąć nadmiernego obciążenia serwera.

## 6. Obsługa błędów

- **400 Bad Request:** W przypadku gdy:
  - Plik nie został przesłany lub nie jest w formacie CSV.
  - CSV posiada nieprawidłowy format (brak wymaganych kolumn, błędne typy danych).
- **401 Unauthorized:** Gdy użytkownik nie posiada autoryzacji (brak tokenu lub niewłaściwa rola).
- **500 Internal Server Error:** W przypadku niespodziewanego błędu po stronie serwera, np. błędy parsowania lub zapisu do bazy.
- **Rejestracja błędów:** Błędy krytyczne powinny być logowane z wykorzystaniem SLF4J oraz przekazywane do systemu monitorowania.

## 7. Rozważania dotyczące wydajności

- **Parsowanie CSV:** Przy dużych plikach warto rozważyć strumieniowe przetwarzanie danych, aby nie przeciążać pamięci.
- **Operacje bazodanowe:** Użycie transakcji (@Transactional) dla grupowego zapisu szablonu i zadań, aby zachować spójność danych.
- **Optymalizacja:** Asynchroniczny import lub przetwarzanie wsadowe w przypadku bardzo dużej liczby rekordów.

## 8. Etapy wdrożenia

1. **Utworzenie kontrolera:**
   - Dodanie nowej metody w `TemplateController` z adnotacją `@PostMapping(path = "/templates/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)`.
   - Weryfikacja uprawnień na poziomie kontrolera (np. poprzez mechanizm Spring Security).

2. **Implementacja walidacji żądania:**
   - Sprawdzenie obecności pliku.
   - Weryfikacja typu pliku oraz podstawowa walidacja zawartości.

3. **Implementacja warstwy serwisowej:**
   - Utworzenie/rozszerzenie metody w `TemplateService` odpowiedzialnej za przetwarzanie pliku CSV.
   - Użycie biblioteki do parsowania CSV (np. OpenCSV) do odczytania i mapowania danych.
   - Mapowanie danych do obiektów `Template` i `TemplateTask`.
   - Zapis danych do bazy przy użyciu repozytoriów JPA.
   - Zapewnienie transakcyjności operacji poprzez `@Transactional`.

4. **Implementacja DTOs:**
   - Przygotowanie DTO w odpowiednim formacie (np. `TemplateResponseDto` oraz zbiorczego podsumowania importu).

5. **Obsługa błędów i logowanie:**
   - Dodanie mechanizmu przechwytywania wyjątków (np. poprzez `@ControllerAdvice`) w celu mapowania wyjątków na odpowiednie kody błędów i `ErrorResponseDto`.
   - Logowanie błędów z użyciem SLF4J.

6. **Testowanie:**
   - Utworzenie testów jednostkowych dla warstwy serwisowej (np. przy użyciu @SpringBootTest lub slice testu).
   - Utworzenie testów integracyjnych dla endpointu z użyciem @WebMvcTest, aby przetestować różne scenariusze (prawidłowy import, błędny format CSV, brak autoryzacji).

7. **Dokumentacja i wdrożenie:**
   - Uaktualnienie dokumentacji API.
   - Przygotowanie środowiska testowego oraz przeprowadzenie testów obciążeniowych.   
   - Wdrożenie aktualizacji w środowisku testowym oraz konfiguracja monitoringu dla operacji związanych z tym endpointem

