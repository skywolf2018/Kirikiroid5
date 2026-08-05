package org.tvp.kirikiri2;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

/* JADX INFO: compiled from: KR2Activity.java */
/* JADX INFO: loaded from: classes.dex */
class SDLInputConnection extends BaseInputConnection {
    public SDLInputConnection(View targetView, boolean fullEditor) {
        super(targetView, fullEditor);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean sendKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getAction() == 0) {
            if (event.isPrintingKey()) {
                commitText(String.valueOf((char) event.getUnicodeChar()), 1);
                KR2Activity.nativeCharInput(keyCode);
                return true;
            }
            if (keyCode != 67) {
                return true;
            }
            KR2Activity.nativeKeyAction(keyCode, true);
            return true;
        }
        if (event.getAction() == 1) {
            if (keyCode != 67) {
                return true;
            }
            KR2Activity.nativeKeyAction(keyCode, false);
            return true;
        }
        return super.sendKeyEvent(event);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitText(CharSequence text, int newCursorPosition) {
        KR2Activity.nativeCommitText(text.toString(), newCursorPosition);
        return super.commitText(text, newCursorPosition);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        return super.setComposingText(text, newCursorPosition);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if (beforeLength == 1 && afterLength == 0) {
            return super.sendKeyEvent(new KeyEvent(0, 67)) && super.sendKeyEvent(new KeyEvent(1, 67));
        }
        return super.deleteSurroundingText(beforeLength, afterLength);
    }
}
