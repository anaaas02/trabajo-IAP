# TransIAP Container Tracking (RabbitMQ + Java)

This project implements the TransIAP real-time container GPS tracking integration using RabbitMQ messaging.
It includes three producer simulators (KML, GeoJSON, CSV), a central middleware that normalizes and enriches
messages, a database updater, and a console visualizer.

## Requirements

- Java 8+
- Maven
- RabbitMQ (default: localhost:5672, guest/guest)
- MySQL with the TransIAP STC schema
- `cliente_http.jar` (SNTN REST client)
- `STC-DAO.jar` and the MySQL connector (for database updates)

Place the external JARs on the runtime classpath when launching the middleware or DB updater.

## Build

```bash
mvn clean package
```

## Configuration

Environment variables (optional):

- `RABBITMQ_HOST` (default `localhost`)
- `RABBITMQ_PORT` (default `5672`)
- `RABBITMQ_USER` (default `guest`)
- `RABBITMQ_PASS` (default `guest`)
- `SNTN_BASE_URL` (default `https://pedvalar.webs.upv.es/iap/rest/sntn/`)
- `DB_HOST` (default `localhost`)
- `DB_PORT` (default `3306`)
- `DB_USER` (default `root`)
- `DB_PASSWORD` (default empty)
- `DB_NAME` (default `stc`)

Producer controls:

- `MESSAGE_COUNT` (default `0` = infinite)
- `MESSAGE_DELAY_MS` (default `1000`)

DAO class overrides (only needed if your STC-DAO classes use packages):

- `DAOFACTORY_CLASS` (default `DAOFactory`)
- `LOCALIZACION_CLASS` (default `LocalizacionGPS`)

## Run

Start RabbitMQ and MySQL first. Then run the components (order is flexible, but middleware should be running
before producers send data).

### Producers

```bash
mvn -q -Dexec.mainClass=com.transiap.producers.KmlProducer exec:java
mvn -q -Dexec.mainClass=com.transiap.producers.GeoJsonProducer exec:java
mvn -q -Dexec.mainClass=com.transiap.producers.CsvProducer exec:java
```

### Middleware

```bash
mvn -q -Dexec.mainClass=com.transiap.middleware.LogisticsMiddleware exec:java
```

Ensure `cliente_http.jar` is on the runtime classpath when launching the middleware.

### Database updater

```bash
mvn -q -Dexec.mainClass=com.transiap.db.DatabaseUpdater exec:java
```

Ensure `STC-DAO.jar` and the MySQL connector are on the runtime classpath.

### Visualizer

```bash
mvn -q -Dexec.mainClass=com.transiap.visualizer.LocationVisualizer exec:java
```

## Testing

```bash
mvn test
```
