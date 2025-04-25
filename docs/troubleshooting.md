# Troubleshooting Guide

This document provides solutions for common issues encountered in the Finans Veri Projesi.

## Filebeat to OpenSearch Integration Issues

### Problem: Filebeat Not Sending Logs to OpenSearch

**Symptoms**:
- No logs appearing in OpenSearch
- No indices created with the pattern "finans-*"
- Filebeat container running but not shipping data

**Possible Causes and Solutions**:

1. **Incorrect Filebeat Configuration**
   
   **Check**: Examine the `filebeat.yml` configuration file
   
   **Solution**: Ensure these settings are correct:
   ```yaml
   output.elasticsearch:
     hosts: ["http://opensearch:9200"]
     protocol: "http"
     ssl.enabled: false
     index: "finans-%{[agent.version]}-%{+yyyy.MM.dd}"
   ```

2. **OpenSearch Not Ready**
   
   **Check**: Verify if OpenSearch is ready to accept connections
   
   **Solution**: 
   - Check OpenSearch status: `curl http://localhost:9200/_cat/health`
   - Restart Filebeat after OpenSearch is fully started: `docker-compose restart filebeat`

3. **Network Connectivity Issues**
   
   **Check**: Test connectivity between Filebeat and OpenSearch
   
   **Solution**:
   ```bash
   docker exec -it filebeat ping opensearch
   docker exec -it filebeat curl -I http://opensearch:9200
   ```

4. **Index Template Issues**
   
   **Check**: Verify if index templates are created properly
   
   **Solution**:
   ```bash
   # Check existing templates
   curl http://localhost:9200/_cat/templates
   
   # Ensure setup.template settings are correct in filebeat.yml:
   setup.template.enabled: true
   setup.template.name: "finans"
   setup.template.pattern: "finans-*"
   ```

5. **Log Level and Path Issues**
   
   **Check**: Ensure Filebeat can access and read the Docker logs
   
   **Solution**:
   - Verify volume mounts in docker-compose.yml
   - Check Filebeat has proper permissions
   - Set logging level to debug in filebeat.yml: `logging.level: debug`

6. **JSON Parsing Issues**
   
   **Check**: If there are JSON parsing errors in Filebeat logs
   
   **Solution**:
   - Enable/disable the JSON decoder depending on your log format
   - Adjust the JSON decoder settings if needed:
     ```yaml
     processors:
       - decode_json_fields:
           fields: ["message"]
           target: ""
           overwrite_keys: true
     ```

## Kafka Connection Issues

### Problem: Services Cannot Connect to Kafka

**Symptoms**:
- Connection errors in application logs
- No messages being produced/consumed
- Kafka-related exceptions

**Solutions**:

1. **Check Kafka Status**:
   ```bash
   docker-compose ps kafka
   docker-compose logs kafka
   ```

2. **Verify Network Settings**:
   - Ensure correct host and port in application configs
   - Check that the Kafka advertised listeners are properly set

3. **Test Kafka Connection**:
   ```bash
   # Install kafkacat for testing
   docker run --network=finans-network -it edenhill/kcat:1.7.0 -b kafka:9092 -L
   ```

## Redis Connection Issues

### Problem: Application Cannot Connect to Redis

**Symptoms**:
- Redis connection errors in logs
- Cache operations failing
- Application performance degradation

**Solutions**:

1. **Check Redis Status**:
   ```bash
   docker-compose ps redis
   docker-compose logs redis
   ```

2. **Verify Redis Connection**:
   ```bash
   # Connect to Redis CLI
   docker exec -it redis_cache redis-cli ping
   ```

3. **Check Redis Configuration**:
   - Ensure RedisConfig.java correctly configures Redis template
   - Verify Redis host and port settings

## Database Issues

### Problem: PostgreSQL Connection or Query Failures

**Symptoms**:
- SQL exceptions in application logs
- Failed database operations
- Missing or inconsistent data

**Solutions**:

1. **Check PostgreSQL Status**:
   ```bash
   docker-compose ps postgres
   docker-compose logs postgres
   ```

2. **Verify Database Connection**:
   ```bash
   docker exec -it postgres_db psql -U rateuser -d ratesdb -c "SELECT version();"
   ```

3. **Check Database Schema**:
   ```bash
   docker exec -it postgres_db psql -U rateuser -d ratesdb -c "\dt"
   ```

4. **Verify Environment Variables**:
   - Ensure database connection properties are correctly set in application configs
   - Check Spring datasource settings in the relevant application.properties or application.yml

## General Application Issues

### Problem: Container Fails to Start

**Solutions**:

1. **Check Container Logs**:
   ```bash
   docker-compose logs <service-name>
   ```

2. **Verify Resource Usage**:
   ```bash
   docker stats
   ```

3. **Check for Port Conflicts**:
   ```bash
   netstat -tuln | grep <port-number>
   ```

4. **Inspect Container**:
   ```bash
   docker inspect <container-id>
   ``` 