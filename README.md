# LegalEase AI

> AI-powered case and client management system for independent lawyers in India.

[![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue?style=flat-square)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square)](https://www.postgresql.org/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-green?style=flat-square)](https://spring.io/projects/spring-ai)

---

## The Problem

Independent lawyers in India spend 40–60% of their working time on paperwork — filling client intake forms, logging court hearing updates, drafting case summaries, and managing follow-up action items. Most still use paper files, Excel sheets, or basic CRMs with zero intelligence built in.

LegalEase AI solves this with a single core idea: **type a rough note, get a structured form filled automatically.** A lawyer types *"John Doe theft case opposing Ravi Kumar, Mumbai Sessions Court, 2022"* and the AI extracts and fills every field in the client intake form. Same for hearing logs — *"Sharma vs Gupta, judge postponed to Nov 5, need to submit affidavit"* fills the entire hearing update in seconds.

---

## Architecture

LegalEase AI is a **Modular Monolith** — a single deployable Spring Boot application with strict internal module boundaries. Each module owns its Controller, Service, Repository, Model, and DTOs. No cross-module repository access. No shared service layer.

```
┌─────────────────────────────────────────────────────────────┐
│                    React + Redux Frontend                    │
│          Vite · RTK Query · React Hook Form · Tailwind      │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTPS / JWT
┌─────────────────────▼───────────────────────────────────────┐
│               Spring Boot 4.1 · Java 25                     │
│                                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │   Auth   │ │  Cases   │ │ Clients  │ │ Hearings │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │    AI    │ │Dashboard │ │Document  │ │  Admin   │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
│                                                             │
│         Spring Security · Spring AI · Spring Data JPA       │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    PostgreSQL 16                             │
│         Flyway migrations · UUID PKs · Row-level isolation  │
└─────────────────────────────────────────────────────────────┘
                      │
              ┌───────▼───────┐
              │  Gemini API   │
              │ (via Spring AI│
              │  OpenAI compat│
              └───────────────┘
```

---

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security 7 · JWT (JJWT 0.12.6) · Google OAuth2 |
| AI | Spring AI 2.0 · Gemini 2.0 Flash (OpenAI-compatible endpoint) |
| ORM | Spring Data JPA · Hibernate |
| Database | PostgreSQL 16 |
| Migrations | Flyway 10 |
| PDF Export | iText 9 |
| Testing | JUnit 5 · Mockito · Spring Boot Test |
| Build | Maven |

### Frontend
| Layer | Technology |
|---|---|
| Framework | React 18 · Vite |
| State | Redux Toolkit · RTK Query |
| Forms | React Hook Form · Zod |
| Styling | Tailwind CSS v4 |
| HTTP | Axios (JWT interceptor) |
| Dates | date-fns |
| Notifications | React Hot Toast |

---

## Features

### For Lawyers
- **AI Log Input** — type a rough note, AI extracts and fills structured form fields (client intake + hearing log)
- **Case management** — create, search, filter by status, paginated list, case detail view
- **Client intake** — AI-assisted or manual, duplicate guard, opposing party tracking, case background
- **Hearing logs** — date-aware logging, next date tracking, action items, AI parsing
- **PDF export** — one-click export for case summaries, client intake forms, and hearing logs
- **Dashboard** — live counts, upcoming hearings widget, recent cases, AI adoption stats
- **Referral system** — generate a referral code, invite clerks to join your practice

### For Clerks
- Log hearings using AI or manually
- Read access to all cases and clients
- Cannot create cases, delete records, or export PDFs

### For Admins
- Review pending lawyer registrations
- View uploaded Bar Council enrollment certificates
- Approve or reject accounts with optional rejection reason
- Full user management — view all users, filter by role, delete accounts

### Platform
- **Delayed authentication** — browse the app before committing to an account
- **Google OAuth2** — one-click login with no password required
- **Role-based access control** — `ROLE_LAWYER`, `ROLE_CLERK`, `ROLE_ADMIN` enforced at both API and UI level
- **Account verification flow** — lawyers start PENDING, admin reviews certificate, account goes ACTIVE
- **Row-level data isolation** — every query is scoped to the authenticated lawyer's UUID

---

## Project Structure

```
legalease/
├── backend/                          ← Spring Boot application
│   └── src/main/java/com/legalease/
│       ├── auth/                     ← Register, login, JWT, OAuth2
│       ├── cases/                    ← Case CRUD, status transitions
│       ├── clients/                  ← Client intake, AI-assisted flag
│       ├── hearings/                 ← Hearing logs, upcoming query
│       ├── ai/                       ← Spring AI, prompt templates, parser
│       ├── dashboard/                ← Aggregated stats endpoint
│       ├── document/                 ← PDF export (iText 9)
│       ├── admin/                    ← Approval flow, user management
│       ├── user/                     ← User entity, UserRepository
│       ├── common/                   ← JWT filter, exceptions, enums
│       └── config/                   ← SecurityConfig, SpringAIConfig
│
└── legalease-frontend/               ← React application
    └── src/
        ├── api/                      ← Axios instance + RTK base query
        ├── app/                      ← Redux store
        ├── components/
        │   ├── common/               ← Button, Input, Badge, Table, Modal...
        │   ├── layout/               ← AppLayout, Sidebar, Topbar, AuthLayout
        │   └── ai/                   ← AILogInput (the core feature component)
        ├── features/
        │   ├── auth/                 ← Login, Register, OAuth2 callback
        │   ├── dashboard/            ← Stats, widgets
        │   ├── cases/                ← CasesPage, CaseDetailPage, CaseForm
        │   ├── clients/              ← ClientsPage, ClientDetailPage, IntakeForm
        │   ├── hearings/             ← HearingsPage, HearingLogForm
        │   ├── ai/                   ← aiApi RTK slice
        │   ├── documents/            ← PDF export API slice
        │   └── admin/                ← Admin dashboard, pending lawyers, users
        ├── hooks/                    ← useAuth, useRole, usePDFExport
        ├── routes/                   ← ProtectedRoute, RoleRoute, PublicRoute
        └── utils/                    ← parseAIResponse, formatDate
```

---

## Getting Started

### Prerequisites
- Java 25
- Node.js 20+
- PostgreSQL 16
- Maven 3.9+
- Gemini API key (from [Google AI Studio](https://aistudio.google.com/))
- Google OAuth2 credentials (from [Google Cloud Console](https://console.cloud.google.com/))

### 1 — Clone the repository
```bash
git clone https://github.com/yourusername/legalease-ai.git
cd legalease-ai
```

### 2 — Set up the database
```sql
psql -U postgres
CREATE DATABASE legalease_db;
\q
```

### 3 — Configure backend environment

Create `backend/src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/legalease_db
    username: postgres
    password: yourpassword
  ai:
    openai:
      api-key: YOUR_GEMINI_API_KEY
      base-url: https://generativelanguage.googleapis.com/v1beta/openai
      chat:
        options:
          model: gemini-2.0-flash
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET

app:
  jwt:
    secret: your-minimum-32-character-secret-key
    expiration-ms: 86400000
  frontend-url: http://localhost:5173
```

### 4 — Run the backend
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Flyway will automatically create all tables on first run. A default admin account is seeded:
```
Email:    admin@legalease.com
Password: Admin@123
```
**Change this password immediately after first login.**

### 5 — Configure frontend environment

Create `legalease-frontend/.env.development`:
```
VITE_API_BASE_URL=http://localhost:8080
VITE_GOOGLE_CLIENT_ID=your-google-client-id
```

### 6 — Run the frontend
```bash
cd legalease-frontend
npm install
npm run dev
```

Visit `http://localhost:5173`

---

## API Overview

All protected endpoints require `Authorization: Bearer <token>` header.

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register lawyer or clerk |
| POST | `/api/auth/login` | Public | Email + password login |
| GET | `/api/auth/me` | Protected | Get current user status |
| GET | `/oauth2/authorization/google` | Public | Initiate Google OAuth2 |

### Cases
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/cases` | Lawyer, Clerk | Paginated list with search + filter |
| POST | `/api/cases` | Lawyer | Create case |
| GET | `/api/cases/:id` | Lawyer, Clerk | Case detail |
| PUT | `/api/cases/:id` | Lawyer | Update case |
| PATCH | `/api/cases/:id/status` | Lawyer | Update case status |
| DELETE | `/api/cases/:id` | Lawyer | Delete case |

### Clients
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/clients/intake` | Lawyer | Create client (AI or manual) |
| GET | `/api/clients` | Lawyer, Clerk | All clients with search |
| GET | `/api/clients/case/:caseId` | Lawyer, Clerk | Clients by case |
| PUT | `/api/clients/:id` | Lawyer | Update client |
| DELETE | `/api/clients/:id` | Lawyer | Delete client |

### Hearings
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/hearings/log` | Lawyer, Clerk | Log hearing (AI or manual) |
| GET | `/api/hearings` | Lawyer, Clerk | All hearings paginated |
| GET | `/api/hearings/upcoming` | Lawyer, Clerk | Future hearings sorted by date |
| GET | `/api/hearings/case/:caseId` | Lawyer, Clerk | Hearings by case |
| PUT | `/api/hearings/:id` | Lawyer, Clerk | Update hearing |
| DELETE | `/api/hearings/:id` | Lawyer | Delete hearing |

### AI
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/ai/parse` | Lawyer, Clerk | Parse raw note → structured JSON |

### Documents
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/docs/export-pdf` | Lawyer | Export case/client/hearing as PDF |

### Dashboard
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/dashboard` | Lawyer, Clerk | All stats in one call |

### Admin
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/admin/dashboard` | Admin | Platform overview |
| GET | `/api/admin/pending-lawyers` | Admin | Pending approval queue |
| GET | `/api/admin/users` | Admin | All users with role filter |
| PATCH | `/api/admin/users/:id/approval` | Admin | Approve or reject |
| DELETE | `/api/admin/users/:id` | Admin | Delete user |

---

## Database Schema

```
users
├── id (UUID PK)
├── full_name, email, password (nullable for OAuth)
├── role (ROLE_LAWYER / ROLE_CLERK / ROLE_ADMIN)
├── auth_provider (LOCAL / GOOGLE)
├── account_status (PENDING / ACTIVE / REJECTED)
├── bar_council_number, certificate_path (lawyers only)
├── referral_code (unique, lawyers only)
└── invited_by (UUID FK → users, clerks only)

cases
├── id (UUID PK)
├── lawyer_id (UUID FK → users)
├── case_title, case_type, case_status
├── case_number, court_name, notes
└── created_at, updated_at

clients
├── id (UUID PK)
├── case_id (UUID FK → cases)
├── lawyer_id (UUID FK → users)
├── client_name, phone, email
├── opposing_party, case_background
├── raw_intake_note (AI audit trail)
└── ai_assisted (boolean)

hearings
├── id (UUID PK)
├── case_id (UUID FK → cases)
├── lawyer_id (UUID FK → users)
├── hearing_date, next_date
├── outcome, action_items (JSON string)
├── raw_note (AI audit trail)
└── ai_assisted (boolean)
```

---

## Testing

```bash
# Run all unit tests
cd backend
./mvnw test

# Run with coverage report
./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html
```

### Test coverage summary
| Module | Unit tests | What's covered |
|---|---|---|
| Auth | 13 | Register validation, login flows, account status, edge cases |
| Cases | 10 | CRUD, status transitions, ownership checks, closed-case guard |
| Clients | 12 | Intake creation, duplicate guard, AI flag preservation, ownership |
| Hearings | 7 | Logging, duplicate date guard, upcoming query, update/delete |
| AI Parser | 4 | Intake parse, hearing parse, JSON fence stripping, LLM failure fallback |
| Dashboard | 2 | Count aggregation, upcoming hearings limit |
| Admin | 8 | Approve/reject, self-delete guard, admin-delete guard, counts |
| **Total** | **56 tests** | |

---

## Security

- **JWT authentication** — HS256 signed tokens, 24h expiry, claims include role and userId
- **Spring Security filter chain** — JWT validated on every request before hitting controllers
- **Role-based access** — `@PreAuthorize` on every sensitive endpoint, enforced at method level
- **Row-level isolation** — every DB query includes `lawyer_id = :lawyerId` condition, no cross-user data access possible
- **Password hashing** — BCrypt with strength 12
- **OAuth2 PKCE** — Google login uses Spring Security's built-in state parameter validation
- **Certificate upload validation** — file type (PDF/image only) and size (10MB max) enforced server-side
- **CORS** — restricted to configured frontend origins only
- **No sensitive data in JWT** — `accountStatus` is always fetched fresh from DB via `/api/auth/me`

---

## Deployment

### Backend — Render

1. Push code to GitHub
2. Create new Web Service on [Render](https://render.com)
3. Select your repo, set build command: `./mvnw clean package -DskipTests`
4. Set start command: `java -jar target/legalease-ai.jar --spring.profiles.active=prod`
5. Add all environment variables from `.env.prod`
6. Create a PostgreSQL instance on Render and link it via `DATABASE_URL`

### Frontend — Vercel

1. Connect your GitHub repo to [Vercel](https://vercel.com)
2. Set framework preset to Vite
3. Set root directory to `legalease-frontend`
4. Add environment variables: `VITE_API_BASE_URL` and `VITE_GOOGLE_CLIENT_ID`
5. Deploy

### Google OAuth — production setup

In Google Cloud Console add these authorized redirect URIs:
```
http://localhost:8080/login/oauth2/code/google    ← for dev
https://your-backend.onrender.com/login/oauth2/code/google  ← for prod
```

---

## Environment Variables Reference

### Backend
| Variable | Description |
|---|---|
| `DATABASE_URL` | Full JDBC URL for PostgreSQL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Min 32 character signing secret |
| `GOOGLE_CLIENT_ID` | From Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | From Google Cloud Console |
| `GEMINI_API_KEY` | From Google AI Studio |
| `FRONTEND_URL` | Deployed React app URL |

### Frontend
| Variable | Description |
|---|---|
| `VITE_API_BASE_URL` | Backend API base URL |
| `VITE_GOOGLE_CLIENT_ID` | From Google Cloud Console |

---

## Certifications & Standards

- OCI Associate certified developer (infrastructure awareness informed deployment decisions)
- NPTEL Java Gold (core language foundation)
- Follows OWASP Top 10 security guidelines for API design

---

## License

This project is licensed under the MIT License.

---

## Author

Built by a Java Full Stack developer targeting backend and SDE roles.
Certifications: OCI Associate · NPTEL Java Gold.

> Built to solve a real problem in a real industry — not a todo app.
