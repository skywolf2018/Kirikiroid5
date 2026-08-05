package org.cocos2dx.lib;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/* JADX INFO: loaded from: classes.dex */
public class Cocos2dxTextInputWraper implements TextWatcher, TextView.OnEditorActionListener {
    private static final String TAG = Cocos2dxTextInputWraper.class.getSimpleName();
    private final Cocos2dxGLSurfaceView mCocos2dxGLSurfaceView;
    private String mOriginText;
    private String mText;

    public Cocos2dxTextInputWraper(Cocos2dxGLSurfaceView pCocos2dxGLSurfaceView) {
        this.mCocos2dxGLSurfaceView = pCocos2dxGLSurfaceView;
    }

    private boolean isFullScreenEdit() {
        TextView textField = this.mCocos2dxGLSurfaceView.getCocos2dxEditText();
        InputMethodManager imm = (InputMethodManager) textField.getContext().getSystemService("input_method");
        return imm.isFullscreenMode();
    }

    public void setOriginText(String pOriginText) {
        this.mOriginText = pOriginText;
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable s) {
        if (!isFullScreenEdit()) {
            int old_i = 0;
            int new_i = 0;
            while (old_i < this.mText.length() && new_i < s.length() && this.mText.charAt(old_i) == s.charAt(new_i)) {
                old_i++;
                new_i++;
            }
            while (old_i < this.mText.length()) {
                this.mCocos2dxGLSurfaceView.deleteBackward();
                old_i++;
            }
            int nModified = s.length() - new_i;
            if (nModified > 0) {
                String insertText = s.subSequence(new_i, s.length()).toString();
                this.mCocos2dxGLSurfaceView.insertText(insertText);
            }
            this.mText = s.toString();
        }
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence pCharSequence, int start, int count, int after) {
        this.mText = pCharSequence.toString();
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence pCharSequence, int start, int before, int count) {
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView pTextView, int pActionID, KeyEvent pKeyEvent) {
        if (this.mCocos2dxGLSurfaceView.getCocos2dxEditText() == pTextView && isFullScreenEdit()) {
            if (this.mOriginText != null) {
                for (int i = this.mOriginText.length(); i > 0; i--) {
                    this.mCocos2dxGLSurfaceView.deleteBackward();
                }
            }
            String text = pTextView.getText().toString();
            if (text != null) {
                if (text.compareTo("") == 0) {
                    text = "\n";
                }
                if ('\n' != text.charAt(text.length() - 1)) {
                    text = String.valueOf(text) + '\n';
                }
            }
            String insertText = text;
            this.mCocos2dxGLSurfaceView.insertText(insertText);
        }
        if (pActionID == 6) {
            this.mCocos2dxGLSurfaceView.requestFocus();
            return false;
        }
        return false;
    }
}
