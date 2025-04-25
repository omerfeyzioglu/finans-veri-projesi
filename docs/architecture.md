# System Architecture

## Overview

The Finans Veri Projesi is built with a microservices architecture, allowing for modularity, scalability, and isolated development. This document details the key components and how they interact.

## Component Diagram

```
┌─────────────┐   ┌─────────────┐
│  TCP Data   │   │  REST API   │
│  Simulator  │   │  Simulator  │
└──────┬──────┘   └──────┬──────┘
       │                 │
       │                 │
       ▼                 ▼
┌──────────────────────────────┐
│                              │
│       Main Application       │
│                              │
└─┬───────────────┬────────┬───┘
  │               │        │
  │               │        │
  ▼               ▼        ▼
┌─────┐    ┌────────────┐  ┌─────────────┐
│Redis│    │   Kafka    │  │   Filebeat  │
└─────┘    └──────┬─────┘  └──────┬──────┘
                  │               │
                  ▼               ▼
           ┌────────────┐  ┌────────────┐
           │ PostgreSQL │  │ OpenSearch │
           └────────────┘  └────────────┘
```

## Data Flow

1. **Data Generation**:
   - TCP Simulator generates financial data and streams it via TCP
   - REST API Simulator provides financial data through REST APIs

2. **Data Processing**:
   - Main Application consumes data from both sources
   - Performs data validation, transformation, and calculation
   - Caches processed data in Redis for fast access
   - Produces events to Kafka for further processing and persistence

3. **Data Storage**:
   - Kafka Consumer reads events from Kafka 
   - Persists data to PostgreSQL for long-term storage
   - Redis holds recent data for quick access and calculations

4. **Monitoring & Observability**:
   - Filebeat collects logs from all containers
   - Sends logs to OpenSearch for indexing and analysis
   - OpenSearch Dashboards provides visualization and monitoring

## Communication Patterns

### Synchronous Communication
- Main Application to REST API: HTTP/REST calls
- User interfaces to APIs: HTTP/REST calls

### Asynchronous Communication
- Main Application to Kafka: Event production
- Kafka to Consumer: Event consumption
- TCP Simulator to Main Application: TCP sockets

## Infrastructure

All components are containerized using Docker, with orchestration handled by Docker Compose. This provides:

- Isolated runtime environments
- Consistent deployment across environments
- Easy scaling of individual components
- Simplified dependency management

## Technology Stack

- **Programming Languages**: Java, Spring Boot
- **Data Storage**: PostgreSQL, Redis
- **Message Broker**: Kafka
- **Log Management**: Filebeat, OpenSearch
- **Containerization**: Docker, Docker Compose 