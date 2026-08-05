package org.apache.http.conn;

import java.io.InterruptedIOException;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class ConnectTimeoutException extends InterruptedIOException {
    public ConnectTimeoutException() {
        throw new RuntimeException("Stub!");
    }

    public ConnectTimeoutException(String message) {
        throw new RuntimeException("Stub!");
    }
}
