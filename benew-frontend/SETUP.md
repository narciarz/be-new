# Be New Frontend - Setup

## Jak uruchomić?

### 1. Backend (Spring Boot)
```bash
cd benew-services
./mvnw spring-boot:run
```

Backend uruchomi się na: `http://localhost:8080`

### 2. Frontend (Angular)
```bash
cd benew-frontend
npm start
# lub
npm run start
```

Frontend uruchomi się na: `http://localhost:4200` z automatycznym proxy do backendu.

## Testowanie

### Dane testowe (z backendu):
- Email: `user@example.com`
- Hasło: `password`

### Co powinno działać:
1. Otwórz `http://localhost:4200` w przeglądarce
2. Zostaniesz przekierowany na `/login`
3. Wpisz credentials: `user@example.com` / `password`
4. Po zalogowaniu zobaczysz dashboard z menu

### Sprawdzanie w konsoli przeglądarki:
```javascript
// Token powinien być zapisany w localStorage
localStorage.getItem('auth_token')

// User powinien być zapisany w localStorage
localStorage.getItem('auth_user')
```

## Struktura projektu

```
benew-frontend/
├── src/
│   ├── components/
│   │   ├── login/           # Komponent logowania
│   │   └── dashboard/       # Główny panel po zalogowaniu
│   ├── models/              # TypeScript interfaces (User, DTOs)
│   ├── services/            # AuthService, guards, interceptors
│   └── environments/        # Konfiguracja środowisk
├── proxy.conf.json          # Proxy do backendu
└── package.json
```

## Troubleshooting

### Problem: 403 Forbidden
**Rozwiązanie**: Upewnij się że backend jest uruchomiony i ma nową konfigurację CORS

### Problem: Cannot GET /auth/login
**Rozwiązanie**: Używaj `npm start` (nie `ng serve`) - potrzebujesz proxy

### Problem: Token nie jest dodawany do requestów
**Rozwiązanie**: Interceptor działa automatycznie - sprawdź w Network tab w devtools

### Problem: Brak contentu na stronie głównej
**Rozwiązanie**: Routing przekieruje niezalogowanych na `/login`, zalogowanych na `/dashboard`
