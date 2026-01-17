package com.transiap.db;

import java.lang.reflect.Method;

public class ReflectionDaoAdapter implements DaoAdapter {
    private final Object daoFactory;
    private final Method connectMethod;
    private final Method getTrasladoDaoMethod;
    private final Method getLocalizacionDaoMethod;
    private final Method getTrasladoActivoMethod;
    private final Method saveLocalizacionMethod;
    private final Method updateUltimaLocalizacionMethod;
    private final Class<?> localizacionClass;

    public ReflectionDaoAdapter() {
        try {
            Class<?> daoFactoryClass = Class.forName(env("DAOFACTORY_CLASS", "DAOFactory"));
            Method getCurrentInstance = daoFactoryClass.getMethod("getCurrentInstance");
            this.daoFactory = getCurrentInstance.invoke(null);
            this.connectMethod = daoFactoryClass.getMethod("connect", String.class, String.class, String.class, String.class, String.class);

            this.getTrasladoDaoMethod = daoFactoryClass.getMethod("getTrasladoDAO");
            this.getLocalizacionDaoMethod = daoFactoryClass.getMethod("getLocalizacionGPSDAO");

            Class<?> trasladoDaoClass = getTrasladoDaoMethod.getReturnType();
            Class<?> locDaoClass = getLocalizacionDaoMethod.getReturnType();

            this.getTrasladoActivoMethod = trasladoDaoClass.getMethod("getTrasladoActivoPorVehiculo", String.class);
            this.updateUltimaLocalizacionMethod = trasladoDaoClass.getMethod("updateUltimaLocalizacionTraslado", Object.class, Object.class);
            this.saveLocalizacionMethod = locDaoClass.getMethod("saveLocalizacionGPS", Object.class);

            this.localizacionClass = Class.forName(env("LOCALIZACION_CLASS", "LocalizacionGPS"));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("STC-DAO classes not found. Ensure STC-DAO.jar is on the classpath.", e);
        }
    }

    @Override
    public void connect(String host, String port, String user, String password, String database) {
        try {
            connectMethod.invoke(daoFactory, host, port, user, password, database);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to connect using DAOFactory", e);
        }
    }

    @Override
    public Object findActiveTraslado(String vehicleId) {
        try {
            Object trasladoDao = getTrasladoDaoMethod.invoke(daoFactory);
            return getTrasladoActivoMethod.invoke(trasladoDao, vehicleId);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to query active traslado", e);
        }
    }

    @Override
    public Object createLocalizacion(double latitude, double longitude, Object traslado) {
        try {
            Object loc = localizacionClass.getDeclaredConstructor().newInstance();
            Method setLatitud = localizacionClass.getMethod("setLatitud", double.class);
            Method setLongitud = localizacionClass.getMethod("setLongitud", double.class);
            Method setTraslado = localizacionClass.getMethod("setTraslado", Object.class);
            setLatitud.invoke(loc, latitude);
            setLongitud.invoke(loc, longitude);
            setTraslado.invoke(loc, traslado);
            return loc;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create LocalizacionGPS", e);
        }
    }

    @Override
    public void saveLocalizacion(Object localizacion) {
        try {
            Object locDao = getLocalizacionDaoMethod.invoke(daoFactory);
            saveLocalizacionMethod.invoke(locDao, localizacion);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to save LocalizacionGPS", e);
        }
    }

    @Override
    public void updateUltimaLocalizacion(Object traslado, Object localizacion) {
        try {
            Object trasladoDao = getTrasladoDaoMethod.invoke(daoFactory);
            updateUltimaLocalizacionMethod.invoke(trasladoDao, traslado, localizacion);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to update ultima localizacion", e);
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
