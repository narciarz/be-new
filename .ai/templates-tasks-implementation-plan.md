# API Endpoint Implementation Plan: Task Management for Template Tasks

## 1. Przegląd punktu końcowego

Endpointy dotyczą zarządzania zadaniami powiązanymi z szablonem checklist, wykorzystywane jako zasób zagnieżdżony w zasobie Template. Celem endpointów jest umożliwienie operacji pobierania, tworzenia, aktualizacji i usuwania zadania należącego do konkretnego szablonu.

## 2. Szczegóły żądania

- **Metody HTTP i struktura URL:**
  - **GET /templates/{templateId}/tasks**  
    Pobranie listy wszystkich zadań dla wskazanego szablonu.
  - **POST /templates/{templateId}/tasks**  
    Utworzenie nowego zadania dla wskazanego szablonu.
  - **PUT /templates/{templateId}/tasks/{taskId}**  
    Aktualizacja istniejącego zadania w szablonie.
  - **DELETE /templates/{templateId}/tasks/{taskId}**  
    Usunięcie wskazanego zadania z szablonu.

- **Parametry:**
  - **Wymagane:**
    - `templateId` – identyfikator szablonu (w ścieżce URL).
    - `taskId` – identyfikator zadania (dla operacji PUT oraz DELETE).
    - Dla POST i PUT: Kluczowe pola w ciele żądania:
      - `title`: string, wymagany.
      - `description`: string, wymagany.
      - `taskOrder`: integer, określający kolejność zadania, wymagany.
      - `ownerRole`: string, określający rolę właściciela zadania, wymagany.
  - **Opcjonalne:**  
    W przyszłości mogą być dodane dodatkowe pola, np. status zadania, daty itp.

## 3. Wykorzystywane typy

- **DTO:**
  - `CreateTemplateTaskRequestDto` – reprezentuje dane żądania dla operacji tworzenia zadania.
  - `UpdateTemplateTaskRequestDto` – reprezentuje dane żądania dla operacji aktualizacji zadania.
  - `TemplateTaskResponseDto` – reprezentuje dane odpowiedzi dla operacji związanych z zadaniem.
- **PoJo (Entity):**
  - `TemplateTask` – encja odpowiadająca tabeli w bazie danych.

## 4. Przepływ danych

1. **Przyjmowanie żądania:**  
   Kontroler odbiera żądanie wraz z parametrami ścieżki (`templateId`, `taskId`) oraz ciałem żądania (dla POST i PUT).
2. **Walidacja wejścia:**  
   Dane wejściowe są walidowane przy użyciu adnotacji Bean Validation (@NotBlank, @NotNull, itd.) na DTO.
3. **Delegacja do warstwy serwisowej:**  
   Kontroler przekazuje dane do warstwy usług (np. `TemplateTaskService`), która zawiera logikę biznesową.
4. **Interakcja z bazą danych:**  
   Warstwa serwisowa wykorzystuje repozytorium (np. `TemplateTaskRepository`) do operacji na bazie danych – pobieranie szablonu, tworzenie, aktualizacja lub usuwanie zadania.
5. **Mapowanie pośredniczące:**  
   Mapowanie między encjami a DTO odbywa się przy użyciu mappera (np. MapStruct) lub własnej logiki przekształcającej.
6. **Zwracana odpowiedź:**  
   Serwis zwraca wynik operacji w postaci DTO wraz z odpowiednim kodem stanu HTTP (200, 201, 204).

## 5. Względy bezpieczeństwa

- **Uwierzytelnianie i autoryzacja:**  
  Endpointy powinny być zabezpieczone przy użyciu JWT. Walidacja tokena oraz kontrola dostępów na poziomie ról (np. tylko admin lub odpowiednie role mogą zarządzać zadaniami w szablonach).
- **Walidacja danych:**  
  Wszelkie dane wejściowe są walidowane, aby zapobiec wstrzyknięciom kodu lub przekroczeniom długości ciągu znaków.
