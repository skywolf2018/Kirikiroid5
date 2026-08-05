package org.tvp.kirikiri2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MediaStoreHack {
    private static final String ALBUM_ART_URI = "content://media/external/audio/albumart";
    private static final String[] ALBUM_PROJECTION = {"_id", "album_id", "media_type"};
    private static final byte[] temptrack_mp3 = {73, 68, 51, 4, 0, 0, 0, 0, 8, 65, 84, 80, 69, 49, 0, 0, 0, 24, 0, 0, 0, 123, 77, 101, 100, 105, 97, 87, 114, 105, 116, 101, 32, 87, 111, 114, 107, 97, 114, 111, 117, 110, 100, 125, 84, 65, 76, 66, 0, 0, 0, 24, 0, 0, 0, 123, 77, 101, 100, 105, 97, 87, 114, 105, 116, 101, 32, 87, 111, 114, 107, 97, 114, 111, 117, 110, 100, 125, 84, 73, 84, 50, 0, 0, 0, 24, 0, 0, 0, 123, 77, 101, 100, 105, 97, 87, 114, 105, 116, 101, 32, 87, 111, 114, 107, 97, 114, 111, 117, 110, 100, 125, 65, 80, 73, 67, 0, 0, 2, 33, 0, 0, 0, 105, 109, 97, 103, 101, 47, 112, 110, 103, 0, 3, 0, -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 32, 0, 0, 0, 32, 8, 2, 0, 0, 0, -4, 24, -19, -93, 0, 0, 0, 1, 115, 82, 71, 66, 0, -82, -50, 28, -23, 0, 0, 0, -50, 73, 68, 65, 84, 72, -57, -19, 85, -55, 10, -128, 64, 8, 77, -1, -1, -97, -19, 48, 36, -30, 62, 83, -76, 64, 30, 66, -59, -52, 124, 79, -35, -74, 95, -34, 38, 68, 68, 68, 82, 81, -2, -95, -69, 78, 25, 31, 102, 87, -70, 124, 70, -2, 40, 114, 8, 74, 3, 0, -22, 42, 76, 77, 0, -112, 4, -96, -78, 59, -33, -112, 1, 101, 60, 90, 0, -72, 34, -42, 71, 22, 78, 20, -107, 92, -2, -51, 45, 20, -78, 96, 42, -14, -48, 33, 74, -17, 98, 96, 69, 117, -103, -101, -58, -26, 28, -56, -106, 106, 121, 103, 75, 70, -96, 11, 84, -97, 39, 37, -86, -24, -66, -48, 39, 67, 107, -128, -97, 100, 81, -78, 121, 58, 0, -44, 44, 82, -112, 44, -20, 18, -100, 34, -122, 59, -25, 57, 12, 53, -117, -94, -103, 96, 61, 31, -123, -126, 69, 35, -53, -45, 27, 102, -115, 72, -10, -94, 37, -121, -56, 61, -126, -2, -70, 118, -69, -100, 0, -93, -100, 54, 12, -49, 92, -85, -23, -125, 51, 123, -83, 58, -21, 8, 59, -47, -110, 75, 57, -81, -66, 70, -71, 95, 46, -111, 29, -73, 67, 31, -101, 122, -93, -81, 8, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};

    public static boolean delete(Context context, File file) {
        String[] selectionArgs = {file.getAbsolutePath()};
        ContentResolver contentResolver = context.getContentResolver();
        Uri filesUri = MediaStore.Files.getContentUri("external");
        contentResolver.delete(filesUri, "_data=?", selectionArgs);
        if (file.exists()) {
            ContentValues values = new ContentValues();
            values.put("_data", file.getAbsolutePath());
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            contentResolver.delete(filesUri, "_data=?", selectionArgs);
        }
        return !file.exists();
    }

    private static File getExternalFilesDir(Context context) {
        return context.getExternalFilesDir(null);
    }

    public static InputStream getInputStream(Context context, File file, long size) {
        try {
            String[] selectionArgs = {file.getAbsolutePath()};
            ContentResolver contentResolver = context.getContentResolver();
            Uri filesUri = MediaStore.Files.getContentUri("external");
            contentResolver.delete(filesUri, "_data=?", selectionArgs);
            ContentValues values = new ContentValues();
            values.put("_data", file.getAbsolutePath());
            values.put("_size", Long.valueOf(size));
            Uri uri = contentResolver.insert(filesUri, values);
            return contentResolver.openInputStream(uri);
        } catch (Throwable th) {
            return null;
        }
    }

    public static OutputStream getOutputStream(Context context, String str) {
        Uri fileUri = getUriFromFile(str, context);
        if (fileUri == null) {
            return null;
        }
        try {
            return context.getContentResolver().openOutputStream(fileUri);
        } catch (Throwable th) {
            return null;
        }
    }

    public static Uri getUriFromFile(String path, Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor filecursor = resolver.query(
                MediaStore.Files.getContentUri("external"),
                new String[]{"_id"},
                "_data = ?",
                new String[]{path},
                "date_added desc"
        );
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
            return MediaStore.Files.getContentUri("external")
                    .buildUpon()
                    .appendPath(Integer.toString(imageId))
                    .build();
        } finally {
            filecursor.close();
        }
    }

    private static int getTemporaryAlbumId(Context context) throws Throwable {
        try {
            File temporaryTrack = installTemporaryTrack(context);
            if (temporaryTrack == null) {
                return 0;
            }
            Uri filesUri = MediaStore.Files.getContentUri("external");
            String[] selectionArgs = {temporaryTrack.getAbsolutePath()};
            ContentResolver contentResolver = context.getContentResolver();

            Cursor cursor = contentResolver.query(filesUri, ALBUM_PROJECTION, "_data=?", selectionArgs, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                ContentValues values = new ContentValues();
                values.put("_data", temporaryTrack.getAbsolutePath());
                values.put("title", "{MediaWrite Workaround}");
                values.put("_size", Long.valueOf(temporaryTrack.length()));
                values.put("mime_type", "audio/mpeg");
                values.put("is_music", true);
                contentResolver.insert(filesUri, values);
            } else {
                cursor.close();
            }

            Cursor cursor2 = contentResolver.query(filesUri, ALBUM_PROJECTION, "_data=?", selectionArgs, null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) cursor2.close();
                return 0;
            }
            int id = cursor2.getInt(0);
            int albumId = cursor2.getInt(1);
            int mediaType = cursor2.getInt(2);
            cursor2.close();

            ContentValues values2 = new ContentValues();
            boolean updateRequired = false;
            if (albumId == 0) {
                values2.put("album_id", 13371337);
                updateRequired = true;
            }
            if (mediaType != 2) {
                values2.put("media_type", 2);
                updateRequired = true;
            }
            if (updateRequired) {
                contentResolver.update(filesUri, values2, "_id=" + id, null);
            }

            Cursor cursor3 = contentResolver.query(filesUri, ALBUM_PROJECTION, "_data=?", selectionArgs, null);
            if (cursor3 == null) {
                return 0;
            }
            try {
                if (cursor3.moveToFirst()) {
                    return cursor3.getInt(1);
                }
                return 0;
            } finally {
                cursor3.close();
            }
        } catch (IOException ex) {
            Log.w("MediaFile", "Error installing temporary track.", ex);
            return 0;
        }
    }

    /**
     * 【核心修复】还原被反编译器损坏的 try-catch-finally 资源清理逻辑。
     * 原始反编译代码中 th/th2 变量未声明且自赋值，导致编译失败。
     */
    private static File installTemporaryTrack(Context context) throws Throwable {
        File externalFilesDir = getExternalFilesDir(context);
        if (externalFilesDir == null) {
            return null;
        }
        File temporaryTrack = new File(externalFilesDir, "temptrack.mp3");
        OutputStream out = null;
        try {
            out = new FileOutputStream(temporaryTrack);
            out.write(temptrack_mp3);
            out.close();
            return temporaryTrack;
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
            throw th;
        }
    }

    public static boolean mkdir(Context context, File file) throws Throwable {
        if (file.exists()) {
            return file.isDirectory();
        }
        File tmpFile = new File(file, ".MediaWriteTemp");
        int albumId = getTemporaryAlbumId(context);
        if (albumId == 0) {
            throw new IOException("Failed to create temporary album id.");
        }
        Uri albumUri = Uri.parse(String.format(Locale.US,
                "content://media/external/audio/albumart/%d", Integer.valueOf(albumId)));
        ContentValues values = new ContentValues();
        values.put("_data", tmpFile.getAbsolutePath());
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver.update(albumUri, values, null, null) == 0) {
            values.put("album_id", Integer.valueOf(albumId));
            contentResolver.insert(Uri.parse(ALBUM_ART_URI), values);
        }
        try {
            ParcelFileDescriptor fd = contentResolver.openFileDescriptor(albumUri, "r");
            if (fd != null) {
                fd.close();
            }
            return file.exists();
        } finally {
            delete(context, tmpFile);
        }
    }

    public static boolean mkfile(Context context, File file) {
        OutputStream outputStream = getOutputStream(context, file.getPath());
        if (outputStream == null) {
            return false;
        }
        try {
            outputStream.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}