package org.libsdl.app;

import android.os.Build;
import android.support.v4.view.InputDeviceCompat;
import android.view.InputDevice;
import android.view.MotionEvent;

/* JADX INFO: loaded from: classes.dex */
public class SDLControllerManager {
    private static final String TAG = "SDLControllerManager";
    protected static SDLHapticHandler mHapticHandler;
    protected static SDLJoystickHandler mJoystickHandler;

    public static native int nativeAddHaptic(int i, String str);

    public static native int nativeAddJoystick(int i, String str, String str2, int i2, int i3, int i4, int i5, int i6);

    public static native int nativeRemoveHaptic(int i);

    public static native int nativeRemoveJoystick(int i);

    public static native int nativeSetupJNI();

    public static native void onNativeHat(int i, int i2, int i3, int i4);

    public static native void onNativeJoy(int i, int i2, float f);

    public static native int onNativePadDown(int i, int i2);

    public static native int onNativePadUp(int i, int i2);

    public static void initialize() {
        mJoystickHandler = null;
        mHapticHandler = null;
        setup();
    }

    public static void setup() {
        if (Build.VERSION.SDK_INT >= 16) {
            mJoystickHandler = new SDLJoystickHandler_API16();
        } else if (Build.VERSION.SDK_INT >= 12) {
            mJoystickHandler = new SDLJoystickHandler_API12();
        } else {
            mJoystickHandler = new SDLJoystickHandler();
        }
        mHapticHandler = new SDLHapticHandler();
    }

    public static boolean handleJoystickMotionEvent(MotionEvent event) {
        return mJoystickHandler.handleMotionEvent(event);
    }

    public static void pollInputDevices() {
        mJoystickHandler.pollInputDevices();
    }

    public static void pollHapticDevices() {
        mHapticHandler.pollHapticDevices();
    }

    public static void hapticRun(int device_id, int length) {
        mHapticHandler.run(device_id, length);
    }

    public static boolean isDeviceSDLJoystick(int deviceId) {
        InputDevice device = InputDevice.getDevice(deviceId);
        if (device == null || deviceId < 0) {
            return false;
        }
        int sources = device.getSources();
        return (sources & 16) == 16 || (sources & InputDeviceCompat.SOURCE_DPAD) == 513 || (sources & InputDeviceCompat.SOURCE_GAMEPAD) == 1025;
    }
}
