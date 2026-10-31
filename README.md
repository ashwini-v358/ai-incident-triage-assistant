# TraceWise AI
### Intelligent Incident Diagnosis and Root-Cause Analysis Platform

TraceWise AI is a full-stack, AI-powered incident analysis platform designed to help developers investigate application failures. It analyzes application logs and stack traces, identifies incident categories and severity levels, and uses Google Gemini to generate evidence-based probable root causes and debugging recommendations.

The platform combines deterministic log analysis with AI-assisted diagnosis to make troubleshooting more structured, understandable, and efficient.

---

## 1. Key Features

- **Log and Stack Trace Analysis:** Accepts application logs and extracts relevant exception details, root-cause messages, and stack frames.
- **Automated Incident Classification:** Categorizes incidents and assigns severity levels using predefined analysis rules.
- **AI-Powered Root-Cause Analysis:** Integrates Google Gemini to identify probable causes based on the submitted log evidence.
- **Evidence-Based Recommendations:** Generates debugging suggestions, recommended fixes, and prevention strategies.
- **Fallback Analysis:** Provides deterministic analysis when the Gemini service is unavailable or returns an unusable response.
- **Incident History:** Stores analyzed incidents and their results in PostgreSQL for future reference.
- **Interactive Dashboard:** Provides a React-based interface to submit logs, view analysis results, and review previous incidents.
- **API Documentation:** Exposes backend REST APIs through Swagger/OpenAPI.

---

## 2. Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React.js, JavaScript, Vite, Bootstrap, Axios |
| Backend | Java 21, Spring Boot 3.3.4, Spring Web |
| Database | PostgreSQL |
| Persistence | Spring Data JPA, Hibernate |
| AI Integration | Google Gemini API |
| API Communication | REST APIs, JSON |
| API Documentation | Swagger / OpenAPI |
| Testing | JUnit |
| Build Tool | Maven |

---

## 3. System Architecture

TraceWise AI follows a layered client-server architecture. The React frontend communicates with the Spring Boot backend through REST APIs. The backend coordinates log processing, incident classification, AI-based diagnosis, and database persistence.

### High-Level Architecture

```text
                  ┌─────────────────────────────┐
                  │          DEVELOPER          │
                  │                             │
                  │  Submits Application Logs  │
                  │       / Stack Trace         │
                  └──────────────┬──────────────┘
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │       PRESENTATION LAYER    │
                  │                             │
                  │        React Dashboard      │
                  │                             │
                  │  • Log Submission           │
                  │  • Analysis Results         │
                  │  • Incident History        │
                  └──────────────┬──────────────┘
                                 │
                           REST API / JSON
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │       BACKEND LAYER         │
                  │                             │
                  │       Spring Boot           │
                  │                             │
                  │  • REST Controllers        │
                  │  • Request Handling        │
                  │  • Service Coordination    │
                  └──────────────┬──────────────┘
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │       ANALYSIS LAYER        │
                  │                             │
                  │  1. Log Parsing             │
                  │  2. Exception Extraction    │
                  │  3. Stack Frame Extraction  │
                  │  4. Incident Classification │
                  │  5. Severity Assessment     │
                  └──────────────┬──────────────┘
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │       AI DIAGNOSIS LAYER    │
                  │                             │
                  │       Google Gemini API     │
                  │                             │
                  │  • Probable Root Cause      │
                  │  • Evidence Analysis        │
                  │  • Debugging Recommendations│
                  │  • Prevention Strategies    │
                  │  • Confidence Score         │
                  └──────────────┬──────────────┘
                                 │
                         Analysis Response
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │      PERSISTENCE LAYER      │
                  │                             │
                  │    Spring Data JPA         │
                  │        Hibernate           │
                  │                             │
                  │       PostgreSQL           │
                  │                             │
                  │  Stores Incident Details   │
                  │  and Analysis Results      │
                  └──────────────┬──────────────┘
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │       RESPONSE LAYER       │
                  │                             │
                  │  Structured Analysis JSON  │
                  │  Returned to React UI      │
                  └──────────────┬──────────────┘
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │       DEVELOPER             │
                  │                             │
                  │ Views Diagnosis, Evidence,  │
                  │ Fixes and Incident History  │
                  └─────────────────────────────┘
```

### Architecture Layer Responsibilities

| Layer | Responsibility |
|---|---|
| Presentation Layer | Collects application logs and displays incident analysis and history. |
| REST API Layer | Receives frontend requests and returns structured JSON responses. |
| Service Layer | Coordinates log processing, classification, AI integration, and persistence. |
| Analysis Layer | Extracts exceptions and stack frames, then determines incident category and severity. |
| AI Integration Layer | Communicates with Gemini to generate probable root causes and recommendations. |
| Persistence Layer | Stores incident information and analysis results in PostgreSQL through JPA/Hibernate. |

---

## 4. Complete Project Workflow

The system processes an incident through the following stages:

### Step 1: Log Submission

The developer opens the React dashboard and submits an application log or stack trace for analysis.

- The frontend collects the log input.
- Axios sends the request to the Spring Boot backend.
- The backend receives the request through the incident analysis REST endpoint.

### Step 2: Log Parsing and Information Extraction

The backend processes the submitted log and extracts relevant diagnostic information.

The parsing process identifies:

