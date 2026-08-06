package org.libsdl.app;

import android.view.InputDevice;

/* JADX INFO: compiled from: SDLControllerManager.java */
/* JADX INFO: loaded from: classes.dex */
class SDLJoystickHandler_API16 extends SDLJoystickHandler_API12 {
    SDLJoystickHandler_API16() {
    }

    @Override // org.libsdl.app.SDLJoystickHandler_API12
    public String getJoystickDescriptor(InputDevice joystickDevice) {
        String desc = joystickDevice.getDescriptor();
        return (desc == null || desc.isEmpty()) ? super.getJoystickDescriptor(joystickDevice) : desc;
    }
}
