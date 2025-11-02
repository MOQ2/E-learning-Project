# E-Learning Platform

Modern end-to-end learning platform that combines a Spring Boot backend, Angular front-end, and an optional AI-powered recommendation service. The system covers the full learner journey: discovery, enrollment, learning, assessments, and intelligent guidance.

## Architecture at a Glance
- **Backend** (`Backend/`): Spring Boot 3, PostgreSQL, Flyway, Spring Security with JWT, Testcontainers-based tests, AWS S3 video storage, Stripe-ready payment flows, Swagger/OpenAPI docs.
- **Front-end** (`Frontend/`): Angular 18+, Angular Material, Bootstrap, Plyr/HLS video playback, JWT-aware routing guards, toast notifications, responsive layout.
- **RAG Service** (`RAG-Service/`): FastAPI, LangChain, Google Gemini, sentence-transformers, pgvector-backed embeddings, chat memory stored in PostgreSQL.
- **Shared Infrastructure**: PostgreSQL database (application data + embeddings), optional object storage (AWS S3), external payment provider (Stripe), Gemini API key for the chatbot.

```
Client (Angular) ─┐
                 ├──> Spring Boot API (courses, quizzes, commerce, media)
Chatbot UI ──────┘            │
                              ├──> PostgreSQL (application data, pgvector embeddings)
                              ├──> AWS S3 (course media)
                              └──> FastAPI RAG Service (recommendations, Q&A)
```

## Platform Features
- **Learning Experience**: Courses with modules, lessons, attachments, tags, difficulty levels, video streaming via pre-signed S3 URLs, quiz authoring and submissions, feedback collection, continue-learning dashboards, and deadline reminders.
- **Authoring & Administration**: Course builder with multipart uploads, module ordering, bulk tag/category management, package catalogs, promotion codes, user video uploads, attachment management, admin dashboards for payments and enrollments.
- **Commerce & Access Control**: One-time purchases, recurring subscriptions (1/3/6 month plans), package sales, promotion code validation, Stripe intent processing, granular access checks (course ownership, user data access, course visibility), user-course entitlement APIs.
- **Intelligent Assistant**: RAG chatbot that recommends courses and answers course-specific questions with hybrid chat memory, Gemini reasoning, embeddings sourced from the core catalog, and synchronization hooks to index new or updated courses.
- **Operational Tooling**: Flyway migrations on startup, Testcontainers-backed integration tests, Swagger UI for REST exploration, environment-based configuration, layered authorization guards, extensible DTO/Mapper pattern.

## Repository Layout
- `Backend/` - Spring Boot service (API, security, services, repositories, migrations).
- `Frontend/` - Angular workspace for learner and admin UI.
- `RAG-Service/` - FastAPI microservice powering the recommendation chatbot.
- `logs/` - Sample log output directories used during development.
- `package.json` (root) - Workspace level tooling placeholder.

## Prerequisites
- Java 17 (JDK) and Maven Wrapper (`./mvnw`) or Maven 3.9+
- Node.js 20+ and npm (Angular CLI 20)
- Python 3.11+ for the RAG service (virtual environments recommended)
- PostgreSQL 15+ with the `pgvector` extension enabled
- AWS account (or compatible S3 storage) for media uploads and streaming
- Google Gemini API key for the chatbot



### Backend (`Backend/src/main/resources/application.properties`)
Either keep this file locally (never push secrets) or externalize via environment variables. Replace placeholder values with your own.

```
spring.datasource.url=jdbc:postgresql://localhost:5432/eLearning
spring.datasource.username=<your_db_user>
spring.datasource.password=<your_db_password>
app.public-base-url=http://localhost:5000
server.port=5000

# AWS S3
aws.region=<your_region>
aws.s3.bucket-name=<your_bucket>
aws.access-key-id=<your_access_key>
aws.secret-access-key=<your_secret_key>


# JWT (consider externalizing instead of the current hard-coded value)
app.jwt.secret=<long_random_string>
app.jwt.expiration-days=365
```


### Front-end (`Frontend/src/environments/environment.ts`)
```
export const environment = {
  production: false,
  apiUrl: 'http://localhost:5000'
};
```
Adjust the URL if the backend is published behind a different host or gateway.

### RAG Service (`RAG-Service/.env`)
Create a `.env` file alongside `main_langchain.py`:

```
DATABASE_URL=postgresql://<user>:<password>@localhost:5432/eLearning
BACKEND_URL=http://localhost:5000
GEMINI_API_KEY=<your_gemini_key>
RAG_SERVICE_HOST=0.0.0.0
RAG_SERVICE_PORT=8000
DEFAULT_TOP_K=5
EMBEDDING_MODEL=all-MiniLM-L6-v2
```

