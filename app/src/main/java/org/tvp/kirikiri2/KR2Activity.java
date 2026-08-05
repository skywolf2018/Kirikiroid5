package org.tvp.kirikiri2;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.MotionEventCompat;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;

/* JADX INFO: loaded from: classes.dex */
public class KR2Activity extends Cocos2dxActivity {
    static final int ORIENT_HORIZONTAL = 2;
    static final int ORIENT_VERTICAL = 1;
    static String[] _extSdPaths;
    static DialogMessage mDialogMessage;
    protected static View mTextEdit;
    static Handler msgHandler;
    public static KR2Activity sInstance;
    SharedPreferences Sp;
    static ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    static ActivityManager mAcitivityManager = null;
    static Debug.MemoryInfo mDbgMemoryInfo = new Debug.MemoryInfo();
    StorageManager mStorageManager = null;
    Method mMethodGetPaths = null;
    Method mGetVolumeState = null;

    private static native void initDump(String str);

    public static native void nativeCharInput(int i);

    public static native void nativeCommitText(String str, int i);

    public static native void nativeDeleteBackward();

    private static native String nativeGetContentText();

    private static native boolean nativeGetHideSystemButton();

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeHoverMoved(float f, float f2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeInsertText(String str);

    public static native boolean nativeKeyAction(int i, boolean z);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeMouseScrolled(float f);

    private static native void nativeOnLowMemory();

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeTouchesBegin(int i, float f, float f2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeTouchesCancel(int[] iArr, float[] fArr, float[] fArr2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeTouchesEnd(int i, float f, float f2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeTouchesMove(int[] iArr, float[] fArr, float[] fArr2);

    public static native void onBannerSizeChanged(int i, int i2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void onMessageBoxOK(int i);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void onMessageBoxText(String str);

    private static native void onNativeExit();

    public static native void onNativeInit();

    static {
        System.loadLibrary("SDL2");
        mDialogMessage = new DialogMessage();
        mTextEdit = null;
        msgHandler = new Handler() { // from class: org.tvp.kirikiri2.KR2Activity.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                KR2Activity.sInstance.handleMessage(msg);
            }
        };
    }

    public static void updateMemoryInfo() {
        if (mAcitivityManager == null) {
            mAcitivityManager = (ActivityManager) sInstance.getSystemService("activity");
        }
        mAcitivityManager.getMemoryInfo(memoryInfo);
        Debug.getMemoryInfo(mDbgMemoryInfo);
    }

    public static long getAvailMemory() {
        return memoryInfo.availMem;
    }

    public static long getUsedMemory() {
        return mDbgMemoryInfo.getTotalPss();
    }

    public static String getKR2DeviceId() {

        TelephonyManager mgr =
                (TelephonyManager) GetInstance()
                        .getSystemService(Context.TELEPHONY_SERVICE);

        if (mgr != null) {
            try {
                String deviceId = mgr.getDeviceId();

                if (deviceId != null && deviceId.length() > 0) {
                    return "DevID:" + deviceId;
                }

            } catch (SecurityException e) {
                Log.w("KR2Activity", "READ_PHONE_STATE permission denied");
            }
        }


        String androidId = Settings.Secure.getString(
                GetInstance().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );


        if (androidId != null
                && androidId.length() > 8
                && !"9774d56d682e549c".equals(androidId)) {

            return "AndroidID:" + androidId;
        }


        if (Build.SERIAL != null
                && Build.SERIAL.length() > 3) {

            return "AndroidID:" + Build.SERIAL;
        }


        return "";
    }

    
private static boolean ffmpegLoaded = false;

public static synchronized void loadFFmpeg() {
    if (ffmpegLoaded) {
        return;
    }
    System.loadLibrary("ffmpeg");
    ffmpegLoaded = true;
}

public static KR2Activity GetInstance() {
        return sInstance;
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        sInstance = this;
        this.Sp = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        initDump(String.valueOf(getFilesDir().getAbsolutePath()) + "/dump");
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onLowMemory() {
        nativeOnLowMemory();
    }

    static class DialogMessage {
        public String[] Buttons;
        public String Text;
        public EditText TextEditor = null;
        public String Title;

        public void Init(String title, String text, String[] buttons) {
            this.Title = title;
            this.Text = text;
            this.Buttons = buttons;
        }

        void onButtonClick(int n) {
            if (this.TextEditor != null) {
                KR2Activity.onMessageBoxText(this.TextEditor.getText().toString());
            }
            KR2Activity.onMessageBoxOK(n);
        }

        public AlertDialog.Builder CreateBuilder() {
            AlertDialog.Builder builder = new AlertDialog.Builder(KR2Activity.sInstance).setTitle(this.Title).setMessage(this.Text).setCancelable(false);
            if (this.Buttons.length >= 1) {
                builder = builder.setPositiveButton(this.Buttons[0], new DialogInterface.OnClickListener() { // from class: org.tvp.kirikiri2.KR2Activity.DialogMessage.1
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        DialogMessage.this.onButtonClick(0);
                    }
                });
            }
            if (this.Buttons.length >= 2) {
                builder = builder.setNeutralButton(this.Buttons[1], new DialogInterface.OnClickListener() { // from class: org.tvp.kirikiri2.KR2Activity.DialogMessage.2
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        DialogMessage.this.onButtonClick(1);
                    }
                });
            }
            if (this.Buttons.length >= 3) {
                builder = builder.setNegativeButton(this.Buttons[2], new DialogInterface.OnClickListener() { // from class: org.tvp.kirikiri2.KR2Activity.DialogMessage.3
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialog, int which) {
                        DialogMessage.this.onButtonClick(2);
                    }
                });
            }
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: org.tvp.kirikiri2.KR2Activity.DialogMessage.4
                @Override // android.content.DialogInterface.OnCancelListener
                public void onCancel(DialogInterface dialog) {
                    DialogMessage.this.onButtonClick(-1);
                }
            });
            return builder;
        }

        public void ShowMessageBox() {
            CreateBuilder().create().show();
        }

        public void ShowInputBox(String text) {
            AlertDialog.Builder builder = CreateBuilder();
            this.TextEditor = new EditText(KR2Activity.sInstance);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
            this.TextEditor.setLayoutParams(lp);
            this.TextEditor.setText(text);
            builder.setView(this.TextEditor);
            AlertDialog ad = builder.create();
            ad.show();
            this.TextEditor.requestFocus();
            InputMethodManager imm = (InputMethodManager) KR2Activity.getContext().getSystemService("input_method");
            imm.showSoftInput(this.TextEditor, 0);
        }
    }

