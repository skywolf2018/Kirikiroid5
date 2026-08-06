package org.apache.http.impl.client;

import org.apache.http.conn.routing.HttpRoute;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class RoutedRequest {
    protected final RequestWrapper request;
    protected final HttpRoute route;

    public RoutedRequest(RequestWrapper req, HttpRoute route) {
        throw new RuntimeException("Stub!");
    }

    public final RequestWrapper getRequest() {
        throw new RuntimeException("Stub!");
    }

    public final HttpRoute getRoute() {
        throw new RuntimeException("Stub!");
    }
}