Ensure the target PostgreSQL instance has the `vector` extension:

```
psql -U <user> -d eLearning -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

## Running the Stack

### 1. Database
1. Create the `eLearning` database in PostgreSQL.
2. Enable the `vector` extension (see above).
3. Spring Boot will apply Flyway migrations on startup.

### 2. Backend API
```powershell
cd Backend
./mvnw clean spring-boot:run
```
- Application listens on `http://localhost:5000` by default.
- Swagger UI: `http://localhost:5000/swagger-ui/index.html`
- Flyway migrations auto-run; ensure the DB user has migration rights.

### 3. Front-end
```powershell
cd Frontend
npm install
npm start
```
- Angular dev server runs on `http://localhost:4200`.
- Update environment files for production builds (`npm run build`).

### 4. RAG Service (optional, required for chatbot features)


https://github.com/user-attachments/assets/08c1c29c-37e4-4f1e-8070-92cd51a4c8b3


```powershell
cd RAG-Service
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
uvicorn main_langchain:app --host 0.0.0.0 --port 8000 --reload
```
- Health check: `http://localhost:8000/health`
- Primary endpoints: `/api/rag/recommend`, `/api/rag/ask-about-course`, `/api/rag/index-course`
- Ensure the backend is reachable at `BACKEND_URL` and shares the same database (or configure differently).

### 5. Launch Order
1. PostgreSQL
2. Spring Boot backend
3. RAG Service (if enabled)
4. Angular front-end

## Testing & Quality Gates
- **Backend**: `cd Backend && ./mvnw test`
  - Integration tests rely on Testcontainers; Docker must be running.
- **Front-end**: `cd Frontend && npm run test`
- **RAG Service**: Add tests with `pytest` (test suite not yet provided).
- Consider adding CI scripts to invoke all three commands.

## Data & Content Management
- **Courses**: Manage via `/api/courses` endpoints. Use multipart requests for creation/update (includes cover images, attachments, etc.).
- **Modules & Lessons**: `/api/modules`, `/api/lessons` endpoints allow CRUD and ordering.
- **Media**: Video uploads go to S3 via the backend, which issues pre-signed URLs and proxy streaming.
- **Commerce**: `/api/payments` and `/api/purchase` handle payment intents, subscriptions, promotion codes, and purchase completion.
- **My Learning**: `/api/my-learning/**` exposes dashboards, progress, expiring subscriptions, and continue-learning lists for authenticated users.
- **Chatbot**: Backend delegates to the RAG service through `RagService` integration; keep the FastAPI service online to power recommendations.

## Indexing Courses for the Chatbot
Whenever a course is created or updated, ensure it is indexed:
1. Backend can call the RAG service automatically (verify integration in `RagService`).
2. Alternatively, trigger manually:
   ```powershell
   curl -X POST http://localhost:8000/api/rag/index-course -H "Content-Type: application/json" -d "{\"course_id\":123,...}"
   ```
3. The `course_embeddings` table stores vector representations; rebuild if embeddings or training data change.

## Production Hardening Checklist
- Move all secrets to environment variables or secret managers.
- Rotate the JWT signing key and update code to read `app.jwt.secret` from configuration.
- Configure HTTPS termination and CORS allowlist in both backend and RAG service.
- Swap S3 credentials for IAM roles when deploying on AWS.
- Set up Stripe webhooks for payment confirmation, not only client-side intents.
- Add monitoring (Spring Actuator, Prometheus, CloudWatch) and log aggregation.
- Run Angular `npm run build` and serve via CDN or web server (Nginx, S3 + CloudFront, etc.).

## Contributing Workflow
- Create feature branches from `master` (default branch).
- Keep commits scoped; run unit tests before pushing.
- Follow existing DTO/Mapper patterns.
- Update Flyway migrations instead of altering existing SQL files.
- Document new endpoints in the README or through OpenAPI annotations.

## Troubleshooting
- **CORS errors**: Confirm Angular `apiUrl` matches backend origin, and backend `@CrossOrigin` or global config permits the front-end host.
- **Video playback issues**: Check S3 bucket CORS, AWS credentials, and the pre-signed URL expiry (`duration` parameter).
- **Payments not completing**: Ensure Stripe keys are set and that the payment intent ID reaches `/api/payments/{id}/process`.
- **Chatbot errors**: Verify Gemini API quota, database `pgvector` extension, and that backend API is reachable from the RAG service.
- **Flyway failures**: Confirm migrations match the actual database state; repair metadata with `./mvnw flyway:repair` if needed.

