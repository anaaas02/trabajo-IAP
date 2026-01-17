package com.transiap;

import com.transiap.middleware.LocationParser;
import com.transiap.model.LocationUpdate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocationParserTest {
    @Test
    void parsesKml() {
        String payload = "<kml><Placemark><Point><coordinates>39.5, -0.3</coordinates></Point><Vehicle id=\"ABC123\"/></Placemark></kml>";
        LocationUpdate update = LocationParser.parseKml(payload);
        assertEquals(39.5, update.getLatitude(), 0.0001);
        assertEquals(-0.3, update.getLongitude(), 0.0001);
        assertEquals("ABC123", update.getVehicleId());
    }

    @Test
    void parsesGeoJson() {
        String payload = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[10.0,20.0]},\"properties\":{\"vehicle\":\"XYZ999\"}}";
        LocationUpdate update = LocationParser.parseGeoJson(payload);
        assertEquals(10.0, update.getLatitude(), 0.0001);
        assertEquals(20.0, update.getLongitude(), 0.0001);
        assertEquals("XYZ999", update.getVehicleId());
    }

    @Test
    void parsesCsv() {
        String payload = "TRUCK-1,40.1,-0.25";
        LocationUpdate update = LocationParser.parseCsv(payload);
        assertEquals(40.1, update.getLatitude(), 0.0001);
        assertEquals(-0.25, update.getLongitude(), 0.0001);
        assertEquals("TRUCK-1", update.getVehicleId());
    }

    @Test
    void rejectsBadCsv() {
        assertThrows(IllegalArgumentException.class, () -> LocationParser.parseCsv("bad"));
    }
}
