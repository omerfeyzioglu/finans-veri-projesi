# Setup Guide

This document provides detailed instructions for setting up the Finans Veri Projesi on your local environment.

## Prerequisites

- Docker Engine (version 20.10.0 or higher)
- Docker Compose (version 2.0.0 or higher)
- Git
- At least 4GB of RAM for Docker
- 10GB of free disk space

## Installation Steps

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/finans-veri-projesi.git
cd finans-veri-projesi
```

### 2. Configure Environment

No configuration changes are typically needed for local development. The default settings in docker-compose.yml are sufficient for most development use cases.

If you need to modify settings:

- **PostgreSQL**: Edit username, password, database name in the `docker-compose.yml` file under the `postgres` service
- **Kafka**: Configure topics and settings in the `docker-compose.yml` file under the `kafka` service
- **OpenSearch**: Adjust memory settings in the `docker-compose.yml` file under the `opensearch` service

### 3. Start the System

```bash
docker-compose up -d
```

This command will:
1. Build or pull all necessary container images
2. Create and start containers for all services
3. Set up the necessary network and volume mounts

To start only specific services:

```bash
docker-compose up -d <service-name> <service-name>
```

Example: `docker-compose up -d postgres kafka redis`

### 4. Verify Services

Check if all services are running:

```bash
docker-compose ps
```

All services should have the "Up" status.

### 5. Access Service Endpoints

- **OpenSearch Dashboards**: http://localhost:5601
- **OpenSearch API**: http://localhost:9200
- **REST API Simulator**: http://localhost:8080
- **TCP Simulator**: tcp://localhost:8081
- **PostgreSQL**: localhost:5432 (connect using a PostgreSQL client)
- **Redis**: localhost:6379 (connect using a Redis client)
- **Kafka**: localhost:19092 (connect using a Kafka client)

## Troubleshooting

### Common Issues

1. **Container fails to start**:
   - Check logs: `docker-compose logs <service-name>`
   - Ensure all ports are available and not used by other applications
   - Verify Docker has sufficient resources (CPU, memory)

2. **OpenSearch doesn't start**:
   - Increase Docker memory limits (at least 4GB recommended)
   - Check OpenSearch logs: `docker-compose logs opensearch`

3. **Filebeat not connecting to OpenSearch**:
   - Verify OpenSearch is fully started
   - Check Filebeat configuration in `filebeat/filebeat.yml`
   - Examine Filebeat logs: `docker-compose logs filebeat`

4. **PostgreSQL connection issues**:
   - Check environment variables in the service that's trying to connect
   - Verify PostgreSQL logs: `docker-compose logs postgres`

## Maintenance

### Stopping Services

```bash
docker-compose down
```

To remove all data (volumes):

```bash
docker-compose down -v
```

### Updating Services

```bash
git pull
docker-compose build
docker-compose up -d
```

### Viewing Logs

```bash
docker-compose logs -f <service-name>
```

Example: `docker-compose logs -f main-application`

### Backup and Restore

**PostgreSQL Backup**:
```bash
docker exec -t postgres_db pg_dump -U rateuser ratesdb > backup.sql
```

**PostgreSQL Restore**:
```bash
cat backup.sql | docker exec -i postgres_db psql -U rateuser -d ratesdb
``` 