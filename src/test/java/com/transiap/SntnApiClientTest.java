package com.transiap;

import com.transiap.middleware.HttpClient;
import com.transiap.middleware.SntnApiClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SntnApiClientTest {
    @Test
    void parsesAuthAndTimestamp() {
        HttpClient fake = (url, mime) -> {
            if (url.endsWith("key/ABC123")) {
                return "{\"appKey\":\"TOKEN\"}";
            }
            return "{\"timeStamp\":\"TS\"}";
        };
        SntnApiClient client = new SntnApiClient("http://example.test/api/", fake);

        assertEquals("TOKEN", client.fetchAuthToken("ABC123"));
        assertEquals("TS", client.fetchTimestamp());
    }
}
