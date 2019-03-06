package io.mattcarroll.hover;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseTouchController {
    private static final String TAG = "BaseTouchController";

    protected Map<String, View> mTouchViewMap = new HashMap<>();
    protected TouchListener mTouchListener;
    protected boolean mIsActivated;
    private boolean mIsDebugMode;
    private List<View> mViewList;

    private View.OnTouchListener mDragTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    mTouchListener.onPress();
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    mTouchListener.onTap();
                    return true;
                default:
                    return false;
            }
        }
    };

    private final View.OnLayoutChangeListener mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            moveTouchViewTo(mTouchViewMap.get(view.getTag()), new PointF(view.getX(), view.getY()));
        }
    };

    public abstract View createTouchView(@NonNull Rect rect);

    public abstract void destroyTouchView(@NonNull View touchView);

    public abstract void moveTouchViewTo(@NonNull View touchView, @NonNull PointF position);

    public void activate(@NonNull TouchListener touchListener, @NonNull List<View> viewList) {
        if (!mIsActivated) {
            Log.d(TAG, "Activating.");
            mIsActivated = true;
            mTouchListener = touchListener;
            mViewList = viewList;
            mTouchViewMap.clear();
            for (int i = 0; i < mViewList.size(); i++) {
                View view = mViewList.get(i);
                String tag = "view" + i;
                view.setTag(tag);
                Rect rect = new Rect();
                view.getDrawingRect(rect);
                View touchView = createTouchView(rect);
                moveTouchViewTo(touchView, new PointF(view.getX(), view.getY()));
                touchView.setOnTouchListener(mDragTouchListener);
                touchView.setTag(tag);
                mTouchViewMap.put(tag, touchView);
                view.addOnLayoutChangeListener(mOnLayoutChangeListener);
            }
            updateTouchControlViewAppearance();
        }
    }

    public void deactivate() {
        if (mIsActivated) {
            Log.d(TAG, "Deactivating.");
            for (View view : mViewList) {
                view.setOnTouchListener(null);
                view.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            }

            for (View touchView : mTouchViewMap.values()) {
                destroyTouchView(touchView);
            }

            mIsActivated = false;
            mTouchViewMap.clear();
            mViewList = null;
            mTouchListener = null;
        }
    }

    public void enableDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
        updateTouchControlViewAppearance();
    }

    private void updateTouchControlViewAppearance() {
        for (View touchView : mTouchViewMap.values()) {
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
        void onPress();

        void onTap();
    }
}
