# Finans Veri Projesi

A distributed financial data processing system for collecting, processing, and storing real-time financial data with scalable components.

## Project Overview

This project implements a comprehensive financial data processing pipeline that:

1. Collects financial data from multiple sources (TCP and REST APIs)
2. Processes and transforms the data
3. Caches processed data in Redis
4. Streams events through Kafka
5. Persists data to PostgreSQL
6. Provides observability through OpenSearch and Filebeat

## Architecture

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

## Components

### Data Sources
- **TCP Simulator**: Streams financial data via TCP connections
- **REST Simulator**: Provides financial data through REST APIs

### Processing & Caching
- **Main Application**: Core application that processes data from both sources
- **Redis**: In-memory cache for fast data access

### Event Streaming & Persistence
- **Kafka**: Message broker for reliable data streaming
- **Kafka Consumer**: Consumes Kafka messages and persists data to PostgreSQL
- **PostgreSQL**: Relational database for persistent storage

### Observability
- **Filebeat**: Collects and ships logs to OpenSearch
- **OpenSearch**: Search and analytics engine for log management
- **OpenSearch Dashboards**: Visualization interface for OpenSearch data

## Getting Started

### Prerequisites
- Docker and Docker Compose

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/finans-veri-projesi.git
   cd finans-veri-projesi
   ```

2. Start the system:
   ```
   docker-compose up -d
   ```

3. Access services:
   - OpenSearch Dashboards: http://localhost:5601
   - REST API: http://localhost:8080
   - TCP Simulator: localhost:8081
   - PostgreSQL: localhost:5432
   - Redis: localhost:6379
   - Kafka: localhost:19092
   - OpenSearch: http://localhost:9200

## Using Data Sources

### REST API Examples

Get all rates:
```bash
curl -X GET http://localhost:8080/api/rates
```

Get specific rate:
```bash
curl -X GET http://localhost:8080/api/rates/USDTRY
```

### TCP Connection Examples

Connect with telnet:
```bash
telnet localhost 8081
```

Connect with netcat:
```bash
nc localhost 8081
```

For more detailed examples, please see the [API Usage Guide](docs/api-usage.md).

## Configuration

Each component is configured in the docker-compose.yml file. Key configurations include:

- **Database**: PostgreSQL settings (credentials, database name)
- **Kafka**: Topics, replication, listeners
- **OpenSearch**: Security settings, memory allocation
- **Filebeat**: Log collection and shipping configuration

## Development

The project is structured into multiple microservices, each in its own directory:

- `main-application/`: Core processing application
- `tcp-platform-app/`: TCP data simulator
- `spring-platform-2/`: REST API simulator
- `kafka-consumer-db/`: Kafka consumer for database operations
- `filebeat/`: Log collection configuration
- `opensearch-config/`: OpenSearch configuration

## Troubleshooting

### Filebeat to OpenSearch Integration

If Filebeat is not sending data to OpenSearch, check:

1. Filebeat configuration in `filebeat/filebeat.yml`:
   - Ensure the hosts are correctly configured
   - Verify SSL settings

2. OpenSearch accessibility:
   - Check if OpenSearch is running: `docker-compose ps opensearch`
   - Verify network connectivity: `docker exec -it filebeat ping opensearch`

3. Filebeat logs:
   - Check Filebeat logs: `docker-compose logs filebeat`

## Documentation

See the [docs](docs/) directory for more detailed documentation:

- [Architecture](docs/architecture.md)
- [Component Details](docs/components.md)
- [Setup Guide](docs/setup.md)
- [API Usage Guide](docs/api-usage.md)
- [Troubleshooting](docs/troubleshooting.md) 