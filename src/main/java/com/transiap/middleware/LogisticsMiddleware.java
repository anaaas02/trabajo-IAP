package com.transiap.middleware;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.transiap.config.RabbitMqConfig;
import com.transiap.model.LocationUpdate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogisticsMiddleware {
    private static final Logger LOGGER = Logger.getLogger(LogisticsMiddleware.class.getName());
    private static final String BASE_URL = env("SNTN_BASE_URL", "https://pedvalar.webs.upv.es/iap/rest/sntn/");

    private final SntnApiClient apiClient;
    private final Connection connection;
    private final Channel channel;

    public LogisticsMiddleware() throws IOException, TimeoutException {
        this.apiClient = new SntnApiClient(BASE_URL, new ReflectionHttpClient());
        this.connection = RabbitMqConfig.connectionFactory().newConnection();
        this.channel = connection.createChannel();
        setupTopology();
    }

    public void start() throws IOException {
        DeliverCallback kmlCallback = (consumerTag, delivery) -> handleMessage(
            new String(delivery.getBody(), StandardCharsets.UTF_8), "kml");
        DeliverCallback geoCallback = (consumerTag, delivery) -> handleMessage(
            new String(delivery.getBody(), StandardCharsets.UTF_8), "geojson");
        DeliverCallback csvCallback = (consumerTag, delivery) -> handleMessage(
            new String(delivery.getBody(), StandardCharsets.UTF_8), "csv");

        channel.basicConsume(RabbitMqConfig.KML_QUEUE, true, kmlCallback, consumerTag -> { });
        channel.basicConsume(RabbitMqConfig.GEOJSON_QUEUE, true, geoCallback, consumerTag -> { });
        channel.basicConsume(RabbitMqConfig.CSV_QUEUE, true, csvCallback, consumerTag -> { });

        LOGGER.info("Logistics middleware running. Waiting for messages...");
    }

    public void stop() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.log(Level.WARNING, "Error closing RabbitMQ resources", e);
        }
    }

    private void setupTopology() throws IOException {
        channel.exchangeDeclare(RabbitMqConfig.KML_EXCHANGE, "direct", true);
        channel.exchangeDeclare(RabbitMqConfig.GEOJSON_EXCHANGE, "direct", true);
        channel.exchangeDeclare(RabbitMqConfig.CSV_EXCHANGE, "direct", true);
        channel.exchangeDeclare(RabbitMqConfig.OUTPUT_EXCHANGE, "fanout", true);

        channel.queueDeclare(RabbitMqConfig.KML_QUEUE, true, false, false, null);
        channel.queueDeclare(RabbitMqConfig.GEOJSON_QUEUE, true, false, false, null);
        channel.queueDeclare(RabbitMqConfig.CSV_QUEUE, true, false, false, null);

        channel.queueBind(RabbitMqConfig.KML_QUEUE, RabbitMqConfig.KML_EXCHANGE, "kml");
        channel.queueBind(RabbitMqConfig.GEOJSON_QUEUE, RabbitMqConfig.GEOJSON_EXCHANGE, "geojson");
        channel.queueBind(RabbitMqConfig.CSV_QUEUE, RabbitMqConfig.CSV_EXCHANGE, "csv");
    }

    private void handleMessage(String payload, String format) {
        try {
            LocationUpdate update = parseLocation(payload, format);
            String auth = apiClient.fetchAuthToken(update.getVehicleId());
            String timestamp = apiClient.fetchTimestamp();
            String message = TransiapMessageBuilder.build(update, auth, timestamp);
            channel.basicPublish(RabbitMqConfig.OUTPUT_EXCHANGE, "", null, message.getBytes(StandardCharsets.UTF_8));
            LOGGER.info(() -> "Published unified message for vehicle " + update.getVehicleId());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to process message: " + payload, e);
        }
    }

    private LocationUpdate parseLocation(String payload, String format) {
        switch (format) {
            case "kml":
                return LocationParser.parseKml(payload);
            case "geojson":
                return LocationParser.parseGeoJson(payload);
            case "csv":
                return LocationParser.parseCsv(payload);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        LogisticsMiddleware middleware = new LogisticsMiddleware();
        Runtime.getRuntime().addShutdownHook(new Thread(middleware::stop));
        middleware.start();
    }
}
