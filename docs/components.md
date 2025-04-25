# Component Documentation

This document describes the key components of the Finans Veri Projesi, their responsibilities, and how they interact.

## Data Sources

### TCP Simulator (`tcp-platform-app`)

A TCP server that generates and streams financial data.

**Responsibilities**:
- Generate simulated financial data (rates, prices, etc.)
- Stream data over TCP socket connections
- Support multiple simultaneous clients
- Configurable data generation rates

**Technologies**:
- Java
- TCP/IP sockets
- Spring Boot

### REST API Simulator (`spring-platform-2`)

A REST API that provides financial data on demand.

**Responsibilities**:
- Expose REST endpoints for financial data retrieval
- Generate simulated financial data
- Support filtering and query parameters
- Provide both real-time and historical data

**Technologies**:
- Java
- Spring Boot
- Spring Web
- RESTful API design

## Core Processing

### Main Application (`main-application`)

The central processing component that receives, processes, and dispatches data.

**Responsibilities**:
- Connect to and consume data from TCP and REST sources
- Process and validate incoming financial data
- Cache processed data in Redis
- Produce events to Kafka
- Coordinate data flow between components

**Technologies**:
- Java
- Spring Boot
- Redis client
- Kafka producer
- TCP client
- REST client

## Data Storage

### Redis Cache (`redis`)

In-memory data structure store used for caching and quick access.

**Responsibilities**:
- Cache processed financial data
- Provide fast access to recent data
- Support data expiration policies

**Technologies**:
- Redis server
- Key-value storage
- Data structures (strings, hashes, lists)

### PostgreSQL Database (`postgres`)

Relational database for persistent storage of financial data.

**Responsibilities**:
- Store processed financial data long-term
- Support complex queries
- Enable data analysis
- Provide data persistence

**Technologies**:
- PostgreSQL
- SQL
- Relational data model

## Event Processing

### Kafka Message Broker (`kafka`)

Distributed event streaming platform for high-throughput data pipelines.

**Responsibilities**:
- Receive and store events from the main application
- Distribute events to consumers
- Provide durability and scalability
- Enable event replay

**Technologies**:
- Apache Kafka
- Zookeeper (for Kafka coordination)
- Topic-based publish/subscribe model

### Kafka Consumer (`kafka-consumer-db`)

Processes Kafka events and persists data to PostgreSQL.

**Responsibilities**:
- Consume events from Kafka topics
- Process and transform event data if needed
- Persist data to PostgreSQL
- Handle error conditions and retries

**Technologies**:
- Java
- Spring Boot
- Kafka consumer client
- JDBC/JPA for database access

## Observability

### Filebeat (`filebeat`)

Log collector that ships logs to OpenSearch.

**Responsibilities**:
- Collect container logs from Docker
- Process and transform log data
- Ship logs to OpenSearch
- Monitor log file changes

**Technologies**:
- Filebeat
- Log processing
- Docker integration

### OpenSearch (`opensearch`)

Search and analytics engine for log storage and analysis.

**Responsibilities**:
- Store and index logs
- Enable full-text search
- Support complex queries and aggregations
- Provide data visualization

**Technologies**:
- OpenSearch (Elasticsearch fork)
- RESTful API
- JSON-based queries

### OpenSearch Dashboards (`opensearch-dashboards`)

Visualization and management interface for OpenSearch.

**Responsibilities**:
- Visualize log data
- Create and share dashboards
- Monitor system health
- Configure OpenSearch

**Technologies**:
- OpenSearch Dashboards (Kibana fork)
- Web interface
- Data visualization
- User management 