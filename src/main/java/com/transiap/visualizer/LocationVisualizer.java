package com.transiap.visualizer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.transiap.config.RabbitMqConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationVisualizer {
    private static final Logger LOGGER = Logger.getLogger(LocationVisualizer.class.getName());

    private final Connection connection;
    private final Channel channel;

    public LocationVisualizer() throws IOException, TimeoutException {
        this.connection = RabbitMqConfig.connectionFactory().newConnection();
        this.channel = connection.createChannel();
        setupTopology();
    }

    public void start() throws IOException {
        DeliverCallback callback = (consumerTag, delivery) -> {
            String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[Visualizer] Received location: " + payload);
        };
        channel.basicConsume(RabbitMqConfig.VISUALIZER_QUEUE, true, callback, consumerTag -> { });
        LOGGER.info("Visualizer running. Waiting for messages...");
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
        channel.queueDeclare(RabbitMqConfig.VISUALIZER_QUEUE, true, false, false, null);
        channel.queueBind(RabbitMqConfig.VISUALIZER_QUEUE, RabbitMqConfig.OUTPUT_EXCHANGE, "");
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        LocationVisualizer visualizer = new LocationVisualizer();
        Runtime.getRuntime().addShutdownHook(new Thread(visualizer::stop));
        visualizer.start();
    }
}
