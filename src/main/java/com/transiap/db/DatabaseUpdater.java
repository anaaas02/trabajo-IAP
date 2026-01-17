package com.transiap.db;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.transiap.config.RabbitMqConfig;
import com.transiap.middleware.TransiapMessageParser;
import com.transiap.model.LocationUpdate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUpdater {
    private static final Logger LOGGER = Logger.getLogger(DatabaseUpdater.class.getName());

    private final DaoAdapter daoAdapter;
    private final Connection connection;
    private final Channel channel;

    public DatabaseUpdater(DaoAdapter daoAdapter) throws IOException, TimeoutException {
        this.daoAdapter = daoAdapter;
        this.connection = RabbitMqConfig.connectionFactory().newConnection();
        this.channel = connection.createChannel();
        setupTopology();
    }

    public void start() throws IOException {
        DeliverCallback callback = (consumerTag, delivery) -> handleMessage(
            new String(delivery.getBody(), StandardCharsets.UTF_8));
        channel.basicConsume(RabbitMqConfig.DB_QUEUE, true, callback, consumerTag -> { });
        LOGGER.info("Database updater running. Waiting for messages...");
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
        channel.exchangeDeclare(RabbitMqConfig.OUTPUT_EXCHANGE, "fanout", true);
        channel.queueDeclare(RabbitMqConfig.DB_QUEUE, true, false, false, null);
        channel.queueBind(RabbitMqConfig.DB_QUEUE, RabbitMqConfig.OUTPUT_EXCHANGE, "");
    }

    private void handleMessage(String payload) {
        try {
            LocationUpdate update = TransiapMessageParser.parse(payload);
            Object traslado = daoAdapter.findActiveTraslado(update.getVehicleId());
            if (traslado == null) {
                LOGGER.warning("No active traslado found for vehicle " + update.getVehicleId());
                return;
            }
            Object loc = daoAdapter.createLocalizacion(update.getLatitude(), update.getLongitude(), traslado);
            daoAdapter.saveLocalizacion(loc);
            daoAdapter.updateUltimaLocalizacion(traslado, loc);
            LOGGER.info(() -> "Stored location for vehicle " + update.getVehicleId());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to update DB for payload: " + payload, e);
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        DaoAdapter adapter = new ReflectionDaoAdapter();
        adapter.connect(
            env("DB_HOST", "localhost"),
            env("DB_PORT", "3306"),
            env("DB_USER", "root"),
            env("DB_PASSWORD", ""),
            env("DB_NAME", "stc")
        );
        DatabaseUpdater updater = new DatabaseUpdater(adapter);
        Runtime.getRuntime().addShutdownHook(new Thread(updater::stop));
        updater.start();
    }
}
