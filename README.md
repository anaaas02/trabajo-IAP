# TransIAP Container Tracking (RabbitMQ + Java)

This project implements the TransIAP real-time container GPS tracking integration using RabbitMQ messaging.
It includes three producer simulators (KML, GeoJSON, CSV), a central middleware that normalizes and enriches
messages, a database updater, and a console visualizer.

## Requirements

- Java 8+
- RabbitMQ (default: localhost:5672, guest/guest)
- MySQL with the TransIAP STC schema
- `cliente_http.jar` (SNTN REST client)
- `STC-DAO.jar` and the MySQL connector (for database updates)
- RabbitMQ Java client JAR and JSON JAR (see `lib/` instructions below)

Place the external JARs in the `lib/` folder at the project root so the provided Windows scripts can compile
and run the project.

## Local dependencies (`lib/`)

Create a `lib/` folder at the project root and drop the following JARs there:

- `amqp-client-<version>.jar` (RabbitMQ Java client)
- `json-<version>.jar` (org.json)
- `cliente_http.jar` (provided)
- `STC-DAO.jar` (provided)
- `mysql-connector-java-<version>.jar` (MySQL driver)
- `junit-platform-console-standalone-<version>.jar` (for tests)

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

### Database (MySQL)

The project expects a MySQL server with the TransIAP STC schema available. You can start MySQL in one of the
following ways:

- **Local service**: start your system MySQL service (e.g., `sudo systemctl start mysql`) and ensure the `stc`
  database/schema is loaded.
- **Docker**: run a MySQL container and load the STC schema inside it, then point the app to the container via
  the `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, and `DB_NAME` environment variables.

### Compile (Windows)

```bat
scripts\\compile-main.bat
```

### Run (Windows)

After compiling, run a component using `java -cp` and the `build\\classes` output. Example:

```bat
java -cp "build\\classes;lib\\*" com.transiap.middleware.LogisticsMiddleware
```

You can run the producers similarly:

```bat
java -cp "build\\classes;lib\\*" com.transiap.producers.KmlProducer
java -cp "build\\classes;lib\\*" com.transiap.producers.GeoJsonProducer
java -cp "build\\classes;lib\\*" com.transiap.producers.CsvProducer
```

And the consumers:

```bat
java -cp "build\\classes;lib\\*" com.transiap.db.DatabaseUpdater
java -cp "build\\classes;lib\\*" com.transiap.visualizer.LocationVisualizer
```

## Testing

```bat
scripts\\run-tests.bat
```
