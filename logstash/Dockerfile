# Use the official Logstash image
FROM docker.elastic.co/logstash/logstash:8.9.1

# Copy the PostgreSQL JDBC driver to the external library folder of Logstash
COPY ./bin/ /usr/share/logstash/logstash-core/lib/jars/

COPY config/ /usr/share/logstash/config/

# Start Logstash with your configuration file
#CMD ["logstash", "-f", "/usr/share/logstash/config/logstash.conf"]
CMD ["logstash", "-f", "/usr/share/logstash/config/logstash.conf"]
