package org.libsdl.app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

/* JADX INFO: compiled from: SDLActivity.java */
/* JADX INFO: loaded from: classes.dex */
class SDLSurface extends SurfaceView implements SurfaceHolder.Callback, View.OnKeyListener, View.OnTouchListener, SensorEventListener {
    protected static Display mDisplay;
    protected static float mHeight;
    protected static SensorManager mSensorManager;
    protected static float mWidth;

    public SDLSurface(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);
        mDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        mSensorManager = (SensorManager) context.getSystemService("sensor");
        if (Build.VERSION.SDK_INT >= 12) {
            setOnGenericMotionListener(new SDLGenericMotionListener_API12());
        }
        mWidth = 1.0f;
        mHeight = 1.0f;
    }

    public void handlePause() {
        enableSensor(1, false);
    }

    public void handleResume() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this);
        setOnTouchListener(this);
        enableSensor(1, true);
    }

    public Surface getNativeSurface() {
        return getHolder().getSurface();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("SDL", "surfaceCreated()");
        holder.setType(2);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v("SDL", "surfaceDestroyed()");
        SDLActivity.mNextNativeState = SDLActivity.NativeState.PAUSED;
        SDLActivity.handleNativeState();
        SDLActivity.mIsSurfaceReady = false;
        SDLActivity.onNativeSurfaceDestroyed();
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v("SDL", "surfaceChanged()");
        int sdlFormat = 353701890;
        switch (format) {
            case 1:
                Log.v("SDL", "pixel format RGBA_8888");
                sdlFormat = 373694468;
                break;
            case 2:
                Log.v("SDL", "pixel format RGBX_8888");
                sdlFormat = 371595268;
                break;
            case 3:
                Log.v("SDL", "pixel format RGB_888");
                sdlFormat = 370546692;
                break;
            case 4:
                Log.v("SDL", "pixel format RGB_565");
                sdlFormat = 353701890;
                break;
            case 5:
            default:
                Log.v("SDL", "pixel format unknown " + format);
                break;
            case 6:
                Log.v("SDL", "pixel format RGBA_5551");
                sdlFormat = 356782082;
                break;
            case 7:
                Log.v("SDL", "pixel format RGBA_4444");
                sdlFormat = 356651010;
                break;
            case 8:
                Log.v("SDL", "pixel format A_8");
                break;
            case 9:
                Log.v("SDL", "pixel format L_8");
                break;
            case 10:
                Log.v("SDL", "pixel format LA_88");
                break;
            case 11:
                Log.v("SDL", "pixel format RGB_332");
                sdlFormat = 336660481;
                break;
        }
        mWidth = width;
        mHeight = height;
        SDLActivity.onNativeResize(width, height, sdlFormat, mDisplay.getRefreshRate());
        Log.v("SDL", "Window size: " + width + "x" + height);
        boolean skip = false;
        int requestedOrientation = SDLActivity.mSingleton.getRequestedOrientation();
        if (requestedOrientation != -1) {
            if (requestedOrientation == 1 || requestedOrientation == 7) {
                if (mWidth > mHeight) {
                    skip = true;
                }
            } else if ((requestedOrientation == 0 || requestedOrientation == 6) && mWidth < mHeight) {
                skip = true;
            }
        }
        if (skip) {
            double min = Math.min(mWidth, mHeight);
            double max = Math.max(mWidth, mHeight);
            if (max / min < 1.2d) {
                Log.v("SDL", "Don't skip on such aspect-ratio. Could be a square resolution.");
                skip = false;
            }
        }
        if (skip) {
            Log.v("SDL", "Skip .. Surface is not ready.");
            SDLActivity.mIsSurfaceReady = false;
        } else {
            SDLActivity.mIsSurfaceReady = true;
            SDLActivity.onNativeSurfaceChanged();
            SDLActivity.handleNativeState();
        }
    }

    @Override // android.view.View.OnKeyListener
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (SDLControllerManager.isDeviceSDLJoystick(event.getDeviceId())) {
            if (event.getAction() == 0) {
                if (SDLControllerManager.onNativePadDown(event.getDeviceId(), keyCode) == 0) {
                    return true;
                }
            } else if (event.getAction() == 1 && SDLControllerManager.onNativePadUp(event.getDeviceId(), keyCode) == 0) {
                return true;
            }
        }
        if ((event.getSource() & 257) != 0) {
            if (event.getAction() == 0) {
                if (SDLActivity.isTextInputEvent(event)) {
                    SDLInputConnection.nativeCommitText(String.valueOf((char) event.getUnicodeChar()), 1);
                }
                SDLActivity.onNativeKeyDown(keyCode);
                return true;
            }
            if (event.getAction() == 1) {
                SDLActivity.onNativeKeyUp(keyCode);
                return true;
            }
        }
        if ((event.getSource() & 8194) != 0 && (keyCode == 4 || keyCode == 125)) {
            switch (event.getAction()) {
                case 0:
                case 1:
                    return true;
            }
        }
        return false;
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View v, MotionEvent event) {
        int mouseButton;
        int touchDevId = event.getDeviceId();
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        int i = -1;
        if (event.getSource() == 8194 && SDLActivity.mSeparateMouseAndTouch) {
            if (Build.VERSION.SDK_INT < 14) {
                mouseButton = 1;
            } else {
                try {
                    mouseButton = ((Integer) event.getClass().getMethod("getButtonState", new Class[0]).invoke(event, new Object[0])).intValue();
                } catch (Exception e) {
                    mouseButton = 1;
                }
            }
            SDLActivity.onNativeMouse(mouseButton, action, event.getX(0), event.getY(0));
            return true;
        }
        switch (action) {
            case 0:
            case 1:
                i = 0;
                break;
            case 2:
                for (int i2 = 0; i2 < pointerCount; i2++) {
                    int pointerFingerId = event.getPointerId(i2);
                    float x = event.getX(i2) / mWidth;
                    float y = event.getY(i2) / mHeight;
                    float p = event.getPressure(i2);
                    if (p > 1.0f) {
                        p = 1.0f;
                    }
                    SDLActivity.onNativeTouch(touchDevId, pointerFingerId, action, x, y, p);
                }
                return true;
            case 3:
                for (int i3 = 0; i3 < pointerCount; i3++) {
                    int pointerFingerId2 = event.getPointerId(i3);
                    float x2 = event.getX(i3) / mWidth;
                    float y2 = event.getY(i3) / mHeight;
                    float p2 = event.getPressure(i3);
                    if (p2 > 1.0f) {
                        p2 = 1.0f;
                    }
                    SDLActivity.onNativeTouch(touchDevId, pointerFingerId2, 1, x2, y2, p2);
                }
                return true;
            case 4:
            default:
                return true;
            case 5:
            case 6:
                break;
        }
        if (i == -1) {
            i = event.getActionIndex();
        }
        int pointerFingerId3 = event.getPointerId(i);
        float x3 = event.getX(i) / mWidth;
        float y3 = event.getY(i) / mHeight;
        float p3 = event.getPressure(i);
        if (p3 > 1.0f) {
            p3 = 1.0f;
        }
        SDLActivity.onNativeTouch(touchDevId, pointerFingerId3, action, x3, y3, p3);
        return true;
    }

    public void enableSensor(int sensortype, boolean enabled) {
        if (enabled) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(sensortype), 1, (Handler) null);
        } else {
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(sensortype));
        }
    }

    @Override // android.hardware.SensorEventListener
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(SensorEvent event) {
        float x;
        float y;
        if (event.sensor.getType() == 1) {
            switch (mDisplay.getRotation()) {
                case 1:
                    x = -event.values[1];
                    y = event.values[0];
                    break;
                case 2:
                    x = -event.values[1];
                    y = -event.values[0];
                    break;
                case 3:
                    x = event.values[1];
                    y = -event.values[0];
                    break;
                default:
                    x = event.values[0];
                    y = event.values[1];
                    break;
            }
            SDLActivity.onNativeAccel((-x) / 9.80665f, y / 9.80665f, event.values[2] / 9.80665f);
        }
    }
}
