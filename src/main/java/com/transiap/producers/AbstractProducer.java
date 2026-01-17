package com.transiap.producers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.transiap.config.RabbitMqConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

public abstract class AbstractProducer {
    private final String exchange;
    private final String routingKey;

    protected AbstractProducer(String exchange, String routingKey) {
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void run() throws IOException, TimeoutException, InterruptedException {
        try (Connection connection = RabbitMqConfig.connectionFactory().newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(exchange, "direct", true);

            int count = Integer.parseInt(env("MESSAGE_COUNT", "0"));
            long delayMs = Long.parseLong(env("MESSAGE_DELAY_MS", "1000"));
            int sent = 0;

            while (count == 0 || sent < count) {
                String message = nextMessage();
                channel.basicPublish(exchange, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println("[Producer] Sent: " + message);
                sent++;
                Thread.sleep(delayMs);
            }
        }
    }

    protected double randomLatitude() {
        return ThreadLocalRandom.current().nextDouble(39.0, 40.0);
    }

    protected double randomLongitude() {
        return ThreadLocalRandom.current().nextDouble(-0.6, 0.1);
    }

    protected String randomVehicle(List<String> vehicles) {
        return vehicles.get(ThreadLocalRandom.current().nextInt(vehicles.size()));
    }

    protected abstract String nextMessage();

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
