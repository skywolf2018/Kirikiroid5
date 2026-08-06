package org.apache.http.impl;

import java.util.Locale;
import org.apache.http.ReasonPhraseCatalog;

/* JADX INFO: loaded from: classes.dex */
@Deprecated
public class EnglishReasonPhraseCatalog implements ReasonPhraseCatalog {
    public static final EnglishReasonPhraseCatalog INSTANCE = null;

    protected EnglishReasonPhraseCatalog() {
        throw new RuntimeException("Stub!");
    }

    @Override // org.apache.http.ReasonPhraseCatalog
    public String getReason(int status, Locale loc) {
        throw new RuntimeException("Stub!");
    }
}