    public void handleMessage(Message msg) {
    }

    void updateLicense() {
    }

    public static void reqUpdateLicense() {
        sInstance.updateLicense();
    }

    public static void ShowMessageBox(String title, String text, String[] Buttons) {
        mDialogMessage.Init(title, text, Buttons);
        msgHandler.post(new Runnable() { // from class: org.tvp.kirikiri2.KR2Activity.2
            @Override // java.lang.Runnable
            public void run() {
                KR2Activity.mDialogMessage.ShowMessageBox();
            }
        });
    }

    public static void ShowInputBox(String title, String prompt, final String text, String[] Buttons) {
        mDialogMessage.Init(title, prompt, Buttons);
        msgHandler.post(new Runnable() { // from class: org.tvp.kirikiri2.KR2Activity.3
            @Override // java.lang.Runnable
            public void run() {
                KR2Activity.mDialogMessage.ShowInputBox(text);
            }
        });
    }

    static class ShowTextInputTask implements Runnable {
        static final int HEIGHT_PADDING = 15;
        public int h;
        public int w;
        public int x;
        public int y;

        public ShowTextInputTask(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override // java.lang.Runnable
        public void run() {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(this.w, this.h + 15);
            params.leftMargin = this.x;
            params.topMargin = this.y;
            if (KR2Activity.mTextEdit == null) {
                KR2Activity.mTextEdit = new DummyEdit(KR2Activity.getContext());
                KR2Activity.sInstance.mFrameLayout.addView(KR2Activity.mTextEdit, params);
            } else {
                KR2Activity.mTextEdit.setLayoutParams(params);
            }
            KR2Activity.mTextEdit.setVisibility(View.VISIBLE);
            KR2Activity.mTextEdit.requestFocus();
            InputMethodManager imm = (InputMethodManager) KR2Activity.getContext().getSystemService("input_method");
            imm.showSoftInput(KR2Activity.mTextEdit, 0);
        }
    }

    public static void showTextInput(int x, int y, int w, int h) {
        msgHandler.post(new ShowTextInputTask(x, y, w, h));
    }

    public static void hideTextInput() {
        msgHandler.post(new Runnable() { // from class: org.tvp.kirikiri2.KR2Activity.4
            @Override // java.lang.Runnable
            public void run() {
                if (KR2Activity.mTextEdit != null) {
                    KR2Activity.mTextEdit.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager) KR2Activity.sInstance.getSystemService("input_method");
                    imm.hideSoftInputFromWindow(KR2Activity.mTextEdit.getWindowToken(), 0);
                }
            }
        });
    }

