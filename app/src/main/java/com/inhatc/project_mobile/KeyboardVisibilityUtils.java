package com.inhatc.project_mobile;

import android.graphics.Rect;
import android.view.ViewTreeObserver;
import android.view.Window;

public class KeyboardVisibilityUtils {
    private static final int MIN_KEYBOARD_HEIGHT_PX = 150;

    private Window window;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private Rect windowVisibleDisplayFrame;
    private int lastVisibleDecorViewHeight;
    private OnKeyboardVisibilityChangeListener onKeyboardVisibilityChangeListener;

    public KeyboardVisibilityUtils(Window window) {
        this(window, null);
    }

    public KeyboardVisibilityUtils(Window window, OnKeyboardVisibilityChangeListener listener) {
        this.window = window;
        this.onKeyboardVisibilityChangeListener = listener;
        this.windowVisibleDisplayFrame = new Rect();
        this.lastVisibleDecorViewHeight = 0;

        this.onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                window.getDecorView().getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

                if (lastVisibleDecorViewHeight != 0) {
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                        int currentKeyboardHeight = window.getDecorView().getHeight() - windowVisibleDisplayFrame.bottom;
                        if (onKeyboardVisibilityChangeListener != null) {
                            onKeyboardVisibilityChangeListener.onShowKeyboard(currentKeyboardHeight);
                        }
                    } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                        if (onKeyboardVisibilityChangeListener != null) {
                            onKeyboardVisibilityChangeListener.onHideKeyboard();
                        }
                    }
                }
                lastVisibleDecorViewHeight = visibleDecorViewHeight;
            }
        };

        window.getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void detachKeyboardListeners() {
        window.getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public interface OnKeyboardVisibilityChangeListener {
        void onShowKeyboard(int keyboardHeight);
        void onHideKeyboard();
    }
}
