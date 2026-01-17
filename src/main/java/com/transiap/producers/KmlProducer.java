package com.transiap.producers;

import com.transiap.config.RabbitMqConfig;

import java.util.Arrays;
import java.util.List;

public class KmlProducer extends AbstractProducer {
    private static final List<String> VEHICLES = Arrays.asList("TRUCK-100", "TRUCK-200", "TRUCK-300");

    public KmlProducer() {
        super(RabbitMqConfig.KML_EXCHANGE, "kml");
    }

    @Override
    protected String nextMessage() {
        double lat = randomLatitude();
        double lon = randomLongitude();
        String vehicle = randomVehicle(VEHICLES);
        return "<kml>" +
            "<Placemark>" +
            "<Point><coordinates>" + lat + ", " + lon + "</coordinates></Point>" +
            "<Vehicle id=\"" + vehicle + "\"/>" +
            "</Placemark>" +
            "</kml>";
    }

    public static void main(String[] args) throws Exception {
        new KmlProducer().run();
    }
}
