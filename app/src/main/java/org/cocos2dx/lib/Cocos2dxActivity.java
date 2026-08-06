package org.cocos2dx.lib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/* JADX INFO: loaded from: classes.dex */
public abstract class Cocos2dxActivity extends Activity implements Cocos2dxHelper.Cocos2dxHelperListener {
    private static final String TAG = Cocos2dxActivity.class.getSimpleName();
    private static Cocos2dxActivity sContext = null;
    private Cocos2dxGLSurfaceView mGLSurfaceView = null;
    public int[] mGLContextAttrs = null;
    private Cocos2dxHandler mHandler = null;
    private Cocos2dxVideoHelper mVideoHelper = null;
    private Cocos2dxWebViewHelper mWebViewHelper = null;
    private Cocos2dxEditBoxHelper mEditBoxHelper = null;
    private boolean hasFocus = false;
    protected ResizeLayout mFrameLayout = null;

    private static native int[] getGLContextAttrs();

    public Cocos2dxGLSurfaceView getGLSurfaceView() {
        return this.mGLSurfaceView;
    }

    public class Cocos2dxEGLConfigChooser implements GLSurfaceView.EGLConfigChooser {
        protected int[] configAttribs;

        public Cocos2dxEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            this.configAttribs = new int[]{redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize};
        }

