# Finans Veri Projesi Documentation

Welcome to the documentation for the Finans Veri Projesi, a comprehensive financial data processing platform.

## Documentation Sections

- [Architecture](architecture.md) - Overview of the system architecture and data flow
- [Components](components.md) - Detailed description of each component and its responsibilities
- [Setup Guide](setup.md) - Instructions for setting up and running the system
- [API Usage Guide](api-usage.md) - How to access REST APIs and TCP data streams
- [Troubleshooting](troubleshooting.md) - Solutions for common issues
- [Filebeat-OpenSearch Fix](filebeat-opensearch-fix.md) - Specific solution for the Filebeat to OpenSearch integration issue

## Quick Start

For a quick start, refer to the [Setup Guide](setup.md) to get the system running in minutes.

## Project Overview

The Finans Veri Projesi is a distributed system designed for collecting, processing, and storing financial data. Key features include:

- Real-time data collection from multiple sources
- In-memory caching with Redis for fast data access
- Event streaming with Kafka for reliable data processing
- Persistent storage in PostgreSQL
- Comprehensive logging and monitoring with OpenSearch

## Architecture Diagram

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