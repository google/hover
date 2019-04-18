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

    private final HoverFrameLayout.OnPositionChangeListener mOnPositionChangeListener = new HoverFrameLayout.OnPositionChangeListener() {
        @Override
        public void onPositionChange(@NonNull View view) {
            moveTouchViewTo(mTouchViewMap.get(view.getTag()).mTouchView, new PointF(view.getX(), view.getY()));
        }
    };

    public abstract View createTouchView(@NonNull Rect rect);

    public abstract void destroyTouchView(@NonNull View touchView);

    public abstract void moveTouchViewTo(@NonNull View touchView, @NonNull PointF position);

    public void activate(final List<Pair<? extends HoverFrameLayout, ? extends TouchListener>> viewList) {
        if (!mIsActivated) {
            Log.d(TAG, "Activating.");
            mIsActivated = true;

            clearTouchViewMap();
            for (int i = 0; i < viewList.size(); i++) {
                final Pair<? extends HoverFrameLayout, ? extends TouchListener> viewItem = viewList.get(i);
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

    private TouchViewItem createTouchViewItem(final HoverFrameLayout originalView, final TouchListener listener, final String tag) {
        return new TouchViewItem<>(originalView, createTouchViewFrom(originalView), listener, tag);
    }

    protected TouchDetector createTouchDetector(final View originalView, final TouchListener touchListener) {
        return new TouchDetector<>(originalView, touchListener);
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

    private Rect getRectFrom(final View view) {
        final Rect rect = new Rect();
        view.getDrawingRect(rect);
        return rect;

    }

    private View createTouchViewFrom(final View originalView) {
        final View touchView = createTouchView(getRectFrom(originalView));
        moveTouchViewTo(touchView, new PointF(originalView.getX(), originalView.getY()));
        return touchView;
    }

    public interface TouchListener {
        void onTap(View view);

        void onTouchDown(View view);

        void onTouchUp(View view);
    }

    protected class TouchDetector<T extends TouchListener> implements View.OnTouchListener {

        @NonNull
        protected final View mOriginalView;
        @NonNull
        protected final T mEventListener;

        TouchDetector(@NonNull final View originalView, @NonNull final T touchListener) {
            this.mOriginalView = originalView;
            this.mEventListener = touchListener;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    mEventListener.onTouchDown(mOriginalView);
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    mEventListener.onTouchUp(mOriginalView);
                    mEventListener.onTap(mOriginalView);
                    return true;
                default:
                    return false;
            }
        }
    }

    protected class TouchViewItem<T extends TouchListener> {
        final HoverFrameLayout mOriginalView;
        final View mTouchView;
        final T mTouchListener;

        TouchViewItem(final HoverFrameLayout originalView, final View touchView, final T touchListener, final String tag) {
            this.mOriginalView = originalView;
            this.mTouchView = touchView;
            this.mTouchListener = touchListener;

            mOriginalView.setTag(tag);
            mTouchView.setTag(tag);

            mTouchView.setOnTouchListener(createTouchDetector(mOriginalView, mTouchListener));
            mOriginalView.addOnPositionChangeListener(mOnPositionChangeListener);
        }

        void destroy() {
            mTouchView.setOnTouchListener(null);
            mOriginalView.removeOnPositionChangeListener(mOnPositionChangeListener);
            destroyTouchView(mTouchView);
        }
    }
}