- Exception type.
- Root-cause or `Caused by` message.
- Stack trace frames.
- Class and method names.
- Source file and line number, when available.

This extracted information provides structured input for the next analysis stages.

### Step 3: Incident Classification and Severity Assessment

The system applies predefined rule-based logic to classify the incident.

It identifies the relevant incident category and assigns a severity level based on the implemented classification rules.

Examples of incident categories include:

- Database-related errors.
- Application exceptions.
- Runtime failures.
- Other supported error categories.

This stage provides a consistent initial classification before AI-assisted diagnosis.

### Step 4: AI-Based Root-Cause Analysis

The backend sends the relevant log information and extracted diagnostic context to the Google Gemini API.

Gemini analyzes the provided information and generates a structured response containing:

- Probable root cause.
- Supporting evidence from the logs.
- Recommended debugging and resolution steps.
- Prevention strategies.
- Confidence score.

The AI-generated diagnosis is intended to assist developers in understanding the likely source of an issue.

### Step 5: Fallback Analysis

The system includes a deterministic fallback mechanism.

If the Gemini service is unavailable, times out, or returns an unusable response, the backend uses fallback analysis instead of depending entirely on the external AI service.

This allows the platform to provide a basic incident analysis even when AI integration is unsuccessful.

### Step 6: Database Persistence

After analysis, the backend stores the incident information and its corresponding analysis results in PostgreSQL.

Spring Data JPA and Hibernate handle the persistence operations.

This allows previously analyzed incidents to be retrieved without requiring developers to submit the same logs again.

### Step 7: Result Presentation

The backend returns the structured analysis response to the React frontend.

The dashboard displays:

- Incident category.
- Severity level.
- Extracted exception details.
- Probable root cause.
- Supporting evidence.
- Recommended fixes.
- Prevention strategies.
- Confidence score.
- Incident history.

The developer can use this information as a starting point for investigating and resolving the application issue.

---

## 5. Backend Processing Flow

The backend coordinates the incident analysis process through its REST API and service components.

```text
Incoming REST Request
        |
        v
Incident Controller
        |
        v
Incident Analysis Service
        |
        v
Log Parser
        |
        v
Extract Diagnostic Information
        |
        v
Rule-Based Classification
        |
        v
Severity Assessment
        |
        v
Gemini AI Integration
        |
        +----------------------+
        |                      |
        v                      v
  Valid AI Response      AI Unavailable /
        |                Unusable Response
        |                      |
        v                      v
  AI Diagnosis          Deterministic
                        Fallback Analysis
        |                      |
        +----------+-----------+
                   |
                   v
          Persist Incident Data
                   |
                   v
          PostgreSQL Database
                   |
                   v
          Structured JSON Response
                   |
                   v
             React Dashboard
```

---

## 6. AI Integration

TraceWise AI integrates the Google Gemini API to provide contextual incident diagnosis.

Instead of relying only on predefined error messages, the system sends extracted log information to the AI service for further analysis.

The AI integration is designed to generate:

1. **Probable Root Cause:** An explanation of what may have caused the incident.
2. **Evidence:** Relevant information from the submitted logs supporting the diagnosis.
3. **Recommended Fixes:** Suggested debugging and resolution steps.
4. **Prevention Strategies:** Recommendations to reduce the likelihood of similar issues.
5. **Confidence Score:** An indication of the confidence associated with the generated diagnosis.

The backend manages communication with Gemini, keeping the API key outside the frontend application.

**Important:** AI-generated diagnoses are recommendations based on the available log evidence. They should be reviewed and validated by a developer.

---

## 7. Database and Persistence

PostgreSQL is used to maintain incident records and their analysis results.

The persistence layer uses:

- **Spring Data JPA:** Provides repository abstractions for database operations.
- **Hibernate:** Handles object-relational mapping between Java entities and database tables.
- **PostgreSQL:** Stores incident and analysis information persistently.

The stored information supports incident-history retrieval and review of previously analyzed failures.

---

## 8. REST API

The backend exposes REST endpoints for frontend communication and incident analysis.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/incidents/analyze` | Submit logs and generate incident analysis. |
| GET | `/api/v1/health` | Check backend application health. |

Swagger/OpenAPI is integrated to document and explore the available backend APIs.

**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

## 9. Project Structure

The application is organized into separate frontend and backend modules.

```text
TraceWise-AI/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── .../
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── parser/
│   │   │   │       ├── repository/
│   │   │   │       ├── entity/
│   │   │   │       ├── dto/
│   │   │   │       └── config/
│   │   │   │
│   │   │   └── resources/
│   │   │
│   │   └── test/
│   │
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   └── ...
│   │
│   └── package.json
│
└── README.md
---

## 10. Testing and Verification

The project includes backend unit tests using JUnit.

The application has also been verified through:

- Maven build and backend test execution.
- Frontend production build.
- Backend health endpoint.
- REST API-based incident analysis.
- Gemini integration and deterministic fallback behavior.

---

## 11. Current Project Scope

TraceWise AI is designed as an **incident diagnosis and debugging recommendation platform**.

It analyzes submitted logs, identifies potential causes, and provides recommendations to assist developers.

The current implementation does not automatically modify source code, apply patches, restart services, or deploy fixes. Developers remain responsible for reviewing and implementing the suggested solutions.

---
