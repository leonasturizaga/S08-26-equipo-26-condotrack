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
