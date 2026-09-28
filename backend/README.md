# CondoTrack

Centralized building and condominium management platform: residents, access
control, deliveries, common area bookings, move requests, incidents,
maintenance and notifications — all traceable back to `Building → Unit → Resident`.

## Structure

```
condotrack/
├── backend/     Spring Boot (Java 21, Maven, PostgreSQL, Spring Security, Lombok)
└── frontend/    React + Vite (JavaScript, i18n)
```

## Backend

```
cd backend
mvn clean
mvn spring-boot:run
```

Runs on `http://localhost:8080`. Health check: `GET /api/health`.

Edit `src/main/resources/application.properties` to point at your Postgres
instance (local, Railway, Supabase, etc.).

**Deployment note:** Vercel does not run Spring Boot / long-lived JVM processes —
it only supports Node/Python/Go/Ruby serverless functions and static sites. Deploy
the backend to Railway, Render, or Fly.io instead, and point the frontend at that
API URL.

