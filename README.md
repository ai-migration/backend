## Before Running Services
### Make sure there is a Kafka server running
```
cd infra
docker-compose up
```
- Check the Kafka messages:
```
docker-compose exec -it kafka /bin/bash
cd /bin
./kafka-console-consumer --bootstrap-server localhost:9092 --topic [topic이름]
```

## Run the backend micro-services
See the README.md files inside the each microservices directory:

- user
- noticeboard
- agent
- admin
- platform


## Run API Gateway (Spring Gateway)
```
cd gateway
mvn spring-boot:run
```

## Test by API
- agent
```
Post http://localhost:8088/agents/translate
```
  <p align="center">
    <img src="https://github.com/user-attachments/assets/83475cf4-320b-4b12-9af5-9af34a885b85" alt="agent api 실행" width="70%">
  </p>

## Build
```
sh build-backend.sh [micro-services 이름] [tag]
ex) sh build-backend.sh admin 250826
```