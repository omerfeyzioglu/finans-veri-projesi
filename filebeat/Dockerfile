FROM docker.elastic.co/beats/filebeat-oss:7.12.1

# Dosya sistemindeki haklarla ilgili sorunları önlemek için
# yapılandırma dosyasını Dockerfile'da oluşturuyoruz
USER root

# Yapılandırma dosyasını oluştur
RUN echo 'filebeat.inputs:' > /usr/share/filebeat/filebeat.yml && \
    echo '- type: container' >> /usr/share/filebeat/filebeat.yml && \
    echo '  paths:' >> /usr/share/filebeat/filebeat.yml && \
    echo '    - /var/lib/docker/containers/*/*.log' >> /usr/share/filebeat/filebeat.yml && \
    echo '' >> /usr/share/filebeat/filebeat.yml && \
    echo 'processors:' >> /usr/share/filebeat/filebeat.yml && \
    echo '  - add_docker_metadata:' >> /usr/share/filebeat/filebeat.yml && \
    echo '      host: "unix:///var/run/docker.sock"' >> /usr/share/filebeat/filebeat.yml && \
    echo '  - add_host_metadata:' >> /usr/share/filebeat/filebeat.yml && \
    echo '      when.not.contains.tags: forwarded' >> /usr/share/filebeat/filebeat.yml && \
    echo '  - decode_json_fields:' >> /usr/share/filebeat/filebeat.yml && \
    echo '      fields: ["message"]' >> /usr/share/filebeat/filebeat.yml && \
    echo '      target: ""' >> /usr/share/filebeat/filebeat.yml && \
    echo '      overwrite_keys: true' >> /usr/share/filebeat/filebeat.yml && \
    echo '' >> /usr/share/filebeat/filebeat.yml && \
    echo 'output.elasticsearch:' >> /usr/share/filebeat/filebeat.yml && \
    echo '  hosts: ["http://opensearch:9200"]' >> /usr/share/filebeat/filebeat.yml && \
    echo '  protocol: "http"' >> /usr/share/filebeat/filebeat.yml && \
    echo '  ssl.enabled: false' >> /usr/share/filebeat/filebeat.yml && \
    echo '  index: "finans-%{[agent.version]}-%{+yyyy.MM.dd}"' >> /usr/share/filebeat/filebeat.yml && \
    echo '' >> /usr/share/filebeat/filebeat.yml && \
    echo 'setup.template.enabled: true' >> /usr/share/filebeat/filebeat.yml && \
    echo 'setup.template.name: "finans"' >> /usr/share/filebeat/filebeat.yml && \
    echo 'setup.template.pattern: "finans-*"' >> /usr/share/filebeat/filebeat.yml && \
    echo 'setup.ilm.enabled: false' >> /usr/share/filebeat/filebeat.yml && \
    echo '' >> /usr/share/filebeat/filebeat.yml && \
    echo 'logging.level: debug' >> /usr/share/filebeat/filebeat.yml && \
    echo 'logging.to_files: true' >> /usr/share/filebeat/filebeat.yml && \
    echo 'logging.files:' >> /usr/share/filebeat/filebeat.yml && \
    echo '  path: /var/log/filebeat' >> /usr/share/filebeat/filebeat.yml && \
    echo '  name: filebeat' >> /usr/share/filebeat/filebeat.yml && \
    echo '  keepfiles: 7' >> /usr/share/filebeat/filebeat.yml && \
    echo '  permissions: 0644' >> /usr/share/filebeat/filebeat.yml

# Loglar için dizin oluştur
RUN mkdir -p /var/log/filebeat && \
    chmod 0755 /var/log/filebeat && \
    chown root:root /usr/share/filebeat/filebeat.yml && \
    chmod go-w /usr/share/filebeat/filebeat.yml

# Root olarak çalışmaya devam et - docker-compose.yml dosyasında user: root zaten tanımlanmış durumda
CMD ["filebeat", "-e", "-strict.perms=false"] 