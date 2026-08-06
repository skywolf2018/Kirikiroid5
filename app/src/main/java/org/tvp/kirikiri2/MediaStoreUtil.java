package org.tvp.kirikiri2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;

/* JADX INFO: compiled from: KR2Activity.java */
/* JADX INFO: loaded from: classes.dex */
abstract class MediaStoreUtil {
    MediaStoreUtil() {
    }

    public static Uri getUriFromFile(String path, Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"), new String[]{"_id"}, "_data = ?", new String[]{path}, "date_added desc");
        if (filecursor == null) {
            return null;
        }
        try {
            if (!filecursor.moveToFirst() || filecursor.isAfterLast()) {
                ContentValues values = new ContentValues();
                values.put("_data", path);
                return resolver.insert(MediaStore.Files.getContentUri("external"), values);
            }
            int imageId = filecursor.getInt(filecursor.getColumnIndexOrThrow("_id"));
            return MediaStore.Files.getContentUri("external").buildUpon()
                    .appendPath(Integer.toString(imageId)).build();
        } finally {
            filecursor.close();
        }
    }

    public static final void addFileToMediaStore(String path, Context context) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File file = new File(path);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
