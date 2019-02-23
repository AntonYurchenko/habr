# Scala http endpoint for kafka topic on base vert.x

## Description of environment
### General
* `KAFKA_BROKERS` - in format host:port split comma
* `KAFKA_TOPIC` - name of target kafka topic
### Option
* HTTP_HOST - host of the REST api into docker container, by default '0.0.0.0'
* HTTP_PORT - port of the REST api into docker container, by default 8080
* KAFKA_INSTANCES - count of instances of KafkaSender, by default 1
* KAFKA_POOL_SIZE - size of threads pool for all instances of KafkaSender, by default 1
Default values we can change into file `src/main/resources/application.conf`
### Logger
* APP_LOG_LEVEL - logger level for logic of the application, by default 'INFO'
* ROOT_LOG_LEVEL - logger level for all classes (include all libraries), by default 'INFO'
Default values we can change into file `src/main/resources/logback.xml`

## Building of docker image
```bash
git clone git@github.com:AntonYurchenko/habr.git habr
docker build -f Docerfile -t http-kafka-endpoint:0.1 ./habr/http-kafka-endpoint/
```

## Running of docker container as service
```bash
docker run --restart always \
    -p 8080:8080 \
    -e APP_LOG_LEVEL=DEBUG \
    -e KAFKA_TOPIC=topic-name \
    -e KAFKA_BROKERS=kafka-host-name:9092 \
    --name http-kafka-endpoint \
    http-kafka-endpoint:0.1 
```

## Metrics example
```bash
curl -X GET 0.0.0.0:8080/metrics
# HELP kafka_message_counter Counter of sent messages to kafka
# TYPE kafka_message_counter counter
kafka_message_counter{status="success",} 1.0
# HELP http_request_counter Counter of http requests to REST
# TYPE http_request_counter counter
http_request_counter{status="200",} 1.0
```

## Testing
### Sending of valid JSON
```bash
curl -X POST \
    -H 'Content-Type: application/json' \
    -d '{"user_id":"test_user", "event_name":"click_to_banner", "timestamp":1549583640}' \
    0.0.0.0:8080/events
```
Response
```json
{
  "status":"SUCCESS",
  "partition":0
}
```
### Sending of empty JSON
```bash
curl -X POST \
    -H 'Content-Type: application/json' \
    -d '' \
    0.0.0.0:8080/events
```
Response
```json
{
  "error":"Empty request"
}
```
### Sending of JSON without required field
```bash
curl -X POST \
    -H 'Content-Type: application/json' \
    -d '{"event_name":"click_to_banner", "timestamp":1549583640}' \
    0.0.0.0:8080/events
```
Response
```json
{
  "error":"Request should contain field: 'user_id'"
}
```