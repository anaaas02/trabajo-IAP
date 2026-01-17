package com.transiap.middleware;

import org.json.JSONObject;

public class SntnApiClient {
    private final String baseUrl;
    private final HttpClient httpClient;

    public SntnApiClient(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.httpClient = httpClient;
    }

    public String fetchAuthToken(String vehicleId) {
        String response = httpClient.get(baseUrl + "key/" + vehicleId, "application/json");
        JSONObject json = new JSONObject(response);
        return json.getString("appKey");
    }

    public String fetchTimestamp() {
        String response = httpClient.get(baseUrl + "timestamp", "application/json");
        JSONObject json = new JSONObject(response);
        return json.getString("timeStamp");
    }
}
