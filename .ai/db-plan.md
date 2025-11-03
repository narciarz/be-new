<conversation_summary> <decisions>
Strategia kluczy głównych (PK): Wszystkie tabele będą używać UUID jako kluczy głównych. Będą one generowane po stronie aplikacji Spring przy użyciu adnotacji @GeneratedValue(strategy = GenerationType.UUID). Baza danych nie będzie używać gen_random_uuid() ani rozszerzenia pgcrypto.
Bezpieczeństwo (RLS): Podjęto strategiczną decyzję o całkowitej rezygnacji z Row-Level Security (RLS) w PostgreSQL dla MVP. Cały ciężar filtrowania danych (np. zapewnienie, że Menedżer widzi tylko swoich pracowników) spoczywa na logice aplikacji Spring.
Integralność Danych (Role/Statusy): Zrezygnowano z użycia natywnych typów ENUM oraz ograniczeń CHECK w bazie danych dla pól takich jak role czy status. Integralność tych danych będzie zapewniona wyłącznie na poziomie aplikacji poprzez mapowanie enumów Javy na kolumny VARCHAR (przy użyciu @Enumerated(EnumType.STRING)).
Integralność Danych (NULL): Zachowano ograniczenia NOT NULL na poziomie bazy danych dla wszystkich krytycznych pól biznesowych (np. email, role, title zadania).
Wersjonowanie Szablonów: Zastosowano podejście denormalizacji, zgodnie z którym zadania z template_task są kopiowane do onboarding_task w momencie tworzenia procesu wdrożenia. Zmiany w szablonie nie wpływają na aktywne wdrożenia.
Obliczanie Postępu: Postęp procentowy będzie obliczany w logice aplikacji Spring, a nie przez triggery bazodanowe. Tabela onboarding_process będzie przechowywać zdenormalizowane liczniki total_tasks_count i completed_tasks_count do szybkiego odczytu.
Architektura Bazy Danych: Aplikacja będzie korzystać z dedykowanego schematu o nazwie benew.
Role Bazy Danych: Zdefiniowano dwie role: app_owner (do zarządzania schematem i migracjami DDL) oraz app_spring (używana przez backend Spring Boot, posiadająca tylko uprawnienia DML).
Klucze Obce (FK): Wszystkie relacje kluczy obcych będą używać strategii ON DELETE RESTRICT. Logika aplikacji Spring jest odpowiedzialna za obsługę przypadków usuwania (np. uniemożliwienie usunięcia Menedżera, który ma przypisanych pracowników).
Audyt: Kolumny audytowe (created_at, updated_at typu TIMESTAMPTZ NOT NULL) będą zarządzane przez Spring Data JPA Auditing (@CreatedDate, @LastModifiedDate).
Indeksowanie: Utworzono jawne indeksy B-tree na wszystkich kluczach obcych UUID oraz na kolumnach używanych do wyszukiwania i zapewnienia unikalności: UNIQUE(lower(email)) oraz UNIQUE(template.position_name).
Normalizacja: Aplikacja jest odpowiedzialna za normalizację (np. trim().toLowerCase()) wartości position_name przed zapisem lub wyszukiwaniem.
</decisions>
<matched_recommendations>

