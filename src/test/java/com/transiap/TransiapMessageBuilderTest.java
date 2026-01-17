package com.transiap;

import com.transiap.middleware.TransiapMessageBuilder;
import com.transiap.middleware.TransiapMessageParser;
import com.transiap.model.LocationUpdate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransiapMessageBuilderTest {
    @Test
    void buildsUnifiedJson() {
        LocationUpdate update = new LocationUpdate(10.0, 20.0, "XYZ123");
        String message = TransiapMessageBuilder.build(update, "TOKEN123", "TS123");
        LocationUpdate parsed = TransiapMessageParser.parse(message);

        assertEquals(10.0, parsed.getLatitude(), 0.0001);
        assertEquals(20.0, parsed.getLongitude(), 0.0001);
        assertEquals("XYZ123", parsed.getVehicleId());
    }
}
