package com.transiap.middleware;

import java.lang.reflect.Method;

public class ReflectionHttpClient implements HttpClient {
    private final Method getMethod;

    public ReflectionHttpClient() {
        this.getMethod = resolveGetMethod();
    }

    @Override
    public String get(String url, String mimeType) {
        try {
            return (String) getMethod.invoke(null, url, mimeType);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke HTTPClient.get", e);
        }
    }

    private Method resolveGetMethod() {
        try {
            Class<?> clientClass = Class.forName("HTTPClient");
            return clientClass.getMethod("get", String.class, String.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("HTTPClient class not found. Ensure cliente_http.jar is on the classpath.", e);
        }
    }
}