- **Bezpieczne operacje na bazie danych:**  
  Upewnij się, że operacje aktualizacji i usuwania są wykonywane tylko, gdy zasób istnieje, aby zapobiec przypadkowym modyfikacjom.

## 6. Obsługa błędów

- **Przykłady scenariuszy błędów:**
  - **400 Bad Request:**  
    Niewłaściwe dane wejściowe lub brak wymaganych pól.
  - **401 Unauthorized:**  
    Brak poprawnego kontekstu uwierzytelnienia.
  - **404 Not Found:**  
    Nie znaleziono szablonu lub zadania o wskazanym identyfikatorze.
  - **500 Internal Server Error:**  
    Błąd po stronie serwera podczas przetwarzania żądania.
- **Rejestrowanie błędów:**  
  Wykorzystanie SLF4J do logowania błędów wraz z krytycznymi danymi diagnostycznymi, ale bez ujawniania wrażliwych informacji.
- **Centralizacja obsługi błędów:**  
  Użycie @ControllerAdvice do globalnej obsługi wyjątków, zwracając spójny obiekt błędu (ErrorResponseDto).

## 7. Rozważania dotyczące wydajności

- **Operacje bazodanowe:**  
  Upewnić się, że operacje zapisu są efektywnie wykonywane (np. użycie odpowiednich indeksów).
- **Mapowanie DTO:**  
  Wydajne mapowanie między encjami a DTO przy użyciu bibliotek takich jak MapStruct.
- **Cache’owanie:**  
  Jeżeli operacja GET /templates/{templateId}/tasks będzie intensywnie wykorzystywana, rozważyć zastosowanie cache’owania.
- **Ograniczenie przepływu danych:**  
  Jeśli lista zadań będzie duża, wdrożyć paginację.

## 8. Etapy wdrożenia

1. **Definicja DTO oraz walidacji:**  
   - Utworzenie `CreateTemplateTaskRequestDto`, `UpdateTemplateTaskRequestDto` oraz `TemplateTaskResponseDto` z odpowiednimi adnotacjami walidacyjnymi.
2. **Rozbudowa encji:**  
   - Weryfikacja lub ewentualna modyfikacja encji `TemplateTask` tak, aby była spójna z wymaganiami.
3. **Implementacja warstwy serwisowej:**  
   - Utworzenie lub uaktualnienie serwisu (`TemplateTaskService`), który będzie odpowiedzialny za logikę biznesową: pobieranie, zapisywanie, aktualizację oraz usuwanie zadań.
4. **Modyfikacja kontrolera:**  
   - Rozszerzenie kontrolera szablonów (TemplateController) albo utworzenie dedykowanego kontrolera dla zadań w ramach szablonu, implementując endpointy GET, POST, PUT, DELETE.
5. **Implementacja repozytorium:**  
   - Weryfikacja, że repozytorium (np. `TemplateTaskRepository`) wspiera wymagane operacje oraz, w razie potrzeby, dodanie metod wyszukujących.
6. **Obsługa wyjątków i centralne logowanie:**  
   - Utworzenie globalnego handlera wyjątków z wykorzystaniem @ControllerAdvice oraz przygotowanie standardowych komunikatów błędów (ErrorResponseDto).
7. **Testowanie slice:**  
   - Przygotowanie testów kontrolera z wykorzystaniem @WebMvcTest oraz testów serwisowych używając np. @SpringBootTest lub testów slice, aby zapewnić poprawność działania endpointów.
8. **Dokumentacja i review:**  
   - Dokumentacja endpointów oraz przeprowadzenie code review, aby upewnić się, że kod jest spójny z zasadami implementacji i wymogami architektonicznymi.
9. **Wdrożenie i monitorowanie:**  
   - Wdrożenie aktualizacji w środowisku testowym oraz konfiguracja monitoringu dla operacji związanych z tymi endpointami.
