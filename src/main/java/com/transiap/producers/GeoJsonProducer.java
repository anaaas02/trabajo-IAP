package com.transiap.producers;

import com.transiap.config.RabbitMqConfig;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class GeoJsonProducer extends AbstractProducer {
    private static final List<String> VEHICLES = Arrays.asList("TRUCK-100", "TRUCK-200", "TRUCK-300");

    public GeoJsonProducer() {
        super(RabbitMqConfig.GEOJSON_EXCHANGE, "geojson");
    }

    @Override
    protected String nextMessage() {
        double lat = randomLatitude();
        double lon = randomLongitude();
        String vehicle = randomVehicle(VEHICLES);

        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        geometry.put("coordinates", new double[]{lat, lon});

        JSONObject properties = new JSONObject();
        properties.put("vehicle", vehicle);

        JSONObject payload = new JSONObject();
        payload.put("type", "Feature");
        payload.put("geometry", geometry);
        payload.put("properties", properties);
        return payload.toString();
    }

    public static void main(String[] args) throws Exception {
        new GeoJsonProducer().run();
    }
}
