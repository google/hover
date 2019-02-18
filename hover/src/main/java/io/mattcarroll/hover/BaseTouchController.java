package io.mattcarroll.hover;

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public abstract class BaseTouchController {
    private static final String TAG = "BaseTouchController";

    protected View mTouchView;
    protected TouchListener mTouchListener;
    protected boolean mIsActivated;
    private boolean mIsDebugMode;

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

    public abstract View createTouchView(@NonNull Rect rect);

    public abstract void destroyTouchView(@NonNull View touchView);

    public abstract void moveTouchViewTo(@NonNull View touchView, @NonNull PointF position);

    public void activate(@NonNull TouchListener touchListener, @NonNull Rect rect) {
        if (!mIsActivated) {
            Log.d(TAG, "Activating.");
            mIsActivated = true;
            mTouchListener = touchListener;
            mTouchView = createTouchView(rect);
            moveTouchViewTo(mTouchView, new PointF(rect.left, rect.top));
            mTouchView.setOnTouchListener(mDragTouchListener);

            updateTouchControlViewAppearance();
        }
    }

    public void deactivate() {
        if (mIsActivated) {
            Log.d(TAG, "Deactivating.");
            mTouchView.setOnTouchListener(null);
            destroyTouchView(mTouchView);
            mIsActivated = false;
            mTouchView = null;
        }
    }

    public void enableDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
        updateTouchControlViewAppearance();
    }

    private void updateTouchControlViewAppearance() {
        if (null != mTouchView) {
            if (mIsDebugMode) {
                mTouchView.setBackgroundColor(0x44FF0000);
            } else {
                mTouchView.setBackgroundColor(0x00000000);
            }
        }
    }

    public interface TouchListener {
        void onPress();

        void onTap();
    }
}
