# AGENTS.md

This file provides guidance to agents when working with code in this repository.

Two independent projects (no root build file): `ecom-backend/` (Spring Boot) and `ecom-frontend/` (React). `cd` into each one first; the workspace root has no `package.json`/`pom.xml`. `ecom-frontend/` has its own `.git`. The Maven artifact and Java package are still `ecom` / `com.ecom.ecom` (only the folder was renamed).

## Business rules
- `/api/v1/orders/**` requires a JWT. Prices are stored in cents (integer) and shown in dollars; convert only at the display layer (`utils/formatPrice.js`).
- **All validation is server-side.** The frontend does no validation; it renders `ApiError.fieldErrors` (see `api/client.js`, `components/Field/Field.jsx`). Add new rules in backend DTOs/services, never in React.
- Orders are priced on the server from the DB (client sends only `bookId` + `quantity`). Card data is validated for format/expiry only and is never stored or returned.
- Users only see their own orders; another user's order id returns 404.

## `ecom-backend/` – Spring Boot 4.1.1 backend (Java 17 target, Maven)
- Base package `com.ecom.ecom` with `controller/ service/ repository/ model/ dto/ exception/ security/ config/`. Use DTOs; never expose entities (`service/BookMapper` maps entity -> DTO).
- JDK 21 is installed but NOT on PATH. Before any Maven command (PowerShell): `$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.12'; $env:Path="$env:JAVA_HOME\bin;$env:Path"`.
- Run: `.\mvnw.cmd spring-boot:run`. Test: `.\mvnw.cmd -B test`. Single test: `-Dtest=ApiIntegrationTests#catalogueIsPublicAndSearchable`.
- API is versioned: all routes are under `/api/v1` (`config/ApiVersion`). Swagger UI `/swagger-ui.html`, OpenAPI `/v3/api-docs`.
- Auth: stateless HS256 JWT (Nimbus via `spring-boot-starter-security-oauth2-resource-server`), BCrypt, CSRF off, tokens last 60 min. Public: `POST /auth/signup|login`, `GET /books/**`, Swagger. Everything else needs a token. 401/403 return JSON `ErrorResponse`.
- Errors: `GlobalExceptionHandler` -> `ErrorResponse{status,error,message,fieldErrors[{field,message}]}`. Throw `BadRequestException`(400) / `ConflictException`(409) / `NotFoundException`(404).
- Config: default DB is in-memory H2 (data resets on restart; `contextLoads` relies on it). `SPRING_PROFILES_ACTIVE=postgres` uses `application-postgres.properties` (`ECOM_DB_URL/USER/PASSWORD`). Other env: `ECOM_JWT_SECRET` (set a long secret outside dev), `ECOM_CORS_ORIGINS` (default `http://localhost:3000`).
- `config/DataSeeder` seeds 50 books on an empty DB, with `authorBio`, `imageUrl` (picsum.photos seeded by title) and `authorImageUrl` (dicebear initials, seeded by author). Tests assert counts (50 books, 7 Romance, 2 Tolkien) so update `ApiIntegrationTests` when changing seed data. Add new authors to `AUTHOR_BIOS` or the bio is null.
- Boot 4.x modular starters: `spring-boot-starter-webmvc` (not `-web`), per-starter test artifacts, MockMvc import `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`. springdoc is `3.1.1`.
- Lombok is wired via `annotationProcessorPaths` in `pom.xml` (compile and testCompile). Add other processors to BOTH. Style: tabs in Java, JUnit 5, package-private tests.

## `ecom-frontend/` – Create React App (react-scripts 5.0.1, React 19, plain JS)
- Use `npm.cmd` (PowerShell blocks `npm.ps1`). `npm.cmd start`, `npm.cmd run build`.
- Tests (Jest + RTL): `$env:CI='true'; npm.cmd test -- --watchAll=false`; single test: add `src/App.test.js -t "checkout flow"`. `CI=true` makes ESLint warnings fail the build.
- A native-command stderr line (Node `DEP0176` warning) makes the tool report "Exit code 1" even on success; read the output ("Compiled successfully", pass counts).
- API: `src/api/client.js` (axios) builds `${REACT_APP_API_URL || http://localhost:8080}/api/v1`. `REACT_APP_API_URL` is the server root only. `.env` is gitignored. It attaches the `ecom_token` JWT and fires `auth:expired` on a 401 for protected calls, which `AuthProvider` handles by logging out. Components never call axios directly.
- Tests mock `./api/client`, `authApi`, `bookApi`, `orderApi` (no backend). Wrap renders in `MemoryRouter` + `AuthProvider` + `CartProvider`. `@testing-library/user-event` is v13 (synchronous, no `userEvent.setup()`).
- Test selectors: the hero repeats the best seller as an `<h1>` and `BookCover` repeats titles as `aria-hidden` text, so query book cards with `getByRole('heading', { name, level: 3 })`, never `getByText`. Cart count is an icon with `aria-label="Cart (N)"`.
- `BookCover` shows `book.imageUrl` and falls back to a generated genre-coloured cover if missing or the image errors. `BookDetail` does the same for the author photo (`authorImageUrl`, falls back to the initial).
- localStorage keys: `ecom_token`, `ecom_user`, `ecom_cart` (`[{book, qty}]`).
- Search state lives in the URL (`/?by=genre&q=romance`): Navbar writes it, Home reads it. Design tokens are CSS variables in `src/index.css`; shared classes `.page .narrow .form .btn(-accent|-outline|-secondary|-lg) .badge .error .field .field-error`.
- Style: 2-space indent, single quotes, semicolons, function components with default export, CSS Modules beside each component. No Prettier or lint script.
