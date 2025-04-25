# Fixing Filebeat to OpenSearch Integration

This guide addresses the issue with Filebeat not successfully sending logs to OpenSearch in the Finans Veri Projesi.

## Problem

Filebeat is running but logs are not being sent to OpenSearch, preventing proper log aggregation and analysis.

## Diagnosis

After examining the configuration files and logs, several potential issues have been identified:

1. **Configuration Mismatch**: The configuration in `filebeat.yml` and the Dockerfile are different, leading to conflicting settings.
2. **JSON Decoding**: The JSON decoder is commented out in the configuration file but enabled in the Dockerfile.
3. **Container Naming**: There's inconsistency in container naming (the container_name is commented out in docker-compose.yml).
4. **Log Settings**: The logging settings differ between the Dockerfile and configuration file.

## Solution

Apply the following fixes to resolve the integration issues:

### 1. Fix filebeat.yml Configuration

Edit `filebeat/filebeat.yml` to ensure proper JSON decoding:

```yaml
filebeat.inputs:
  - type: container
    paths:
      - '/var/lib/docker/containers/*/*.log'

processors:
  - add_docker_metadata:
      host: "unix:///var/run/docker.sock"
  - add_host_metadata:
      when.not.contains.tags: forwarded
  - decode_json_fields:
      fields: ["message"]
      target: ""
      overwrite_keys: true
      add_error_key: true

# Elasticsearch output (compatible with OpenSearch)
output.elasticsearch:
  hosts: ["http://opensearch:9200"]
  protocol: "http"
  ssl.enabled: false
  index: "finans-%{[agent.version]}-%{+yyyy.MM.dd}"

setup.template.enabled: true
setup.template.name: "finans"
setup.template.pattern: "finans-*"
setup.ilm.enabled: false

# Logging 
logging.level: debug
logging.to_files: true
logging.files:
  path: /var/log/filebeat
  name: filebeat
  keepfiles: 7
  permissions: 0644
```

### 2. Fix Docker Compose Configuration

Ensure the Filebeat container is properly configured in `docker-compose.yml`:

```yaml
filebeat:
  build:
    context: ./filebeat
  container_name: filebeat
  user: root
  command: filebeat -e -strict.perms=false
  volumes:
    - /var/lib/docker/containers:/var/lib/docker/containers:ro
    - /var/run/docker.sock:/var/run/docker.sock:rw
  depends_on:
    - opensearch
  restart: unless-stopped
  networks:
    - finans-network
```

### 3. Validate OpenSearch Health

Before troubleshooting Filebeat, ensure OpenSearch is healthy:

```bash
# Check OpenSearch health
curl -X GET "http://localhost:9200/_cluster/health?pretty"
```

Expected output should show status green or yellow:
```json
{
  "cluster_name" : "opensearch-cluster",
  "status" : "yellow",
  "timed_out" : false,
  ...
}
```

### 4. Restart Services

After making configuration changes, restart the services:

```bash
# Restart OpenSearch and Filebeat
docker-compose restart opensearch
docker-compose restart filebeat

# View Filebeat logs to verify proper startup
docker-compose logs -f filebeat
```

### 5. Verify Integration

Check if indices are being created in OpenSearch:

```bash
# List indices
curl -X GET "http://localhost:9200/_cat/indices?v"
```

You should see indices with the pattern `finans-*`.

## Additional Considerations

1. **Memory Settings**: If OpenSearch is failing due to memory constraints, adjust the Java heap size:
   ```yaml
   environment:
     - "OPENSEARCH_JAVA_OPTS=-Xms1g -Xmx1g"
   ```

2. **Template Creation**: If templates aren't being created, you can manually load them:
   ```bash
   curl -X PUT "http://localhost:9200/_template/finans" -H 'Content-Type: application/json' -d '{
     "index_patterns": ["finans-*"],
     "settings": {
       "number_of_shards": 1
     },
     "mappings": {
       "properties": {
         "@timestamp": { "type": "date" },
         "message": { "type": "text" }
       }
     }
   }'
   ```

3. **Permissions**: Ensure Filebeat has proper permissions to read log files:
   ```bash
   docker exec -it filebeat chmod -R 644 /var/lib/docker/containers
   ```

By following these steps, Filebeat should successfully integrate with OpenSearch, allowing for proper log aggregation and analysis. 