package org.apache.http.protocol;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class SyncBasicHttpContext extends BasicHttpContext {
    public SyncBasicHttpContext(HttpContext parentContext) {
        throw new RuntimeException("Stub!");
    }

    @Override // org.apache.http.protocol.BasicHttpContext, org.apache.http.protocol.HttpContext
    public synchronized Object getAttribute(String id) {
        throw new RuntimeException("Stub!");
    }

    @Override // org.apache.http.protocol.BasicHttpContext, org.apache.http.protocol.HttpContext
    public synchronized void setAttribute(String id, Object obj) {
        throw new RuntimeException("Stub!");
    }

    @Override // org.apache.http.protocol.BasicHttpContext, org.apache.http.protocol.HttpContext
    public synchronized Object removeAttribute(String id) {
        throw new RuntimeException("Stub!");
    }
}
