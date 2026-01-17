package com.transiap.config;

import com.rabbitmq.client.ConnectionFactory;

public final class RabbitMqConfig {
    public static final String KML_EXCHANGE = "location.kml";
    public static final String GEOJSON_EXCHANGE = "location.geojson";
    public static final String CSV_EXCHANGE = "location.csv";
    public static final String OUTPUT_EXCHANGE = "traslados.localizaciones";

    public static final String KML_QUEUE = "location.kml.queue";
    public static final String GEOJSON_QUEUE = "location.geojson.queue";
    public static final String CSV_QUEUE = "location.csv.queue";

    public static final String DB_QUEUE = "stc.db.update.queue";
    public static final String VISUALIZER_QUEUE = "stc.visualizer.queue";

    private RabbitMqConfig() {
    }

    public static ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(env("RABBITMQ_HOST", "localhost"));
        factory.setPort(Integer.parseInt(env("RABBITMQ_PORT", "5672")));
        factory.setUsername(env("RABBITMQ_USER", "guest"));
        factory.setPassword(env("RABBITMQ_PASS", "guest"));
        return factory;
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
