package io.mattcarroll.hover;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

class HoverFrameLayout extends FrameLayout {

    private final Set<OnPositionChangeListener> mOnPositionChangeListeners = new CopyOnWriteArraySet<>();

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

    protected void notifyListenersOfPositionChange(View view) {
        for (OnPositionChangeListener listener : mOnPositionChangeListeners) {
            listener.onPositionChange(view);
        }
    }

    protected void notifyListenersOfDockChange(Dock dock) {
        for (OnPositionChangeListener listener : mOnPositionChangeListeners) {
            listener.onDockChange(dock);
        }
    }

    interface OnPositionChangeListener {
        void onPositionChange(@NonNull View view);

        void onDockChange(@NonNull Dock dock);
    }
}
