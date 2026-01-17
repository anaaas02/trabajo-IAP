package com.transiap.db;

public interface DaoAdapter {
    void connect(String host, String port, String user, String password, String database);

    Object findActiveTraslado(String vehicleId);

    Object createLocalizacion(double latitude, double longitude, Object traslado);

    void saveLocalizacion(Object localizacion);

    void updateUltimaLocalizacion(Object traslado, Object localizacion);
}
