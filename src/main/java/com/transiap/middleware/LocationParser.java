package com.transiap.middleware;

import com.transiap.model.LocationUpdate;
import org.json.JSONArray;
import org.json.JSONObject;

public final class LocationParser {
    private LocationParser() {
    }

    public static LocationUpdate parseKml(String payload) {
        String coordinatesTag = extractTagValue(payload, "coordinates");
        String[] coordinates = coordinatesTag.split(",");
        if (coordinates.length < 2) {
            throw new IllegalArgumentException("Invalid KML coordinates: " + coordinatesTag);
        }
        double latitude = Double.parseDouble(coordinates[0].trim());
        double longitude = Double.parseDouble(coordinates[1].trim());
        String vehicleId = extractAttribute(payload, "Vehicle", "id");
        return new LocationUpdate(latitude, longitude, vehicleId);
    }

    public static LocationUpdate parseGeoJson(String payload) {
        JSONObject json = new JSONObject(payload);
        JSONArray coords = json.getJSONObject("geometry").getJSONArray("coordinates");
        double latitude = coords.getDouble(0);
        double longitude = coords.getDouble(1);
        String vehicleId = json.getJSONObject("properties").getString("vehicle");
        return new LocationUpdate(latitude, longitude, vehicleId);
    }

    public static LocationUpdate parseCsv(String payload) {
        String[] tokens = payload.split(",");
        if (tokens.length < 3) {
            throw new IllegalArgumentException("Invalid CSV payload: " + payload);
        }
        String vehicleId = tokens[0].trim();
        double latitude = Double.parseDouble(tokens[1].trim());
        double longitude = Double.parseDouble(tokens[2].trim());
        return new LocationUpdate(latitude, longitude, vehicleId);
    }

    private static String extractTagValue(String payload, String tagName) {
        String openTag = "<" + tagName + ">";
        String closeTag = "</" + tagName + ">";
        int start = payload.indexOf(openTag);
        int end = payload.indexOf(closeTag);
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException("Missing tag " + tagName + " in payload");
        }
        return payload.substring(start + openTag.length(), end).trim();
    }

    private static String extractAttribute(String payload, String tagName, String attributeName) {
        String search = "<" + tagName;
        int tagStart = payload.indexOf(search);
        if (tagStart < 0) {
            throw new IllegalArgumentException("Missing tag " + tagName + " in payload");
        }
        int tagEnd = payload.indexOf(">", tagStart);
        if (tagEnd < 0) {
            throw new IllegalArgumentException("Malformed tag " + tagName + " in payload");
        }
        String tagContent = payload.substring(tagStart, tagEnd);
        String attrToken = attributeName + "=\"";
        int attrStart = tagContent.indexOf(attrToken);
        if (attrStart < 0) {
            throw new IllegalArgumentException("Missing attribute " + attributeName + " in " + tagName);
        }
        int valueStart = attrStart + attrToken.length();
        int valueEnd = tagContent.indexOf("\"", valueStart);
        if (valueEnd < 0) {
            throw new IllegalArgumentException("Malformed attribute " + attributeName + " in " + tagName);
        }
        return tagContent.substring(valueStart, valueEnd);
    }
}
