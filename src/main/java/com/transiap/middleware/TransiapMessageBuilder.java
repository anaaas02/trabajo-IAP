package com.transiap.middleware;

import com.transiap.model.LocationUpdate;
import org.json.JSONObject;

public final class TransiapMessageBuilder {
    private TransiapMessageBuilder() {
    }

    public static String build(LocationUpdate update, String auth, String timestamp) {
        JSONObject coordinates = new JSONObject();
        coordinates.put("latitud", update.getLatitude());
        coordinates.put("longitud", update.getLongitude());

        JSONObject message = new JSONObject();
        message.put("coordenadas", coordinates);
        message.put("vehiculo", update.getVehicleId());
        message.put("auth", auth);
        message.put("timestamp", timestamp);
        return message.toString();
    }
}