Użycie jednej, centralnej tabeli app_user z rekurencyjną relacją manager_id do modelowania hierarchii Menedżer-Pracownik.
Zastosowanie denormalizacji (kopiowanie zadań) jako strategii wersjonowania szablonów, aby zmiany w definicjach nie wpływały na aktywne procesy.
Przeniesienie logiki obliczania postępu do aplikacji i przechowywanie wyników w zdenormalizowanych kolumnach (total_tasks_count, completed_tasks_count) w celu optymalizacji odczytu.
Użycie kolumny status ('ACTIVE', 'ARCHIVED') do implementacji archiwizacji procesów.
Zastosowanie ON DELETE RESTRICT dla kluczy obcych, aby wymusić obsługę logiki biznesowej (np. usuwania) po stronie aplikacji.
Dodanie kolumny task_order typu INTEGER do zarządzania kolejnością zadań w checklistach.
Implementacja audytu (created_at, updated_at) przy użyciu TIMESTAMPTZ i zarządzanie nimi przez mechanizmy Spring Auditing.
Stworzenie dedykowanych ról PostgreSQL (app_owner dla migracji DDL, app_spring dla DML) w celu separacji uprawnień.
Jawne zdefiniowanie indeksów B-tree na wszystkich kluczach obcych UUID oraz na kolumnach tekstowych używanych w klauzulach WHERE (np. email, position_name) w celu zapewnienia wydajności.
Użycie adnotacji @Enumerated(EnumType.STRING) w JPA do mapowania enumów Java na kolumny VARCHAR w bazie danych. </matched_recommendations>
<database_planning_summary> ### Podsumowanie planowania bazy danych
Na podstawie wymagań produktu (PRD) i stacku technologicznego (Spring Boot, PostgreSQL), opracowano schemat bazy danych dla MVP.
a. Główne wymagania dotyczące schematu
Schemat musi wspierać aplikację SaaS z trzema rolami użytkowników (Admin, Menedżer, Użytkownik). Kluczowe funkcje to zarządzanie szablonami checklist, automatyczne tworzenie wdrożeń na podstawie stanowiska oraz śledzenie postępu zadań. Schemat musi wspierać wersjonowanie szablonów (zmiany nie wpływają na aktywne procesy) oraz archiwizację zakończonych wdrożeń.
b. Kluczowe encje i ich relacje
Zdefiniowano pięć głównych tabel w schemacie benew:
app_user: (PK: id UUID) Przechowuje wszystkich użytkowników (Admin, Menedżer, Pracownik). Zawiera email, password_hash, role (VARCHAR), first_name (VARCHAR(50)), last_name (VARCHAR(50)) i position_name (VARCHAR(50)). Posiada klucz obcy manager_id (rekurencyjnie wskazujący na app_user(id)) do budowania hierarchii.
template: (PK: id UUID) Nagłówek szablonu. Zawiera unikalne position_name (VARCHAR(50)).
template_task: (PK: id UUID) Zadania wzorcowe. Zawiera title (VARCHAR(255)), description (TEXT), task_order (INTEGER) i owner_role (VARCHAR). Posiada klucz obcy template_id -> template(id).
onboarding_process: (PK: id UUID) Reprezentuje aktywne wdrożenie. Zawiera status (VARCHAR), liczniki postępu (total_tasks_count, completed_tasks_count). Posiada klucze obce user_id -> app_user(id), manager_id -> app_user(id) oraz source_template_id -> template(id).
onboarding_task: (PK: id UUID) Skopiowane zadania dla konkretnego wdrożenia. Zawiera title, description, task_order, owner_role oraz is_completed (BOOLEAN). Posiada klucz obcy onboarding_process_id -> onboarding_process(id).
Wszystkie klucze obce używają strategii ON DELETE RESTRICT.

c. Ważne kwestie dotyczące bezpieczeństwa i skalowalności
Bezpieczeństwo: Podjęto kluczową decyzję o rezygnacji z RLS. Cała odpowiedzialność za filtrowanie danych (izolację danych między menedżerami a pracownikami) spoczywa na logice aplikacji Spring. Rola app_spring ma dostęp DML do wszystkich danych w schemacie benew.
Integralność Danych: Integralność jest ograniczona do ograniczeń NOT NULL i UNIQUE (na email i position_name). Walidacja danych biznesowych (np. poprawność ról) odbywa się w aplikacji.
Wydajność (Skalowalność): Zastosowanie UUID jako PK. Aby zapewnić wydajność zapytań JOIN i WHERE, utworzono jawne indeksy B-tree na wszystkich kolumnach kluczy obcych (manager_id, user_id, template_id itd.) oraz na polach wyszukiwania (email, position_name, last_name, first_name).
Wydajność (Obliczenia): Uniknięto kosztownych obliczeń COUNT() przy odczycie dashboardu poprzez denormalizację liczników postępu (total_tasks_count, completed_tasks_count) w tabeli onboarding_process, które są aktualizowane przez aplikację.
</database_planning_summary>
<unresolved_issues> Wszystkie zidentyfikowane kwestie zostały omówione i rozwiązane podczas rozmowy. Ostateczny schemat DDL został wygenerowany i zaakceptowany, nie pozostawiając żadnych nierozwiązanych punktów dotyczących planowania bazy danych dla MVP. </unresolved_issues> </conversation_summary>