        public Cocos2dxEGLConfigChooser(int[] attribs) {
            this.configAttribs = attribs;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            int[] value = new int[1];
            if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                int defaultValue2 = value[0];
                return defaultValue2;
            }
            return defaultValue;
        }

        class ConfigValue implements Comparable<ConfigValue> {
            public EGLConfig config;
            public int[] configAttribs;
            public int value;

            private void calcValue() {
                if (this.configAttribs[4] > 0) {
                    this.value = this.value + 536870912 + ((this.configAttribs[4] % 64) << 6);
                }
                if (this.configAttribs[5] > 0) {
                    this.value = this.value + 268435456 + (this.configAttribs[5] % 64);
                }
                if (this.configAttribs[3] > 0) {
                    this.value = this.value + 1073741824 + ((this.configAttribs[3] % 16) << 24);
                }
                if (this.configAttribs[1] > 0) {
                    this.value += (this.configAttribs[1] % 16) << 20;
                }
                if (this.configAttribs[2] > 0) {
                    this.value += (this.configAttribs[2] % 16) << 16;
                }
                if (this.configAttribs[0] > 0) {
                    this.value += (this.configAttribs[0] % 16) << 12;
                }
            }

            public ConfigValue(int[] attribs) {
                this.config = null;
                this.configAttribs = null;
                this.value = 0;
                this.configAttribs = attribs;
                calcValue();
            }

            public ConfigValue(EGL10 egl, EGLDisplay display, EGLConfig config) {
                this.config = null;
                this.configAttribs = null;
                this.value = 0;
                this.config = config;
                this.configAttribs = new int[6];
                this.configAttribs[0] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12324, 0);
                this.configAttribs[1] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12323, 0);
                this.configAttribs[2] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12322, 0);
                this.configAttribs[3] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12321, 0);
                this.configAttribs[4] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12325, 0);
                this.configAttribs[5] = Cocos2dxEGLConfigChooser.this.findConfigAttrib(egl, display, config, 12326, 0);
                calcValue();
            }

            @Override // java.lang.Comparable
            public int compareTo(ConfigValue another) {
                if (this.value < another.value) {
                    return -1;
                }
                if (this.value > another.value) {
                    return 1;
                }
                return 0;
            }

            public String toString() {
                return "{ color: " + this.configAttribs[3] + this.configAttribs[2] + this.configAttribs[1] + this.configAttribs[0] + "; depth: " + this.configAttribs[4] + "; stencil: " + this.configAttribs[5] + ";}";
            }
        }

        @Override // android.opengl.GLSurfaceView.EGLConfigChooser
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] EGLattribs = {12324, this.configAttribs[0], 12323, this.configAttribs[1], 12322, this.configAttribs[2], 12321, this.configAttribs[3], 12325, this.configAttribs[4], 12326, this.configAttribs[5], 12352, 4, 12344};
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            boolean eglChooseResult = egl.eglChooseConfig(display, EGLattribs, configs, 1, numConfigs);
            if (eglChooseResult && numConfigs[0] > 0) {
                return configs[0];
            }
            int[] EGLV2attribs = {12352, 4, 12344};
            boolean eglChooseResult2 = egl.eglChooseConfig(display, EGLV2attribs, null, 0, numConfigs);
            if (eglChooseResult2 && numConfigs[0] > 0) {
                int num = numConfigs[0];
                ConfigValue[] cfgVals = new ConfigValue[num];
                EGLConfig[] configs2 = new EGLConfig[num];
                egl.eglChooseConfig(display, EGLV2attribs, configs2, num, numConfigs);
                for (int i = 0; i < num; i++) {
                    cfgVals[i] = new ConfigValue(egl, display, configs2[i]);
                }
                ConfigValue e = new ConfigValue(this.configAttribs);
                int lo = 0;
                int hi = num;
                while (lo < hi - 1) {
                    int mi = (lo + hi) / 2;
                    if (e.compareTo(cfgVals[mi]) < 0) {
                        hi = mi;
                    } else {
                        lo = mi;
                    }
                }
                if (lo != num - 1) {
                    lo++;
                }
                Log.w("cocos2d", "Can't find EGLConfig match: " + e + ", instead of closest one:" + cfgVals[lo]);
                return cfgVals[lo].config;
            }
            Log.e("device_policy", "Can not select an EGLConfig for rendering.");
            return null;
        }
    }

    public static Context getContext() {
        return sContext;
    }

    public void setKeepScreenOn(final boolean value) {
        runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxActivity.1
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxActivity.this.mGLSurfaceView.setKeepScreenOn(value);
            }
        });
    }

    protected void onLoadNativeLibraries() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 128);
            Bundle bundle = ai.metaData;
            String libName = bundle.getString("android.app.lib_name");
            System.loadLibrary(libName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onLoadNativeLibraries();
        sContext = this;
        this.mHandler = new Cocos2dxHandler(this);
        Cocos2dxHelper.init(this);
        this.mGLContextAttrs = getGLContextAttrs();
        init();
        if (this.mVideoHelper == null) {
            this.mVideoHelper = new Cocos2dxVideoHelper(this, this.mFrameLayout);
        }
        if (this.mWebViewHelper == null) {
            this.mWebViewHelper = new Cocos2dxWebViewHelper(this.mFrameLayout);
        }
        if (this.mEditBoxHelper == null) {
            this.mEditBoxHelper = new Cocos2dxEditBoxHelper(this.mFrameLayout);
        }
        Window window = getWindow();
        window.setSoftInputMode(32);
    }

    @Override // android.app.Activity
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        resumeIfHasFocus();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged() hasFocus=" + hasFocus);
        super.onWindowFocusChanged(hasFocus);
        this.hasFocus = hasFocus;
        resumeIfHasFocus();
    }

    private void resumeIfHasFocus() {
        if (this.hasFocus) {
            Cocos2dxHelper.onResume();
            this.mGLSurfaceView.onResume();
        }
    }

    @Override // android.app.Activity
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        Cocos2dxHelper.onPause();
        this.mGLSurfaceView.onPause();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override // org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener
    public void showDialog(String pTitle, String pMessage) {
        Message msg = new Message();
        msg.what = 1;
        msg.obj = new Cocos2dxHandler.DialogMessage(pTitle, pMessage);
        this.mHandler.sendMessage(msg);
    }

    @Override // org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener
    public void runOnGLThread(Runnable pRunnable) {
        this.mGLSurfaceView.queueEvent(pRunnable);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (PreferenceManager.OnActivityResultListener listener : Cocos2dxHelper.getOnActivityResultListeners()) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void init() {
        ViewGroup.LayoutParams framelayout_params = new ViewGroup.LayoutParams(-1, -1);
        this.mFrameLayout = new ResizeLayout(this);
        this.mFrameLayout.setLayoutParams(framelayout_params);
        ViewGroup.LayoutParams edittext_layout_params = new ViewGroup.LayoutParams(-1, -2);
        Cocos2dxEditBox edittext = new Cocos2dxEditBox(this);
        edittext.setLayoutParams(edittext_layout_params);
        this.mFrameLayout.addView(edittext);
        this.mGLSurfaceView = onCreateView();
        this.mFrameLayout.addView(this.mGLSurfaceView);
        if (isAndroidEmulator()) {
            this.mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        }
        this.mGLSurfaceView.setCocos2dxRenderer(new Cocos2dxRenderer());
        this.mGLSurfaceView.setCocos2dxEditText(edittext);
        setContentView(this.mFrameLayout);
    }

    public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
        if (this.mGLContextAttrs[3] > 0) {
            glSurfaceView.getHolder().setFormat(-3);
        }
        Cocos2dxEGLConfigChooser chooser = new Cocos2dxEGLConfigChooser(this.mGLContextAttrs);
        glSurfaceView.setEGLConfigChooser(chooser);
        return glSurfaceView;
    }

    private static final boolean isAndroidEmulator() {
        String model = Build.MODEL;
        Log.d(TAG, "model=" + model);
        String product = Build.PRODUCT;
        Log.d(TAG, "product=" + product);
        boolean isEmulator = false;
        if (product != null) {
            isEmulator = product.equals("sdk") || product.contains("_sdk") || product.contains("sdk_");
        }
        Log.d(TAG, "isEmulator=" + isEmulator);
        return isEmulator;
    }
}
