package org.libsdl.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class SDLActivity extends Activity {
    static final int COMMAND_CHANGE_TITLE = 1;
    static final int COMMAND_CHANGE_WINDOW_STYLE = 2;
    static final int COMMAND_SET_KEEP_SCREEN_ON = 5;
    static final int COMMAND_TEXTEDIT_HIDE = 3;
    protected static final int COMMAND_USER = 32768;
    private static final String TAG = "SDL";
    private static Object expansionFile;
    private static Method expansionFileMethod;
    public static boolean mBrokenLibraries;
    protected static SDLClipboardHandler mClipboardHandler;
    public static NativeState mCurrentNativeState;
    public static boolean mExitCalledFromJava;
    public static boolean mHasFocus;
    public static boolean mIsResumedCalled;
    public static boolean mIsSurfaceReady;
    protected static ViewGroup mLayout;
    public static NativeState mNextNativeState;
    protected static Thread mSDLThread;
    protected static boolean mScreenKeyboardShown;
    public static boolean mSeparateMouseAndTouch;
    protected static SDLActivity mSingleton;
    protected static SDLSurface mSurface;
    protected static View mTextEdit;
    Handler commandHandler = new SDLCommandHandler();
    protected final int[] messageboxSelection = new int[1];
    protected int dialogs = 0;

    public enum NativeState {
        INIT,
        RESUMED,
        PAUSED;

        /* JADX INFO: renamed from: values, reason: to resolve conflict with enum method */
        public static NativeState[] valuesCustom() {
            NativeState[] nativeStateArrValuesCustom = values();
            int length = nativeStateArrValuesCustom.length;
            NativeState[] nativeStateArr = new NativeState[length];
            System.arraycopy(nativeStateArrValuesCustom, 0, nativeStateArr, 0, length);
            return nativeStateArr;
        }
    }

    public static native String nativeGetHint(String str);

    public static native void nativeLowMemory();

    public static native void nativePause();

    public static native void nativeQuit();

    public static native void nativeResume();

    public static native int nativeRunMain(String str, String str2, Object obj);

    public static native void nativeSetenv(String str, String str2);

    public static native int nativeSetupJNI();

    public static native void onNativeAccel(float f, float f2, float f3);

    public static native void onNativeClipboardChanged();

    public static native void onNativeDropFile(String str);

    public static native void onNativeKeyDown(int i);

    public static native void onNativeKeyUp(int i);

    public static native void onNativeKeyboardFocusLost();

    public static native void onNativeMouse(int i, int i2, float f, float f2);

    public static native void onNativeResize(int i, int i2, int i3, float f);

    public static native void onNativeSurfaceChanged();

    public static native void onNativeSurfaceDestroyed();

    public static native void onNativeTouch(int i, int i2, int i3, float f, float f2, float f3);

    protected String getMainSharedObject() {
        String[] libraries = mSingleton.getLibraries();
        if (libraries.length > 0) {
            String library = "lib" + libraries[libraries.length - 1] + ".so";
            return library;
        }
        return "libmain.so";
    }

    protected String getMainFunction() {
        return "SDL_main";
    }

    protected String[] getLibraries() {
        return new String[]{"game"};
    }

    public void loadLibraries() {
        for (String lib : getLibraries()) {
            System.loadLibrary(lib);
        }
    }

    protected String[] getArguments() {
        return new String[0];
    }

    public static void initialize() {
        mSingleton = null;
        mSurface = null;
        mTextEdit = null;
        mLayout = null;
        mClipboardHandler = null;
        mSDLThread = null;
        mExitCalledFromJava = false;
        mBrokenLibraries = false;
        mIsResumedCalled = false;
        mIsSurfaceReady = false;
        mHasFocus = true;
        mNextNativeState = NativeState.INIT;
        mCurrentNativeState = NativeState.INIT;
    }

    // ========== 修正后的 onCreate ==========
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long startTime = System.currentTimeMillis();
        Log.i(TAG, "SDL onCreate START");

        super.onCreate(savedInstanceState);

        String errorMsgBrokenLib = "";
        try {
            loadLibraries();
        } catch (Exception e) {
            errorMsgBrokenLib = e.getMessage();
        }
        Log.i(TAG, "loadLibraries cost: " + (System.currentTimeMillis() - startTime) + "ms");

        SDL.setupJNI();
        SDL.initialize();
        Log.i(TAG, "setupJNI cost: " + (System.currentTimeMillis() - startTime) + "ms");

        mSingleton = this;

        if (errorMsgBrokenLib != null && !errorMsgBrokenLib.isEmpty()) {
            showBrokenLibDialog(errorMsgBrokenLib);
            return;
        }

        // 剪贴板处理器（原有逻辑）
        if (Build.VERSION.SDK_INT >= 11) {
            mClipboardHandler = new SDLClipboardHandler_API11();
        } else {
            mClipboardHandler = new SDLClipboardHandler_Old();
        }

        // 创建 Surface 视图
        mSurface = new SDLSurface(getApplication());

        // 沉浸式全屏（Android 4.4+）
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }

        // 将 Surface 添加到布局并设为内容视图
        mLayout = new RelativeLayout(this);
        mLayout.addView(mSurface);
        setContentView(mLayout);

        // 窗口样式（原有调用）
        setWindowStyle(false);

        Log.i(TAG, "setContentView cost: " + (System.currentTimeMillis() - startTime) + "ms");

        // 处理外部打开的文件
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            String filename = intent.getData().getPath();
            if (filename != null) {
                Log.v(TAG, "Got filename: " + filename);
                onNativeDropFile(filename);
            }
        }

        // 触发界面恢复（启动 SDL 线程）
        mNextNativeState = NativeState.RESUMED;
        handleNativeState();
    }
    // =====================================

    // 显示库加载失败对话框（新增方法）
    private void showBrokenLibDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Library Load Error")
                .setMessage("Failed to load native libraries:\n" + message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()");
        super.onPause();
        mNextNativeState = NativeState.PAUSED;
        mIsResumedCalled = false;
        if (!mBrokenLibraries) {
            handleNativeState();
        }
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
        mNextNativeState = NativeState.RESUMED;
        mIsResumedCalled = true;
        if (!mBrokenLibraries) {
            handleNativeState();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.v(TAG, "onWindowFocusChanged(): " + hasFocus);
        if (!mBrokenLibraries) {
            mHasFocus = hasFocus;
            if (hasFocus) {
                mNextNativeState = NativeState.RESUMED;
            } else {
                mNextNativeState = NativeState.PAUSED;
            }
            handleNativeState();
        }
    }

    @Override
    public void onLowMemory() {
        Log.v(TAG, "onLowMemory()");
        super.onLowMemory();
        if (!mBrokenLibraries) {
            nativeLowMemory();
        }
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        if (mBrokenLibraries) {
            super.onDestroy();
            initialize();
            return;
        }
        mNextNativeState = NativeState.PAUSED;
        handleNativeState();
        mExitCalledFromJava = true;
        nativeQuit();
        if (mSDLThread != null) {
            try {
                mSDLThread.join();
            } catch (Exception e) {
                Log.v(TAG, "Problem stopping thread: " + e);
            }
            mSDLThread = null;
        }
        super.onDestroy();
        initialize();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode;
        if (mBrokenLibraries || (keyCode = event.getKeyCode()) == 25 || keyCode == 24 || keyCode == 27 || keyCode == 168 || keyCode == 169) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    public static void handleNativeState() {
        if (mNextNativeState != mCurrentNativeState) {
            if (mNextNativeState == NativeState.INIT) {
                mCurrentNativeState = mNextNativeState;
                return;
            }
            if (mNextNativeState == NativeState.PAUSED) {
                nativePause();
                if (mSurface != null) {
                    mSurface.handlePause();
                }
                mCurrentNativeState = mNextNativeState;
                return;
            }
            if (mNextNativeState == NativeState.RESUMED && mIsSurfaceReady && mHasFocus && mIsResumedCalled) {
                if (mSDLThread == null) {
                    mSDLThread = new Thread(new SDLMain(), "SDLThread");
                    mSurface.enableSensor(1, true);
                    mSDLThread.start();
                }
                nativeResume();
                mSurface.handleResume();
                mCurrentNativeState = mNextNativeState;
            }
        }
    }

    public static void handleNativeExit() {
        mSDLThread = null;
        mSingleton.finish();
    }

    protected boolean onUnhandledMessage(int command, Object param) {
        return false;
    }

    protected static class SDLCommandHandler extends Handler {
        protected SDLCommandHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            Window window;
            Context context = SDL.getContext();
            if (context == null) {
                Log.e(SDLActivity.TAG, "error handling message, getContext() returned null");
            }
            switch (msg.arg1) {
                case 1:
                    if (context instanceof Activity) {
                        ((Activity) context).setTitle((String) msg.obj);
                    } else {
                        Log.e(SDLActivity.TAG, "error handling message, getContext() returned no Activity");
                    }
                    break;
                case 2:
                    if (Build.VERSION.SDK_INT < 19) {
                    }
                    break;
                case 3:
                    if (SDLActivity.mTextEdit != null) {
                        SDLActivity.mTextEdit.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
                        InputMethodManager imm = (InputMethodManager) context.getSystemService("input_method");
                        imm.hideSoftInputFromWindow(SDLActivity.mTextEdit.getWindowToken(), 0);
                        SDLActivity.mScreenKeyboardShown = false;
                    }
                    break;
                case 4:
                default:
                    if ((context instanceof SDLActivity) && !((SDLActivity) context).onUnhandledMessage(msg.arg1, msg.obj)) {
                        Log.e(SDLActivity.TAG, "error handling message, command is " + msg.arg1);
                        break;
                    }
                    break;
                case 5:
                    if ((context instanceof Activity) && (window = ((Activity) context).getWindow()) != null) {
                        if ((msg.obj instanceof Integer) && ((Integer) msg.obj).intValue() != 0) {
                            window.addFlags(128);
                        } else {
                            window.clearFlags(128);
                        }
                        break;
                    }
                    break;
            }
        }
    }

    boolean sendCommand(int command, Object data) {
        Message msg = this.commandHandler.obtainMessage();
        msg.arg1 = command;
        msg.obj = data;
        return this.commandHandler.sendMessage(msg);
    }

    public static boolean setActivityTitle(String title) {
        return mSingleton.sendCommand(1, title);
    }

    public static void setWindowStyle(boolean fullscreen) {
        mSingleton.sendCommand(2, Integer.valueOf(fullscreen ? 1 : 0));
    }

    public static void setOrientation(int w, int h, boolean resizable, String hint) {
        if (mSingleton != null) {
            mSingleton.setOrientationBis(w, h, resizable, hint);
        }
    }

    public void setOrientationBis(int w, int h, boolean resizable, String hint) {
        int orientation = -1;
        if (hint.contains("LandscapeRight") && hint.contains("LandscapeLeft")) {
            orientation = 6;
        } else if (hint.contains("LandscapeRight")) {
            orientation = 0;
        } else if (hint.contains("LandscapeLeft")) {
            orientation = 8;
        } else if (hint.contains("Portrait") && hint.contains("PortraitUpsideDown")) {
            orientation = 7;
        } else if (hint.contains("Portrait")) {
            orientation = 1;
        } else if (hint.contains("PortraitUpsideDown")) {
            orientation = 9;
        }
        if (orientation == -1 && !resizable) {
            if (w > h) {
                orientation = 6;
            } else {
                orientation = 7;
            }
        }
        Log.v(TAG, "setOrientation() orientation=" + orientation + " width=" + w + " height=" + h + " resizable=" + resizable + " hint=" + hint);
        if (orientation != -1) {
            mSingleton.setRequestedOrientation(orientation);
        }
    }

    public static boolean isScreenKeyboardShown() {
        if (mTextEdit == null || !mScreenKeyboardShown) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager) SDL.getContext().getSystemService("input_method");
        return imm.isAcceptingText();
    }

    public static boolean sendMessage(int command, int param) {
        if (mSingleton == null) {
            return false;
        }
        return mSingleton.sendCommand(command, Integer.valueOf(param));
    }

    public static Context getContext() {
        return SDL.getContext();
    }

    public static boolean isAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) getContext().getSystemService("uimode");
        return uiModeManager.getCurrentModeType() == 4;
    }

    public static DisplayMetrics getDisplayDPI() {
        return getContext().getResources().getDisplayMetrics();
    }

    public static boolean getManifestEnvironmentVariables() {
        try {
            ApplicationInfo applicationInfo = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), 128);
            Bundle bundle = applicationInfo.metaData;
            if (bundle == null) {
                return false;
            }
            int trimLength = "SDL_ENV.".length();
            for (String key : bundle.keySet()) {
                if (key.startsWith("SDL_ENV.")) {
                    String name = key.substring(trimLength);
                    String value = bundle.get(key).toString();
                    nativeSetenv(name, value);
                }
            }
            return true;
        } catch (Exception e) {
            Log.v(TAG, "exception " + e.toString());
            return false;
        }
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

        @Override
        public void run() {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(this.w, this.h + 15);
            params.leftMargin = this.x;
            params.topMargin = this.y;
            if (SDLActivity.mTextEdit == null) {
                SDLActivity.mTextEdit = new DummyEdit(SDL.getContext());
                SDLActivity.mLayout.addView(SDLActivity.mTextEdit, params);
            } else {
                SDLActivity.mTextEdit.setLayoutParams(params);
            }
            SDLActivity.mTextEdit.setVisibility(0);
            SDLActivity.mTextEdit.requestFocus();
            InputMethodManager imm = (InputMethodManager) SDL.getContext().getSystemService("input_method");
            imm.showSoftInput(SDLActivity.mTextEdit, 0);
            SDLActivity.mScreenKeyboardShown = true;
        }
    }

    public static boolean showTextInput(int x, int y, int w, int h) {
        return mSingleton.commandHandler.post(new ShowTextInputTask(x, y, w, h));
    }

    public static boolean isTextInputEvent(KeyEvent event) {
        if (Build.VERSION.SDK_INT < 11 || !event.isCtrlPressed()) {
            return event.isPrintingKey() || event.getKeyCode() == 62;
        }
        return false;
    }

    public static Surface getNativeSurface() {
        if (mSurface == null) {
            return null;
        }
        return mSurface.getNativeSurface();
    }

    public static int[] inputGetInputDeviceIds(int sources) {
        int[] ids = InputDevice.getDeviceIds();
        int[] filtered = new int[ids.length];
        int used = 0;
        for (int i : ids) {
            InputDevice device = InputDevice.getDevice(i);
            if (device != null && (device.getSources() & sources) != 0) {
                filtered[used] = device.getId();
                used++;
            }
        }
        return Arrays.copyOf(filtered, used);
    }

    public static InputStream openAPKExpansionInputStream(String fileName) throws IOException {
        String patchHint;
        InputStream fileStream = null;
        if (expansionFile == null) {
            String mainHint = nativeGetHint("SDL_ANDROID_APK_EXPANSION_MAIN_FILE_VERSION");
            if (mainHint != null && (patchHint = nativeGetHint("SDL_ANDROID_APK_EXPANSION_PATCH_FILE_VERSION")) != null) {
                try {
                    Integer mainVersion = Integer.valueOf(mainHint);
                    Integer patchVersion = Integer.valueOf(patchHint);
                    try {
                        expansionFile = Class.forName("com.android.vending.expansion.zipfile.APKExpansionSupport").getMethod("getAPKExpansionZipFile", Context.class, Integer.TYPE, Integer.TYPE).invoke(null, SDL.getContext(), mainVersion, patchVersion);
                        expansionFileMethod = expansionFile.getClass().getMethod("getInputStream", String.class);
                        fileStream = (InputStream) expansionFileMethod.invoke(expansionFile, fileName);
                        if (fileStream == null) {
                            throw new IOException("Could not find path in APK expansion file");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        expansionFile = null;
                        expansionFileMethod = null;
                        throw new IOException("Could not access APK expansion support library", ex);
                    }
                } catch (NumberFormatException ex2) {
                    ex2.printStackTrace();
                    throw new IOException("No valid file versions set for APK expansion files", ex2);
                }
            }
        } else {
            try {
                fileStream = (InputStream) expansionFileMethod.invoke(expansionFile, fileName);
                if (fileStream == null) {
                    throw new IOException("Could not find path in APK expansion file");
                }
            } catch (Exception ex3) {
                ex3.printStackTrace();
                throw new IOException("Could not open stream from APK expansion file", ex3);
            }
        }
        return fileStream;
    }

    public int messageboxShowMessageBox(int flags, String title, String message, int[] buttonFlags, int[] buttonIds, String[] buttonTexts, int[] colors) {
        int i = -1;
        this.messageboxSelection[0] = -1;
        if (buttonFlags.length == buttonIds.length || buttonIds.length == buttonTexts.length) {
            final Bundle args = new Bundle();
            args.putInt("flags", flags);
            args.putString("title", title);
            args.putString("message", message);
            args.putIntArray("buttonFlags", buttonFlags);
            args.putIntArray("buttonIds", buttonIds);
            args.putStringArray("buttonTexts", buttonTexts);
            args.putIntArray("colors", colors);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SDLActivity sDLActivity = SDLActivity.this;
                    SDLActivity sDLActivity2 = SDLActivity.this;
                    int i2 = sDLActivity2.dialogs;
                    sDLActivity2.dialogs = i2 + 1;
                    sDLActivity.showDialog(i2, args);
                }
            });
            synchronized (this.messageboxSelection) {
                try {
                    this.messageboxSelection.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            i = this.messageboxSelection[0];
        }
        return i;
    }

    @Override
    protected Dialog onCreateDialog(int ignore, Bundle args) {
        int backgroundColor;
        int textColor;
        int buttonBackgroundColor;
        int[] colors = args.getIntArray("colors");
        if (colors != null) {
            int i = (-1) + 1;
            backgroundColor = colors[i];
            int i2 = i + 1;
            textColor = colors[i2];
            int i3 = i2 + 1;
            int i4 = colors[i3];
            int i5 = i3 + 1;
            buttonBackgroundColor = colors[i5];
            int i6 = colors[i5 + 1];
        } else {
            backgroundColor = 0;
            textColor = 0;
            buttonBackgroundColor = 0;
        }
        final Dialog dialog = new Dialog(this);
        dialog.setTitle(args.getString("title"));
        dialog.setCancelable(false);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface unused) {
                synchronized (SDLActivity.this.messageboxSelection) {
                    SDLActivity.this.messageboxSelection.notify();
                }
            }
        });
        TextView message = new TextView(this);
        message.setGravity(17);
        message.setText(args.getString("message"));
        if (textColor != 0) {
            message.setTextColor(textColor);
        }
        int[] buttonFlags = args.getIntArray("buttonFlags");
        int[] buttonIds = args.getIntArray("buttonIds");
        String[] buttonTexts = args.getStringArray("buttonTexts");
        final SparseArray<Button> mapping = new SparseArray<>();
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(0);
        buttons.setGravity(17);
        for (int i7 = 0; i7 < buttonTexts.length; i7++) {
            Button button = new Button(this);
            final int id = buttonIds[i7];
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SDLActivity.this.messageboxSelection[0] = id;
                    dialog.dismiss();
                }
            });
            if (buttonFlags[i7] != 0) {
                if ((buttonFlags[i7] & 1) != 0) {
                    mapping.put(66, button);
                }
                if ((buttonFlags[i7] & 2) != 0) {
                    mapping.put(111, button);
                }
            }
            button.setText(buttonTexts[i7]);
            if (textColor != 0) {
                button.setTextColor(textColor);
            }
            if (buttonBackgroundColor != 0) {
                Drawable drawable = button.getBackground();
                if (drawable == null) {
                    button.setBackgroundColor(buttonBackgroundColor);
                } else {
                    drawable.setColorFilter(buttonBackgroundColor, PorterDuff.Mode.MULTIPLY);
                }
            }
            buttons.addView(button);
        }
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(1);
        content.addView(message);
        content.addView(buttons);
        if (backgroundColor != 0) {
            content.setBackgroundColor(backgroundColor);
        }
        dialog.setContentView(content);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface d, int keyCode, KeyEvent event) {
                Button button2 = (Button) mapping.get(keyCode);
                if (button2 == null) {
                    return false;
                }
                if (event.getAction() != 1) {
                    return true;
                }
                button2.performClick();
                return true;
            }
        });
        return dialog;
    }

    public static boolean clipboardHasText() {
        return mClipboardHandler.clipboardHasText();
    }

    public static String clipboardGetText() {
        return mClipboardHandler.clipboardGetText();
    }

    public static void clipboardSetText(String string) {
        mClipboardHandler.clipboardSetText(string);
    }
}