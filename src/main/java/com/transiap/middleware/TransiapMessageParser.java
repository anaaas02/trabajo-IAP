package com.transiap.middleware;

import com.transiap.model.LocationUpdate;
import org.json.JSONObject;

public final class TransiapMessageParser {
    private TransiapMessageParser() {
    }

    public static LocationUpdate parse(String payload) {
        JSONObject json = new JSONObject(payload);
        JSONObject coords = json.getJSONObject("coordenadas");
        double latitude = coords.getDouble("latitud");
        double longitude = coords.getDouble("longitud");
        String vehicle = json.getString("vehiculo");
        return new LocationUpdate(latitude, longitude, vehicle);
    }
}
