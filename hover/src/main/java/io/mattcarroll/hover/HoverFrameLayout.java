package io.mattcarroll.hover;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

class HoverFrameLayout extends FrameLayout {

    protected final Set<OnPositionChangeListener> mOnPositionChangeListeners = new CopyOnWriteArraySet<>();
    private final OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            notifyListenersOfPositionChange(v);
        }
    };

    public HoverFrameLayout(@NonNull Context context) {
        super(context);
    }

    public HoverFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HoverFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HoverFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addOnPositionChangeListener(@Nullable OnPositionChangeListener listener) {
        mOnPositionChangeListeners.add(listener);
    }

    public void removeOnPositionChangeListener(@NonNull OnPositionChangeListener listener) {
        mOnPositionChangeListeners.remove(listener);
    }

    protected void notifyListenersOfPositionChange(final View view) {
        for (OnPositionChangeListener listener : mOnPositionChangeListeners) {
            listener.onPositionChange(view);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addOnLayoutChangeListener(mOnLayoutChangeListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnLayoutChangeListener(mOnLayoutChangeListener);
    }

    interface OnPositionChangeListener {
        void onPositionChange(@NonNull View view);
    }
}

