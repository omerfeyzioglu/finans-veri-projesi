FROM docker.elastic.co/beats/filebeat-oss:7.12.1

USER root

# Filebeat yapılandırma dosyasını oluştur
RUN echo '# Filebeat Inputs' > /usr/share/filebeat/filebeat.yml && \
    echo 'filebeat.inputs:' >> /usr/share/filebeat/filebeat.yml && \
    echo '- type: container' >> /usr/share/filebeat/filebeat.yml && \
    echo '  paths:' >> /usr/share/filebeat/filebeat.yml && \
    echo '    - /var/lib/docker/containers/*/*.log' >> /usr/share/filebeat/filebeat.yml && \
    echo '  multiline.type: pattern' >> /usr/share/filebeat/filebeat.yml && \
    echo '  multiline.pattern: ^\{' >> /usr/share/filebeat/filebeat.yml && \
    echo '  multiline.negate: true' >> /usr/share/filebeat/filebeat.yml && \
    echo '  multiline.match: after' >> /usr/share/filebeat/filebeat.yml && \
    echo '' >> /usr/share/filebeat/filebeat.yml && \
    echo '# Processors' >> /usr/share/filebeat/filebeat.yml && \
    echo 'processors:' >> /usr/share/filebeat/filebeat.yml && \
    echo '  - add_docker_metadata:' >> /usr/share/filebeat/filebeat.yml && \
    echo '      host: "unix:///var/run/docker.sock"' >> /usr/share/filebeat/filebeat.yml && \
    echo '  - add_host_metadata:' >> /usr/share/filebeat/filebeat.yml && \
    echo '      when.not.contains.tags: forwarded' >> /usr/share/filebeat/filebeat.yml && \
    echo '  - decode_json_fields:' >> /usr/share/filebeat/filebeat.yml && \
    echo '      fields: ["message"]' >> /usr/share/filebeat/filebeat.yml && \
    echo '      target: ""' >> /usr/share/filebeat/filebeat.yml && \
    echo '      overwrite_keys: true' >> /usr/share/filebeat/filebeat.yml && \
    echo '      add_error_key: true' >> /usr/share/filebeat/filebeat.yml && \
    echo '' >> /usr/share/filebeat/filebeat.yml && \
    echo '# Elasticsearch Output (OpenSearch ile uyumlu)' >> /usr/share/filebeat/filebeat.yml && \
    echo 'output.elasticsearch:' >> /usr/share/filebeat/filebeat.yml && \
    echo '  hosts: ["http://opensearch:9200"]' >> /usr/share/filebeat/filebeat.yml && \
    echo '  protocol: "http"' >> /usr/share/filebeat/filebeat.yml && \
    echo '  ssl.enabled: false' >> /usr/share/filebeat/filebeat.yml && \
    echo '  index: "finans-%{[agent.version]}-%{+yyyy.MM.dd}"' >> /usr/share/filebeat/filebeat.yml && \
    echo '' >> /usr/share/filebeat/filebeat.yml && \
    echo '# Template ve ILM Ayarları' >> /usr/share/filebeat/filebeat.yml && \
    echo 'setup.template.enabled: true' >> /usr/share/filebeat/filebeat.yml && \
    echo 'setup.template.name: "finans"' >> /usr/share/filebeat/filebeat.yml && \
    echo 'setup.template.pattern: "finans-*"' >> /usr/share/filebeat/filebeat.yml && \
    echo 'setup.ilm.enabled: false' >> /usr/share/filebeat/filebeat.yml && \
    echo '' >> /usr/share/filebeat/filebeat.yml && \
    echo '# Logging Ayarları' >> /usr/share/filebeat/filebeat.yml && \
    echo 'logging.level: debug' >> /usr/share/filebeat/filebeat.yml && \
    echo 'logging.to_files: false' >> /usr/share/filebeat/filebeat.yml

# Log klasörünü oluştur
RUN mkdir -p /var/log/filebeat && chmod 0755 /var/log/filebeat

# Dosya izinlerini ayarla
RUN chown root:root /usr/share/filebeat/filebeat.yml && chmod go-w /usr/share/filebeat/filebeat.yml

# Filebeat'i başlat
CMD ["filebeat", "-e", "-strict.perms=false"]
