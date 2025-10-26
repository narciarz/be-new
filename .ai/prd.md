# Dokument wymagań produktu (PRD) - Be New

## 1. Przegląd produktu
Be New to aplikacja webowa upraszczająca i standaryzująca proces wdrożenia nowych pracowników w organizacji. System zapewnia centralne zarządzanie checklistami onboardingowymi, widoczność postępu dla menedżerów oraz pełną kontrolę nad szablonami zadań dla administratorów. Produkt jest dostarczany jako rozwiązanie SaaS uruchamiane w przeglądarce, z bezpiecznym logowaniem i granularnymi rolami dostępu.

## 2. Problem użytkownika
Obecnie onboarding jest niespójny, a zespoły korzystają z różnych narzędzi (arkusze, e-maile, komunikatory). Brakuje jednego źródła prawdy o liście zadań, a menedżerowie nie mają bieżącej informacji o statusie wdrożenia. Skutkiem są pominięte kroki, opóźnienia i zbędne koszty administracyjne.

## 3. Wymagania funkcjonalne
1. Zarządzanie użytkownikami (rola Administrator)
   - CRUD kont dla ról Menedżer i Użytkownik.
   - Przypisanie użytkownika do menedżera.
   - Resetowanie hasła.
2. Zarządzanie szablonami checklist (rola Administrator)
   - CRUD szablonów przypisanych do stanowisk.
   - Dodawanie, edycja, usuwanie zadań; zmiana kolejności.
   - Jednorazowy import z CSV.
   - Wersjonowanie: zmiany nie wpływają na aktywne wdrożenia.
3. Proces wdrożenia (rola Menedżer, Użytkownik)
   - Automatyczne tworzenie checklisty przy założeniu konta pracownika.
   - Oznaczanie zadań jako ukończone przez właściciela zadania.
   - Wizualny pasek postępu oraz przekreślenie ukończonych pozycji.
4. Dashboard menedżera
   - Lista przypisanych pracowników z postępem.
   - Wyszukiwarka.
   - Archiwizacja i przywracanie procesów.
5. Widok użytkownika
   - Podgląd pełnej checklisty; edycja tylko własnych zadań.
6. Bezpieczeństwo i uwierzytelnianie
   - Logowanie z hasłem obsługiwane przez moduł bezpieczeństwa.
   - Sesje wygasają po 30 minutach bezczynności.
   - Role: Administrator, Menedżer, Użytkownik.

## 4. Granice produktu
- Poza zakresem MVP: komentarze do zadań, powiadomienia e-mail, aplikacja mobilna, integracje z narzędziami HR i innymi systemami firmowymi.
- Brak samodzielnej zmiany hasła przez użytkownika (reset wyłącznie przez administratora).
- Automatyczne usuwanie danych zgodnie z RODO zostanie rozważone po MVP.

## 5. Historyjki użytkowników
| ID | Tytuł | Opis | Kryteria akceptacji |
|----|-------|------|----------------------|
| US-001 | Logowanie administratora | Administrator loguje się do systemu, aby zarządzać użytkownikami i szablonami | 1. Przy poprawnych danych logowanie kończy się sukcesem. 2. Przy błędnych danych wyświetlany jest komunikat o błędzie. |
| US-002 | Tworzenie szablonu checklisty | Administrator tworzy nowy szablon checklisty dla stanowiska Programista | 1. Po wypełnieniu formularza i zapisaniu szablon zostaje utworzony. 2. Szablon widoczny jest na liście szablonów. |
| US-003 | Edycja zadań w szablonie | Administrator edytuje treść zadania w istniejącym szablonie | 1. Po zapisaniu zmian nowa treść jest widoczna w podglądzie szablonu. 2. Aktywne wdrożenia nie zmieniają się. |
| US-004 | Import szablonów z CSV | Administrator importuje plik CSV z zadaniami do nowego szablonu | 1. Po imporcie na liście zadań pojawiają się wszystkie wiersze z pliku. 2. System zgłasza błędy dla niepoprawnych wierszy. |
| US-005 | Zakładanie konta menedżera | Administrator zakłada konto dla menedżera | 1. Konto menedżera widoczne jest na liście użytkowników. 2. Menedżer otrzymuje tymczasowe hasło od administratora. |
| US-006 | Przypisanie pracownika do menedżera | Administrator tworzy konto pracownika i przypisuje go do menedżera | 1. System automatycznie generuje checklistę dla pracownika. 2. Menedżer widzi nowy wpis na dashboardzie z postępem 0 %. |
| US-007 | Reset hasła użytkownika | Administrator resetuje hasło użytkownika na prośbę menedżera | 1. Po operacji generowane jest nowe hasło tymczasowe. 2. Stare hasło przestaje działać. |
| US-008 | Podgląd dashboardu | Menedżer przegląda listę swoich pracowników wraz z paskiem postępu | 1. Lista zawiera imię, nazwisko, stanowisko i procent ukończenia. 2. Wyszukiwarka filtruje listę w czasie rzeczywistym. |
| US-009 | Aktualizacja statusu zadania menedżera | Menedżer oznacza zadanie firmowe jako ukończone | 1. Zadanie zostaje przekreślone i wyszarzone. 2. Pasek postępu aktualizuje się natychmiast. |
| US-010 | Cofnięcie statusu zadania | Menedżer cofa oznaczenie zadania przez pomyłkę | 1. Zadanie wraca do stanu otwartego. 2. Pasek postępu aktualizuje się. |
| US-011 | Ukończenie zadania przez pracownika | Pracownik odznacza swoje zadanie | 1. Zadanie przekreślone i wyszarzone. 2. Pasek postępu aktualizuje się dla menedżera i pracownika. |
| US-012 | Archiwizacja ukończonego wdrożenia | Menedżer archiwizuje proces po ukończeniu wszystkich zadań | 1. Proces znika z listy aktywnej i pojawia się w archiwum. 2. Status zmienia się na Zakończony. |
| US-013 | Przywrócenie procesu z archiwum | Menedżer przywraca omyłkowo zarchiwizowany proces | 1. Proces wraca na listę aktywną z zachowanym postępem. |
| US-014 | Autoryzacja ról | System ogranicza dostęp do funkcji w zależności od roli | 1. Administrator widzi panel admina, menedżer nie. 2. Użytkownik widzi wyłącznie własną checklistę. |
| US-015 | Wylogowanie po bezczynności | System wylogowuje użytkownika po 30 minutach braku aktywności | 1. Po przekroczeniu limitu sesja wygasa. 2. Próba wykonania akcji przekierowuje na ekran logowania. |

## 6. Metryki sukcesu
- Wskaźnik adopcji: co najmniej 90 % nowych pracowników na objętych stanowiskach korzysta z Be New.
- Ujednolicenie procesu: 100 % pracowników o tej samej roli otrzymuje identyczną bazową checklistę.
- Widoczność: menedżerowie oceniają przejrzystość dashboardu na minimum 4 / 5 w ankietach.
- Czas ukończenia wdrożenia: skrócenie mediany czasu o 20 % w porównaniu z procesem manualnym.
