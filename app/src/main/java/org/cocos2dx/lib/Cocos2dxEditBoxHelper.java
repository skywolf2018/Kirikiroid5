package org.cocos2dx.lib;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxEditBoxHelper {
    private static Cocos2dxActivity mCocos2dxActivity;
    private static SparseArray<Cocos2dxEditBox> mEditBoxArray;
    private static ResizeLayout mFrameLayout;
    private static final String TAG = Cocos2dxEditBoxHelper.class.getSimpleName();
    private static int mViewTag = 0;

    private static native void editBoxEditingChanged(int i, String str);

    private static native void editBoxEditingDidBegin(int i);

    private static native void editBoxEditingDidEnd(int i, String str);

    public static void __editBoxEditingDidBegin(int index) {
        editBoxEditingDidBegin(index);
    }

    public static void __editBoxEditingChanged(int index, String text) {
        editBoxEditingChanged(index, text);
    }

    public static void __editBoxEditingDidEnd(int index, String text) {
        editBoxEditingDidEnd(index, text);
    }

    public Cocos2dxEditBoxHelper(ResizeLayout layout) {
        mFrameLayout = layout;
        mCocos2dxActivity = (Cocos2dxActivity) Cocos2dxActivity.getContext();
        mEditBoxArray = new SparseArray<>();
    }

    public static int convertToSP(float point) {
        Resources r = mCocos2dxActivity.getResources();
        int convertedValue = (int) TypedValue.applyDimension(2, point, r.getDisplayMetrics());
        return convertedValue;
    }

    public static int createEditBox(final int left, final int top, final int width, final int height, final float scaleX) {
        final int index = mViewTag;
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1
            @Override // java.lang.Runnable
            public void run() {
                final Cocos2dxEditBox editBox = new Cocos2dxEditBox(Cocos2dxEditBoxHelper.mCocos2dxActivity);
                editBox.setFocusable(true);
                editBox.setFocusableInTouchMode(true);
                editBox.setInputFlag(4);
                editBox.setInputMode(6);
                editBox.setReturnType(0);
                editBox.setHintTextColor(-7829368);
                editBox.setVisibility(4);
                editBox.setBackgroundColor(0);
                editBox.setTextColor(-1);
                editBox.setSingleLine();
                editBox.setOpenGLViewScaleX(scaleX);
                Resources r = Cocos2dxEditBoxHelper.mCocos2dxActivity.getResources();
                float density = r.getDisplayMetrics().density;
                int paddingBottom = Cocos2dxEditBoxHelper.convertToSP(((int) ((height * 0.33f) / density)) - ((scaleX * 5.0f) / density)) / 2;
                int paddingLeft = (int) ((scaleX * 5.0f) / density);
                editBox.setPadding(Cocos2dxEditBoxHelper.convertToSP(paddingLeft), paddingBottom, 0, paddingBottom);
                FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(-2, -2);
                lParams.leftMargin = left;
                lParams.topMargin = top;
                lParams.width = width;
                lParams.height = height;
                lParams.gravity = 51;
                Cocos2dxEditBoxHelper.mFrameLayout.addView(editBox, lParams);
                final int i = index;
                editBox.addTextChangedListener(new TextWatcher() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.1
                    @Override // android.text.TextWatcher
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override // android.text.TextWatcher
                    public void onTextChanged(final CharSequence s, int start, int before, int count) {
                        Cocos2dxActivity cocos2dxActivity = Cocos2dxEditBoxHelper.mCocos2dxActivity;
                        final int i2 = i;
                        cocos2dxActivity.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                Cocos2dxEditBoxHelper.__editBoxEditingChanged(i2, s.toString());
                            }
                        });
                    }

                    @Override // android.text.TextWatcher
                    public void afterTextChanged(Editable s) {
                    }
                });
                final int i2 = index;
                editBox.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.2
                    @Override // android.view.View.OnFocusChangeListener
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            Cocos2dxActivity cocos2dxActivity = Cocos2dxEditBoxHelper.mCocos2dxActivity;
                            final int i3 = i2;
                            cocos2dxActivity.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.2.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    Cocos2dxEditBoxHelper.__editBoxEditingDidBegin(i3);
                                }
                            });
                            editBox.setSelection(editBox.getText().length());
                            Cocos2dxEditBoxHelper.mFrameLayout.setEnableForceDoLayout(true);
                            Cocos2dxEditBoxHelper.mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(true);
                            Log.d(Cocos2dxEditBoxHelper.TAG, "edit box get focus");
                            return;
                        }
                        editBox.setVisibility(8);
                        Cocos2dxActivity cocos2dxActivity2 = Cocos2dxEditBoxHelper.mCocos2dxActivity;
                        final int i4 = i2;
                        final Cocos2dxEditBox cocos2dxEditBox = editBox;
                        cocos2dxActivity2.runOnGLThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.2.2
                            @Override // java.lang.Runnable
                            public void run() {
                                Cocos2dxEditBoxHelper.__editBoxEditingDidEnd(i4, cocos2dxEditBox.getText().toString());
                            }
                        });
                        Cocos2dxEditBoxHelper.mFrameLayout.setEnableForceDoLayout(false);
                        Log.d(Cocos2dxEditBoxHelper.TAG, "edit box lose focus");
                    }
                });
                final int i3 = index;
                editBox.setOnKeyListener(new View.OnKeyListener() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.3
                    @Override // android.view.View.OnKeyListener
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (event.getAction() != 0 || keyCode != 66 || (editBox.getInputType() & 131072) == 131072) {
                            return false;
                        }
                        Cocos2dxEditBoxHelper.closeKeyboard(i3);
                        Cocos2dxEditBoxHelper.mCocos2dxActivity.getGLSurfaceView().requestFocus();
                        return true;
                    }
                });
                final int i4 = index;
                editBox.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.1.4
                    @Override // android.widget.TextView.OnEditorActionListener
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == 6) {
                            Cocos2dxEditBoxHelper.closeKeyboard(i4);
                            Cocos2dxEditBoxHelper.mCocos2dxActivity.getGLSurfaceView().requestFocus();
                            return false;
                        }
                        return false;
                    }
                });
                Cocos2dxEditBoxHelper.mEditBoxArray.put(index, editBox);
            }
        });
        int i = mViewTag;
        mViewTag = i + 1;
        return i;
    }

    public static void removeEditBox(final int index) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.2
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    Cocos2dxEditBoxHelper.mEditBoxArray.remove(index);
                    Cocos2dxEditBoxHelper.mFrameLayout.removeView(editBox);
                    Log.e(Cocos2dxEditBoxHelper.TAG, "remove EditBox");
                }
            }
        });
    }

    public static void setFont(final int index, final String fontName, final float fontSize) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.3
            @Override // java.lang.Runnable
            public void run() {
                Typeface tf;
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    if (!fontName.isEmpty()) {
                        tf = Typeface.create(fontName, 0);
                    } else {
                        tf = Typeface.DEFAULT;
                    }
                    if (fontSize >= 0.0f) {
                        float density = Cocos2dxEditBoxHelper.mCocos2dxActivity.getResources().getDisplayMetrics().density;
                        editBox.setTextSize(2, fontSize / density);
                    }
                    editBox.setTypeface(tf);
                }
            }
        });
    }

    public static void setFontColor(final int index, final int red, final int green, final int blue, final int alpha) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.4
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setTextColor(Color.argb(alpha, red, green, blue));
                }
            }
        });
    }

    public static void setPlaceHolderText(final int index, final String text) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.5
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setHint(text);
                }
            }
        });
    }

    public static void setPlaceHolderTextColor(final int index, final int red, final int green, final int blue, final int alpha) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.6
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setHintTextColor(Color.argb(alpha, red, green, blue));
                }
            }
        });
    }

    public static void setMaxLength(final int index, final int maxLength) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.7
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setMaxLength(maxLength);
                }
            }
        });
    }

    public static void setVisible(final int index, final boolean visible) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.8
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setVisibility(visible ? 0 : 8);
                    if (!visible) {
                        Cocos2dxEditBoxHelper.mCocos2dxActivity.getGLSurfaceView().requestFocus();
                        Cocos2dxEditBoxHelper.closeKeyboard(index);
                    } else {
                        editBox.requestFocus();
                        Cocos2dxEditBoxHelper.openKeyboard(index);
                    }
                }
            }
        });
    }

    public static void setText(final int index, final String text) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.9
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setText(text);
                }
            }
        });
    }

    public static void setReturnType(final int index, final int returnType) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.10
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setReturnType(returnType);
                }
            }
        });
    }

    public static void setInputMode(final int index, final int inputMode) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.11
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setInputMode(inputMode);
                }
            }
        });
    }

    public static void setInputFlag(final int index, final int inputFlag) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.12
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setInputFlag(inputFlag);
                }
            }
        });
    }

    public static void setEditBoxViewRect(final int index, final int left, final int top, final int maxWidth, final int maxHeight) {
        mCocos2dxActivity.runOnUiThread(new Runnable() { // from class: org.cocos2dx.lib.Cocos2dxEditBoxHelper.13
            @Override // java.lang.Runnable
            public void run() {
                Cocos2dxEditBox editBox = (Cocos2dxEditBox) Cocos2dxEditBoxHelper.mEditBoxArray.get(index);
                if (editBox != null) {
                    editBox.setEditBoxViewRect(left, top, maxWidth, maxHeight);
                }
            }
        });
    }

    public static void openKeyboard(int index) {
        InputMethodManager imm = (InputMethodManager) Cocos2dxActivity.getContext().getSystemService("input_method");
        Cocos2dxEditBox editBox = mEditBoxArray.get(index);
        if (editBox != null) {
            imm.showSoftInput(editBox, 0);
            mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(true);
        }
    }

    public static void closeKeyboard(int index) {
        InputMethodManager imm = (InputMethodManager) Cocos2dxActivity.getContext().getSystemService("input_method");
        Cocos2dxEditBox editBox = mEditBoxArray.get(index);
        if (editBox != null) {
            imm.hideSoftInputFromWindow(editBox.getWindowToken(), 0);
            mCocos2dxActivity.getGLSurfaceView().setSoftKeyboardShown(false);
        }
    }
}
