package org.cocos2dx.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* JADX INFO: loaded from: classes.dex */
public final class Cocos2dxBitmap {
    private static final int HORIZONTAL_ALIGN_CENTER = 3;
    private static final int HORIZONTAL_ALIGN_LEFT = 1;
    private static final int HORIZONTAL_ALIGN_RIGHT = 2;
    private static final int VERTICAL_ALIGN_BOTTOM = 2;
    private static final int VERTICAL_ALIGN_CENTER = 3;
    private static final int VERTICAL_ALIGN_TOP = 1;
    private static Context sContext;

    private static native void nativeInitBitmapDC(int i, int i2, byte[] bArr);

    public static void setContext(Context context) {
        sContext = context;
    }

    public static boolean createTextBitmapShadowStroke(byte[] bytes, String fontName, int fontSize, int fontTintR, int fontTintG, int fontTintB, int fontTintA, int alignment, int width, int height, boolean shadow, float shadowDX, float shadowDY, float shadowBlur, float shadowOpacity, boolean stroke, int strokeR, int strokeG, int strokeB, int strokeA, float strokeSize) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        String string = new String(bytes);
        Layout.Alignment hAlignment = Layout.Alignment.ALIGN_NORMAL;
        int horizontalAlignment = alignment & 15;
        switch (horizontalAlignment) {
            case 2:
                hAlignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
            case 3:
                hAlignment = Layout.Alignment.ALIGN_CENTER;
                break;
        }
        TextPaint paint = newPaint(fontName, fontSize);
        if (stroke) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeSize);
        }
        int maxWidth = width;
        if (maxWidth <= 0) {
            maxWidth = (int) Math.ceil(StaticLayout.getDesiredWidth(string, paint));
        }
        StaticLayout staticLayout = new StaticLayout(string, paint, maxWidth, hAlignment, 1.0f, 0.0f, false);
        int layoutWidth = staticLayout.getWidth();
        int layoutHeight = staticLayout.getLineTop(staticLayout.getLineCount());
        int bitmapWidth = Math.max(layoutWidth, width);
        int bitmapHeight = layoutHeight;
        if (height > 0) {
            bitmapHeight = height;
        }
        if (bitmapWidth == 0 || bitmapHeight == 0) {
            return false;
        }
        int offsetX = 0;
        if (horizontalAlignment == 3) {
            offsetX = (bitmapWidth - layoutWidth) / 2;
        } else if (horizontalAlignment == 2) {
            offsetX = bitmapWidth - layoutWidth;
        }
        int offsetY = 0;
        int verticalAlignment = (alignment >> 4) & 15;
        switch (verticalAlignment) {
            case 2:
                offsetY = bitmapHeight - layoutHeight;
                break;
            case 3:
                offsetY = (bitmapHeight - layoutHeight) / 2;
                break;
        }
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(offsetX, offsetY);
        if (stroke) {
            paint.setARGB(strokeA, strokeR, strokeG, strokeB);
            staticLayout.draw(canvas);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB(fontTintA, fontTintR, fontTintG, fontTintB);
        staticLayout.draw(canvas);
        initNativeObject(bitmap);
        return true;
    }

    private static TextPaint newPaint(String fontName, int fontSize) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(fontSize);
        paint.setAntiAlias(true);
        if (fontName.endsWith(".ttf")) {
            try {
                Typeface typeFace = Cocos2dxTypefaces.get(sContext, fontName);
                paint.setTypeface(typeFace);
            } catch (Exception e) {
                Log.e("Cocos2dxBitmap", "error to create ttf type face: " + fontName);
                paint.setTypeface(Typeface.create(fontName, 0));
            }
        } else {
            paint.setTypeface(Typeface.create(fontName, 0));
        }
        return paint;
    }

    private static void initNativeObject(Bitmap bitmap) {
        byte[] pixels = getPixels(bitmap);
        if (pixels != null) {
            nativeInitBitmapDC(bitmap.getWidth(), bitmap.getHeight(), pixels);
        }
    }

    private static byte[] getPixels(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight() * 4];
        ByteBuffer buf = ByteBuffer.wrap(pixels);
        buf.order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(buf);
        return pixels;
    }

    public static int getFontSizeAccordingHeight(int height) {
        TextPaint paint = new TextPaint();
        Rect bounds = new Rect();
        paint.setTypeface(Typeface.DEFAULT);
        int text_size = 1;
        boolean found_desired_size = false;
        while (!found_desired_size) {
            paint.setTextSize(text_size);
            paint.getTextBounds("SghMNy", 0, "SghMNy".length(), bounds);
            text_size++;
            if (height - bounds.height() <= 2) {
                found_desired_size = true;
            }
        }
        return text_size;
    }

    private static String getStringWithEllipsis(String string, float width, float fontSize) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        TextPaint paint = new TextPaint();
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(fontSize);
        return TextUtils.ellipsize(string, paint, width, TextUtils.TruncateAt.END).toString();
    }
}
