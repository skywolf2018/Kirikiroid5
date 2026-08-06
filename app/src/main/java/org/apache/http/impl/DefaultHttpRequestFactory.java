package org.apache.http.impl;

import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class DefaultHttpRequestFactory implements HttpRequestFactory {
    public DefaultHttpRequestFactory() {
        throw new RuntimeException("Stub!");
    }

    @Override // org.apache.http.HttpRequestFactory
    public HttpRequest newHttpRequest(RequestLine requestline) throws MethodNotSupportedException {
        throw new RuntimeException("Stub!");
    }

    @Override // org.apache.http.HttpRequestFactory
    public HttpRequest newHttpRequest(String method, String uri) throws MethodNotSupportedException {
        throw new RuntimeException("Stub!");
    }
}
