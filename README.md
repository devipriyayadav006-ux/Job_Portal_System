# JobConnect Job Portal — Admin and Job Seeker

Full-stack project using Java 21, Spring Boot, JDBC, MySQL, React and Vite.

## Features
- Login and logout with server-side HTTP session
- Public job-seeker registration (new accounts are always USER role)
- Passwords stored as BCrypt hashes
- Admin can create, edit and delete jobs
- Users can browse/search and apply to jobs
- Users can see their own application statuses and interview information
- Admin can see applicants and their contact email, shortlist/reject/hire, schedule interviews, and add interview notes
- Duplicate applications are prevented by a database unique constraint

## Requirements
- JDK 21
- MySQL Server / MySQL Workbench
- Node.js LTS and npm
- IntelliJ IDEA and VS Code recommended

## 1. Database
Open MySQL Workbench and execute `database/job_portal.sql`.
This creates `users`, `jobs`, and `applications`, with foreign keys and duplicate-application protection.

## 2. Backend
Open the `backend` folder as a Maven project in IntelliJ. In `backend/src/main/resources/application.properties`, set your actual MySQL username/password, or set environment variables `DB_USERNAME` and `DB_PASSWORD`.
Run `JobportalApplication.java`. It creates a demo admin if no admin account exists.

Demo admin login (local development only):
- Email: `admin@jobconnect.com`
- Password: `Admin@123`

Change these using `ADMIN_EMAIL` and `ADMIN_PASSWORD` environment variables before first startup in any shared/deployed environment. This is a demo starter, not production-hardened.

Backend endpoints:
- `GET /` health/info
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/jobs`, `GET /api/jobs/{id}`
- Admin-only `POST /api/jobs`, `PUT /api/jobs/{id}`, `DELETE /api/jobs/{id}`
- User-only `POST /api/applications`, `GET /api/applications/mine`
- Admin-only `GET /api/applications`, `PUT /api/applications/{id}/status`

## 3. Frontend
Open a terminal in `frontend`:
```bash
npm install
npm run dev
```
Open `http://localhost:5173`. Keep backend running at `http://localhost:8080`.

## 4. Workflow
1. Sign in with the demo admin credentials.
2. Admin posts one or more jobs.
3. Register a separate user account from the login page.
4. Sign out as admin and sign in as the user.
5. Browse jobs and apply.
6. Sign out, sign back in as admin, and review the applications.
7. Select a status. To schedule an interview, choose `Schedule interview`, set the date/time, add notes/meeting details, and click Save.
8. The user sees the updated status and interview details under My Applications.

## Important security notes
- This starter is intended for local learning. Before production, add CSRF protection suitable for cookie-based sessions, rate limiting, email verification/password reset, stronger validation, secure HTTPS cookie settings, audit logging, and managed secrets.
- Change the demo admin password before sharing or deploying. Never commit real credentials.
- Do not create admin accounts through public registration; registration always creates USER accounts.
