package io.mattcarroll.hover;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseTouchController {
    private static final String TAG = "BaseTouchController";

    protected Map<String, TouchViewItem> mTouchViewMap = new HashMap<>();
    protected boolean mIsActivated;
    private boolean mIsDebugMode;

    private final View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            moveTouchViewTo(mTouchViewMap.get(view.getTag()).mTouchView, new PointF(view.getX(), view.getY()));
        }
    };

    public abstract View createTouchView(@NonNull Rect rect);

    public abstract void destroyTouchView(@NonNull View touchView);

    public abstract void moveTouchViewTo(@NonNull View touchView, @NonNull PointF position);

    public void activate(final List<Pair<? extends View, ? extends TouchListener>> viewList) {
        if (!mIsActivated) {
            Log.d(TAG, "Activating.");
            mIsActivated = true;

            clearTouchViewMap();
            for (int i = 0; i < viewList.size(); i++) {
                final Pair<? extends View, ? extends TouchListener> viewItem = viewList.get(i);
                final String tag = "view" + i;
                final TouchViewItem touchViewItem = createTouchViewItem(viewItem.first, viewItem.second, tag);
                mTouchViewMap.put(tag, touchViewItem);
            }
            updateTouchControlViewAppearance();
        }
    }

    public void deactivate() {
        if (mIsActivated) {
            Log.d(TAG, "Deactivating.");
            clearTouchViewMap();
            mIsActivated = false;
        }
    }

    public void enableDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
        updateTouchControlViewAppearance();
    }

    private TouchViewItem createTouchViewItem(final View originalView, final TouchListener listener, final String tag) {
        originalView.setTag(tag);

        Rect rect = new Rect();
        originalView.getDrawingRect(rect);
        View touchView = createTouchView(rect);
        moveTouchViewTo(touchView, new PointF(originalView.getX(), originalView.getY()));
        touchView.setTag(tag);
        final TouchViewItem touchViewItem = new TouchViewItem<>(originalView, touchView, listener);
        setEventListener(touchViewItem);
        originalView.addOnLayoutChangeListener(mOnLayoutChangeListener);
        return touchViewItem;
    }

    protected void setEventListener(final TouchViewItem touchViewItem) {
        touchViewItem.mTouchView.setOnTouchListener(new TouchDetector<>(touchViewItem.mOriginalView, touchViewItem.mTouchListener));
    }

    private void clearTouchViewMap() {
        for (final TouchViewItem touchViewItem : mTouchViewMap.values()) {
            touchViewItem.destroy();
        }
        mTouchViewMap.clear();
    }

    private void updateTouchControlViewAppearance() {
        for (final TouchViewItem touchViewItemItem : mTouchViewMap.values()) {
            final View touchView = touchViewItemItem.mTouchView;
            if (null != touchView) {
                if (mIsDebugMode) {
                    touchView.setBackgroundColor(0x44FF0000);
                } else {
                    touchView.setBackgroundColor(0x00000000);
                }
            }
        }
    }

    public interface TouchListener {
        void onPress(View view);

        void onTap(View view);
    }

    protected class TouchDetector<T extends TouchListener> implements View.OnTouchListener {

        protected final View mOriginalView;
        protected final T mEventListener;

        TouchDetector(final View originalView, final T touchListener) {
            this.mOriginalView = originalView;
            this.mEventListener = touchListener;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    mEventListener.onPress(mOriginalView);
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    mEventListener.onTap(mOriginalView);
                    return true;
                default:
                    return false;
            }
        }
    }

    protected class TouchViewItem<T extends TouchListener> {
        final View mOriginalView;
        final View mTouchView;
        final T mTouchListener;

        TouchViewItem(final View originalView, final View touchView, final T touchListener) {
            this.mOriginalView = originalView;
            this.mTouchView = touchView;
            this.mTouchListener = touchListener;
        }

        void destroy() {
            mOriginalView.setOnTouchListener(null);
            mOriginalView.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            destroyTouchView(mTouchView);
        }
    }
}
