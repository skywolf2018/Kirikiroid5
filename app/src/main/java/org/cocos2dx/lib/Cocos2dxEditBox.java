package org.cocos2dx.lib;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.FrameLayout;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxEditBox extends EditText {
    private final int kEditBoxInputFlagInitialCapsAllCharacters;
    private final int kEditBoxInputFlagInitialCapsSentence;
    private final int kEditBoxInputFlagInitialCapsWord;
    private final int kEditBoxInputFlagPassword;
    private final int kEditBoxInputFlagSensitive;
    private final int kEditBoxInputModeAny;
    private final int kEditBoxInputModeDecimal;
    private final int kEditBoxInputModeEmailAddr;
    private final int kEditBoxInputModeNumeric;
    private final int kEditBoxInputModePhoneNumber;
    private final int kEditBoxInputModeSingleLine;
    private final int kEditBoxInputModeUrl;
    private final int kKeyboardReturnTypeDefault;
    private final int kKeyboardReturnTypeDone;
    private final int kKeyboardReturnTypeGo;
    private final int kKeyboardReturnTypeSearch;
    private final int kKeyboardReturnTypeSend;
    private int mInputFlagConstraints;
    private int mInputModeConstraints;
    private int mMaxLength;
    private float mScaleX;

    public Cocos2dxEditBox(Context context) {
        super(context);
        this.kEditBoxInputModeAny = 0;
        this.kEditBoxInputModeEmailAddr = 1;
        this.kEditBoxInputModeNumeric = 2;
        this.kEditBoxInputModePhoneNumber = 3;
        this.kEditBoxInputModeUrl = 4;
        this.kEditBoxInputModeDecimal = 5;
        this.kEditBoxInputModeSingleLine = 6;
        this.kEditBoxInputFlagPassword = 0;
        this.kEditBoxInputFlagSensitive = 1;
        this.kEditBoxInputFlagInitialCapsWord = 2;
        this.kEditBoxInputFlagInitialCapsSentence = 3;
        this.kEditBoxInputFlagInitialCapsAllCharacters = 4;
        this.kKeyboardReturnTypeDefault = 0;
        this.kKeyboardReturnTypeDone = 1;
        this.kKeyboardReturnTypeSend = 2;
        this.kKeyboardReturnTypeSearch = 3;
        this.kKeyboardReturnTypeGo = 4;
    }

    public void setEditBoxViewRect(int left, int top, int maxWidth, int maxHeight) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.leftMargin = left;
        layoutParams.topMargin = top;
        layoutParams.width = maxWidth;
        layoutParams.height = maxHeight;
        layoutParams.gravity = 51;
        setLayoutParams(layoutParams);
    }

    public float getOpenGLViewScaleX() {
        return this.mScaleX;
    }

    public void setOpenGLViewScaleX(float mScaleX) {
        this.mScaleX = mScaleX;
    }

    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(this.mMaxLength)});
    }

    public void setMultilineEnabled(boolean flag) {
        this.mInputModeConstraints |= 131072;
    }

    public void setReturnType(int returnType) {
        switch (returnType) {
            case 0:
                setImeOptions(268435457);
                break;
            case 1:
                setImeOptions(268435462);
                break;
            case 2:
                setImeOptions(268435460);
                break;
            case 3:
                setImeOptions(268435459);
                break;
            case 4:
                setImeOptions(268435458);
                break;
            default:
                setImeOptions(268435457);
                break;
        }
    }

    public void setInputMode(int inputMode) {
        switch (inputMode) {
            case 0:
                this.mInputModeConstraints = 131073;
                break;
            case 1:
                this.mInputModeConstraints = 33;
                break;
            case 2:
                this.mInputModeConstraints = InputDeviceCompat.SOURCE_TOUCHSCREEN;
                break;
            case 3:
                this.mInputModeConstraints = 3;
                break;
            case 4:
                this.mInputModeConstraints = 17;
                break;
            case 5:
                this.mInputModeConstraints = 12290;
                break;
            case 6:
                this.mInputModeConstraints = 1;
                break;
        }
        setInputType(this.mInputModeConstraints | this.mInputFlagConstraints);
    }

    @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int pKeyCode, KeyEvent pKeyEvent) {
        switch (pKeyCode) {
            case 4:
                Cocos2dxActivity activity = (Cocos2dxActivity) getContext();
                activity.getGLSurfaceView().requestFocus();
                return true;
            default:
                return super.onKeyDown(pKeyCode, pKeyEvent);
        }
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        return super.onKeyPreIme(keyCode, event);
    }

    public void setInputFlag(int inputFlag) {
        switch (inputFlag) {
            case 0:
                this.mInputFlagConstraints = 129;
                setTypeface(Typeface.DEFAULT);
                setTransformationMethod(new PasswordTransformationMethod());
                break;
            case 1:
                this.mInputFlagConstraints = AccessibilityEventCompat.TYPE_GESTURE_DETECTION_END;
                break;
            case 2:
                this.mInputFlagConstraints = 8192;
                break;
            case 3:
                this.mInputFlagConstraints = 16384;
                break;
            case 4:
                this.mInputFlagConstraints = 4096;
                break;
        }
        setInputType(this.mInputFlagConstraints | this.mInputModeConstraints);
    }
}
