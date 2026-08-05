package org.libsdl.app;

import android.util.Log;

/* JADX INFO: compiled from: SDLActivity.java */
/* JADX INFO: loaded from: classes.dex */
class SDLMain implements Runnable {
    SDLMain() {
    }

    @Override // java.lang.Runnable
    public void run() {
        String library = SDLActivity.mSingleton.getMainSharedObject();
        String function = SDLActivity.mSingleton.getMainFunction();
        String[] arguments = SDLActivity.mSingleton.getArguments();
        Log.v("SDL", "Running main function " + function + " from library " + library);
        SDLActivity.nativeRunMain(library, function, arguments);
        Log.v("SDL", "Finished main function");
        if (!SDLActivity.mExitCalledFromJava) {
            SDLActivity.handleNativeExit();
        }
    }
}
