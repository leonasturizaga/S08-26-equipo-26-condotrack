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

## Frontend

```
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`. This is the part that deploys cleanly to Vercel.

### Deploying the frontend to Vercel

1. Push this repo to GitHub.
2. In Vercel: New Project → import the repo.
3. Set **Root Directory** to `frontend`.
4. Framework preset: Vite. Build command: `npm run build`. Output directory: `dist`.
5. Deploy.

## Next steps

- Define JPA entities matching the ER diagram (Building, Unit, Resident, Staff,
  VisitorAuthorization, AccessLog, Delivery, CommonArea, Booking, MoveRequest,
  Incident, MaintenanceRequest, Notification).
- Write the PostgreSQL DDL with status check constraints (delivery_status,
  incident_status, etc.) and audit columns (created_at, updated_at, updated_by)
  on every table, per the project's markdown prompt spec.
- Add booking-conflict prevention (unique constraint or transactional lock on
  common_area_id + time range) to eliminate double-bookings.
- Replace `permitAll()` in `SecurityConfig` with role-based rules
  (ADMINISTRATOR / RECEPTION / RESIDENT) once auth is designed.
- Update the CORS allowed origin in `SecurityConfig` once the frontend has a
  deployed URL.
- Add Tailwind, a table/grid library, and a charting library to the frontend
  once the KPI dashboard work starts.