    public static void MessageController(int what, int arg1, int arg2) {
        Message msg = msgHandler.obtainMessage();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msgHandler.sendMessage(msg);
    }

    public static String GetVersion() {
        try {
            String verstr = sInstance.getPackageManager().getPackageInfo(sInstance.getPackageName(), 0).versionName;
            return verstr;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public String[] getStoragePath() {
        String[] ret = new String[0];
        if (this.mStorageManager == null) {
            this.mStorageManager = (StorageManager) getSystemService("storage");
            try {
                this.mMethodGetPaths = StorageManager.class.getMethod("getVolumePaths", new Class[0]);
                this.mGetVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (this.mMethodGetPaths != null) {
            try {
                ret = (String[]) this.mMethodGetPaths.invoke(this.mStorageManager, new Object[0]);
            } catch (IllegalAccessException e2) {
            } catch (IllegalArgumentException e3) {
            } catch (InvocationTargetException e4) {
            } catch (Exception e5) {
            }
        }
        if (this.mGetVolumeState != null) {
            for (int i = 0; i < ret.length; i++) {
                try {
                    String status = (String) this.mGetVolumeState.invoke(this.mStorageManager, ret[i]);
                    if (!"mounted".equals(status) && !"mounted_ro".equals(status)) {
                        ret[i] = null;
                    }
                } catch (IllegalAccessException e6) {
                } catch (IllegalArgumentException e7) {
                } catch (InvocationTargetException e8) {
                } catch (Exception e9) {
                }
            }
        }
        return ret;
    }

    class KR2GLSurfaceView extends Cocos2dxGLSurfaceView {
        public KR2GLSurfaceView(Context context) {
            super(context);
        }

        public KR2GLSurfaceView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override // org.cocos2dx.lib.Cocos2dxGLSurfaceView
        public void insertText(String pText) {
            KR2Activity.nativeInsertText(pText);
        }

        @Override // org.cocos2dx.lib.Cocos2dxGLSurfaceView
        public void deleteBackward() {
            KR2Activity.nativeDeleteBackward();
        }

        @Override // org.cocos2dx.lib.Cocos2dxGLSurfaceView, android.view.View, android.view.KeyEvent.Callback
        public boolean onKeyDown(int pKeyCode, KeyEvent pKeyEvent) {
            switch (pKeyCode) {
                case 4:
                case 19:
                case 20:
                case MotionEventCompat.AXIS_WHEEL /* 21 */:
                case MotionEventCompat.AXIS_GAS /* 22 */:
                case MotionEventCompat.AXIS_BRAKE /* 23 */:
                case 66:
                case 82:
                case 85:
                    KR2Activity.nativeKeyAction(pKeyCode, true);
                    return true;
                default:
                    return super.onKeyDown(pKeyCode, pKeyEvent);
            }
        }

        @Override // org.cocos2dx.lib.Cocos2dxGLSurfaceView, android.view.View, android.view.KeyEvent.Callback
        public boolean onKeyUp(int pKeyCode, KeyEvent pKeyEvent) {
            switch (pKeyCode) {
                case 4:
                case 19:
                case 20:
                case MotionEventCompat.AXIS_WHEEL /* 21 */:
                case MotionEventCompat.AXIS_GAS /* 22 */:
                case MotionEventCompat.AXIS_BRAKE /* 23 */:
                case 66:
                case 82:
                case 85:
                    KR2Activity.nativeKeyAction(pKeyCode, false);
                    return true;
                default:
                    return super.onKeyUp(pKeyCode, pKeyEvent);
            }
        }

        @Override // android.view.View
        public boolean onHoverEvent(MotionEvent pMotionEvent) {
            int pointerNumber = pMotionEvent.getPointerCount();
            float[] xs = new float[pointerNumber];
            float[] ys = new float[pointerNumber];
            for (int i = 0; i < pointerNumber; i++) {
                xs[i] = pMotionEvent.getX(i);
                ys[i] = pMotionEvent.getY(i);
            }
            switch (pMotionEvent.getActionMasked()) {
                case 7:
                    KR2Activity.nativeHoverMoved(xs[0], ys[0]);
                    break;
            }
            return true;
        }

        @Override // org.cocos2dx.lib.Cocos2dxGLSurfaceView, android.view.View
        public boolean onTouchEvent(MotionEvent pMotionEvent) {
            int pointerNumber = pMotionEvent.getPointerCount();
            int[] ids = new int[pointerNumber];
            float[] xs = new float[pointerNumber];
            float[] ys = new float[pointerNumber];
            for (int i = 0; i < pointerNumber; i++) {
                ids[i] = pMotionEvent.getPointerId(i);
                xs[i] = pMotionEvent.getX(i);
                ys[i] = pMotionEvent.getY(i);
            }
            switch (pMotionEvent.getAction() & 255) {
                case 0:
                    int idDown = pMotionEvent.getPointerId(0);
                    float xDown = xs[0];
                    float yDown = ys[0];
                    KR2Activity.nativeTouchesBegin(idDown, xDown, yDown);
                    break;
                case 1:
                    int idUp = pMotionEvent.getPointerId(0);
                    float xUp = xs[0];
                    float yUp = ys[0];
                    KR2Activity.nativeTouchesEnd(idUp, xUp, yUp);
                    break;
                case 2:
                    KR2Activity.nativeTouchesMove(ids, xs, ys);
                    break;
                case 3:
                    KR2Activity.nativeTouchesCancel(ids, xs, ys);
                    break;
                case 5:
                    int indexPointerDown = pMotionEvent.getAction() >> 8;
                    int idPointerDown = pMotionEvent.getPointerId(indexPointerDown);
                    float xPointerDown = pMotionEvent.getX(indexPointerDown);
                    float yPointerDown = pMotionEvent.getY(indexPointerDown);
                    KR2Activity.nativeTouchesBegin(idPointerDown, xPointerDown, yPointerDown);
                    break;
                case 6:
                    int indexPointUp = pMotionEvent.getAction() >> 8;
                    int idPointUp = pMotionEvent.getPointerId(indexPointUp);
                    float xPointUp = pMotionEvent.getX(indexPointUp);
                    float yPointUp = pMotionEvent.getY(indexPointUp);
                    KR2Activity.nativeTouchesEnd(idPointUp, xPointUp, yPointUp);
                    break;
            }
            return true;
        }

        @Override // android.view.View
        @TargetApi(MotionEventCompat.AXIS_RX)
        public boolean onGenericMotionEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case 8:
                    float v = event.getAxisValue(9);
                    KR2Activity.nativeMouseScrolled(-v);
                    return true;
                default:
                    return super.onGenericMotionEvent(event);
            }
        }
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity
    public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new KR2GLSurfaceView(this);
        hideSystemUI();
        if (this.mGLContextAttrs[3] > 0) {
            glSurfaceView.getHolder().setFormat(-3);
        }
        Cocos2dxActivity.Cocos2dxEGLConfigChooser chooser = new Cocos2dxActivity.Cocos2dxEGLConfigChooser(this.mGLContextAttrs);
        glSurfaceView.setEGLConfigChooser(chooser);
        return glSurfaceView;
    }

    public int get_res_sd_operate_step() {
        return -1;
    }

    static void requireLEXA(final String path) {
        msgHandler.post(new Runnable() { // from class: org.tvp.kirikiri2.KR2Activity.5
            @Override // java.lang.Runnable
            public void run() {
                KR2Activity.guideDialogForLEXA(path);
            }
        });
    }

    static void guideDialogForLEXA(String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(sInstance);
        ImageView image = new ImageView(sInstance);
        image.setImageResource(sInstance.get_res_sd_operate_step());
        builder.setView(image).setTitle(path).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: org.tvp.kirikiri2.KR2Activity.6
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                KR2Activity.triggerStorageAccessFramework();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // from class: org.tvp.kirikiri2.KR2Activity.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
    }

    static final boolean isWritable(File file) {
        boolean zCanWrite = false;
        if (file != null) {
            boolean isExisting = file.exists();
            try {
                FileOutputStream output = new FileOutputStream(file, true);
                try {
                    output.close();
                } catch (IOException e) {
                }
                zCanWrite = file.canWrite();
                if (!isExisting) {
                    file.delete();
                }
            } catch (FileNotFoundException e2) {
            }
        }
        return zCanWrite;
    }

    static final boolean isWritableNormal(String path) {
        File f = new File(path);
        return isWritable(f);
    }

    static final boolean isWritableNormalOrSaf(String path) {
        File file;
        Context c = sInstance;
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }
        int i = 0;
        do {
            i++;
            String fileName = "AugendiagnoseDummyFile" + i;
            file = new File(folder, fileName);
        } while (file.exists());
        if (isWritable(file)) {
            return true;
        }
        DocumentFile document = getDocumentFile(file, false, c);
        if (document == null) {
            return false;
        }
        boolean result = document.canWrite() && file.exists();
        document.delete();
        return result;
    }

    @TargetApi(19)
    private static String[] getExtSdCardPaths(Context context) {
        List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("FileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                    }
                    paths.add(path);
                }
            }
        }
        return (String[]) paths.toArray(new String[0]);
    }

    public static String getExtSdCardFolder(File file, Context context) {
        if (_extSdPaths == null) {
            _extSdPaths = getExtSdCardPaths(context);
        }
        for (int i = 0; i < _extSdPaths.length; i++) {
            try {
                if (file.getCanonicalPath().startsWith(_extSdPaths[i])) {
                    return _extSdPaths[i];
                }
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public static boolean isOnExtSdCard(File file, Context c) {
        return getExtSdCardFolder(file, c) != null;
    }

    public static DocumentFile getDocumentFile(File file, boolean isDirectory, Context context) {
        String baseFolder = getExtSdCardFolder(file, context);
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }
        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath)) {
                relativePath = fullPath.substring(baseFolder.length() + 1);
            } else {
                originalDirectory = true;
            }
        } catch (IOException e) {
            return null;
        } catch (Exception e2) {
            originalDirectory = true;
        }
        String as = PreferenceManager.getDefaultSharedPreferences(context).getString("URI", null);
        Uri treeUri = as != null ? Uri.parse(as) : null;
        if (treeUri == null) {
            return null;
        }
        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        if (!originalDirectory) {
            String[] parts = relativePath.split("\\/");
            for (int i = 0; i < parts.length; i++) {
                DocumentFile nextDocument = document.findFile(parts[i]);
                if (nextDocument == null) {
                    try {
                        if (i < parts.length - 1 || isDirectory) {
                            nextDocument = document.createDirectory(parts[i]);
                        } else {
                            nextDocument = document.createFile("image", parts[i]);
                        }
                    } catch (Exception e3) {
                        return null;
                    }
                }
                document = nextDocument;
            }
            return document;
        }
        return document;
    }

    public static boolean RenameFile(String from, String to) {
        File file = new File(from);
        File target = new File(to);
        if (!file.exists()) {
            return false;
        }
        if (target.exists() && !DeleteFile(target.getAbsolutePath())) {
            return false;
        }
        File parent = target.getParentFile();
        if (!parent.exists() && !CreateFolders(parent.getAbsolutePath())) {
            return false;
        }
        if (file.renameTo(target)) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            DocumentFile document = getDocumentFile(file, false, sInstance);
            if (document.renameTo(target.getName())) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT != 19) {
            return false;
        }
        try {
            FileInputStream input = new FileInputStream(file);
            int filesize = (int) file.length();
            byte[] buffer = new byte[filesize];
            input.read(buffer);
            input.close();
            OutputStream out = MediaStoreHack.getOutputStream(sInstance, target.getAbsolutePath());
            out.write(buffer);
            out.close();
            return MediaStoreHack.delete(sInstance, file);
        } catch (IOException e) {
            return false;
        }
    }

    public static final boolean deleteFilesInFolder(File folder, Context context) {
        boolean totalSuccess = true;
        if (folder == null) {
            return false;
        }
        if (folder.isDirectory()) {
            for (File child : folder.listFiles()) {
                deleteFilesInFolder(child, context);
            }
            if (!folder.delete()) {
                totalSuccess = false;
            }
        } else if (!folder.delete()) {
            totalSuccess = false;
        }
        return totalSuccess;
    }

    public static boolean DeleteFile(String path) {
        File file = new File(path);
        boolean fileDelete = deleteFilesInFolder(file, sInstance);
        if (file.delete() || fileDelete) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= 21 && isOnExtSdCard(file, sInstance)) {
            DocumentFile document = getDocumentFile(file, false, sInstance);
            return document.delete();
        }
        if (Build.VERSION.SDK_INT != 19) {
            return !file.exists();
        }
        ContentResolver resolver = sInstance.getContentResolver();
        try {
            Uri uri = MediaStoreHack.getUriFromFile(file.getAbsolutePath(), sInstance);
            resolver.delete(uri, null, null);
            return !file.exists();
        } catch (Exception e) {
            Log.e("FileUtils", "Error when deleting file " + file.getAbsolutePath(), e);
            return false;
        }
    }

    public static OutputStream getOutputStream(@NonNull File target, Context context, long s) throws Exception {
        OutputStream outStream = null;
        try {
            if (isWritable(target)) {
                outStream = new FileOutputStream(target);
            } else if (Build.VERSION.SDK_INT >= 21) {
                DocumentFile targetDocument = getDocumentFile(target, false, context);
                outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
            } else if (Build.VERSION.SDK_INT == 19) {
                return MediaStoreHack.getOutputStream(context, target.getPath());
            }
        } catch (Exception e) {
            Log.e("FileUtils", "Error when copying file from " + target.getAbsolutePath(), e);
        }
        return outStream;
    }

    static String[] splitFileName(String name) {
        String[] ret = new String[2];
        int pos = name.lastIndexOf(46);
        if (pos >= 0) {
            ret[0] = name.substring(0, pos);
            ret[1] = name.substring(pos);
        } else {
            ret[0] = name;
            ret[1] = "";
        }
        return ret;
    }

    static boolean _WriteFile(File target, byte[] data) {
        OutputStream out = null;
        try {
            if (isWritable(target)) {
                OutputStream os = new FileOutputStream(target);
                os.write(data);
                os.close();
                return true;
            }
            if (Build.VERSION.SDK_INT >= 21) {
                DocumentFile document = getDocumentFile(target, false, sInstance);
                try {
                    Uri docUri = document.getUri();
                    out = sInstance.getContentResolver().openOutputStream(docUri);
                } catch (IOException e) {
                }
            } else {
                if (Build.VERSION.SDK_INT != 19) {
                    return false;
                }
                Uri uri = MediaStoreHack.getUriFromFile(target.getAbsolutePath(), sInstance);
                out = sInstance.getContentResolver().openOutputStream(uri);
            }
            if (out != null) {
                out.write(data);
                out.close();
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean WriteFile(String path, byte[] data) {
        File target = new File(path);
        String bakname = null;
        if (target.exists()) {
            String parentPath = target.getParent();
            String[] splitedPath = splitFileName(target.getName());
            String bakname2 = String.valueOf(new Date().getTime());
            bakname = String.valueOf(parentPath) + "/" + splitedPath[0] + "." + bakname2 + splitedPath[1];
            RenameFile(path, bakname);
        }
        boolean ret = _WriteFile(target, data);
        if (ret && bakname != null) {
            DeleteFile(bakname);
        }
        return ret;
    }

    public static boolean CreateFolders(String path) {
        File file = new File(path);
        if (file.mkdirs()) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            DocumentFile document = getDocumentFile(file, true, sInstance);
            return document.exists();
        }
        if (Build.VERSION.SDK_INT == 19) {
            try {
                return MediaStoreHack.mkdir(sInstance, file);
            } catch (Throwable e) {
                return false;
            }
        }
        return false;
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @TargetApi(11)
    void doSetSystemUiVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(5894);
    }

    void hideSystemUI() {
        if (nativeGetHideSystemButton() && Build.VERSION.SDK_INT >= 19) {
            doSetSystemUiVisibility();
        }
    }

    public static String getLocaleName() {
        Locale defloc = Locale.getDefault();
        String lang = defloc.getLanguage();
        String country = defloc.getCountry();
        if (!country.isEmpty()) {
            return String.valueOf(String.valueOf(lang) + "_") + country.toLowerCase();
        }
        return lang;
    }

    public static void exit() {
        System.exit(0);
    }

    public static void setOrientation(int orient) {
        if (orient == 1) {
            sInstance.setRequestedOrientation(1);
        } else if (orient == 2) {
            sInstance.setRequestedOrientation(0);
        }
    }

    @TargetApi(MotionEventCompat.AXIS_WHEEL)
    public static void triggerStorageAccessFramework() {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
        sInstance.startActivityForResult(intent, 3);
    }

    @Override // org.cocos2dx.lib.Cocos2dxActivity, android.app.Activity
    @TargetApi(19)
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == 3) {
            String p = this.Sp.getString("URI", null);
            Uri oldUri = p != null ? Uri.parse(p) : null;
            Uri treeUri = null;
            if (responseCode == -1 && (treeUri = intent.getData()) != null) {
                this.Sp.edit().putString("URI", treeUri.toString()).commit();
            }
            if (responseCode != -1) {
                if (treeUri != null) {
                    this.Sp.edit().putString("URI", oldUri.toString()).commit();
                }
            } else {
                int takeFlags = intent.getFlags() & 3;
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            }
        }
    }
}