package org.cocos2dx.lib;

import android.content.Context;
import android.graphics.Typeface;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxTypefaces {
    private static final HashMap<String, Typeface> sTypefaceCache = new HashMap<>();

    public static synchronized Typeface get(Context context, String assetName) {
        Typeface typeface;
        if (!sTypefaceCache.containsKey(assetName)) {
            if (assetName.startsWith("/")) {
                typeface = Typeface.createFromFile(assetName);
            } else {
                typeface = Typeface.createFromAsset(context.getAssets(), assetName);
            }
            sTypefaceCache.put(assetName, typeface);
        }
        return sTypefaceCache.get(assetName);
    }
}
