package com.transiap.producers;

import com.transiap.config.RabbitMqConfig;

import java.util.Arrays;
import java.util.List;

public class CsvProducer extends AbstractProducer {
    private static final List<String> VEHICLES = Arrays.asList("TRUCK-100", "TRUCK-200", "TRUCK-300");

    public CsvProducer() {
        super(RabbitMqConfig.CSV_EXCHANGE, "csv");
    }

    @Override
    protected String nextMessage() {
        double lat = randomLatitude();
        double lon = randomLongitude();
        String vehicle = randomVehicle(VEHICLES);
        return vehicle + "," + lat + "," + lon;
    }

    public static void main(String[] args) throws Exception {
        new CsvProducer().run();
    }
}
