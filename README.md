# AI-Powered Incident Triage and Root-Cause Assistant

An AI-powered developer tool that analyzes application logs and stack traces to identify probable root causes and recommend debugging solutions.

## Features
- Application log and stack-trace analysis
- Automatic incident category and severity classification
- AI-powered root-cause explanations using Google Gemini
- Recommended debugging and prevention strategies
- Fallback analysis when the AI service is unavailable
- Incident history and analysis storage

## Technology Stack

**Frontend:** React, Vite, Bootstrap, Axios

**Backend:** Java 21, Spring Boot 3.3.4, REST APIs

**Database:** PostgreSQL

**ORM:** Spring Data JPA, Hibernate

**AI Integration:** Google Gemini API

**Tools:** Maven, Swagger, JUnit

## Architecture

React Frontend → Spring Boot REST API → Log Processing and Classification → Gemini AI → PostgreSQL → Analysis Results

## How It Works
1. Developer submits application logs or stack traces.
2. Backend extracts exception and stack-trace information.
3. Rules classify the incident category and severity.
4. Gemini generates probable root causes and recommendations.
5. Results are saved in PostgreSQL.
6. React displays the analysis.
