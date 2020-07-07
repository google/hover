package io.mattcarroll.hover.base;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.FloatValueHolder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import io.mattcarroll.hover.Dragger;

public abstract class DraggerImpl implements Dragger {

    private static final String TAG = "InWindowDragger";

    private final int mTouchAreaDiameter;
    private final float mTapTouchSlop;
    private Dragger.DragListener mDragListener;
    private boolean mIsActivated;
    private boolean mIsDragging;
    private boolean mIsDebugMode;
    private boolean mIsDetachOriginal;

    private PointF mOriginalViewPosition = new PointF();
    private PointF mCurrentViewPosition = new PointF();
    private PointF mOriginalTouchPosition = new PointF();

    private float mScreenWidth;
    private float mScreenHeight;
    private float mScaleVelocity = getDefaultScaleVelocity();
    private float mFriction = getDefaultFriction();

    private VelocityTracker mVelocityTracker;
    private float mXVelocity = 0f;
    private float mYVelocity = 0f;
    private FlingAnimation mXFling;
    private FlingAnimation mYFling;
    private float mX;
    private float mY;
    private DynamicAnimation.OnAnimationUpdateListener mXAnimationUpdate = new DynamicAnimation.OnAnimationUpdateListener() {
        @Override
        public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
            onUpdatePosition(value, mY);
        }
    };
    private DynamicAnimation.OnAnimationUpdateListener mYAnimationUpdate = new DynamicAnimation.OnAnimationUpdateListener() {
        @Override
        public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
            onUpdatePosition(mX, value);
        }
    };


    private View.OnTouchListener mDragTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onActionDown(motionEvent);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    onActionMove(motionEvent);
                    return true;
                case MotionEvent.ACTION_UP:
                    onActionUp(motionEvent);
                    return true;
                default:
                    return false;
            }
        }
    };

    public DraggerImpl(int touchAreaDiameter,
                       float tapTouchSlop) {
        mTouchAreaDiameter = touchAreaDiameter;
        mTapTouchSlop = tapTouchSlop;
    }

    public void setScreenSize(float width, float height) {
        mScreenWidth = width;
        mScreenHeight = height;
    }

    public void setScreenSize(Point size) {
        mScreenWidth = size.x;
        mScreenHeight = size.y;
    }

    public void activate(@NonNull DragListener dragListener, @NonNull Point dragStartCenterPosition) {
        if (!mIsActivated) {
            Log.d(TAG, "Activating.");
            createTouchControlView(dragStartCenterPosition);
            mDragListener = dragListener;
            getDragView().setOnTouchListener(mDragTouchListener);
            mIsActivated = true;
        }
    }

    public void deactivate() {
        if (mIsActivated) {
            Log.d(TAG, "Deactivating.");
            mDragListener = null;
            getDragView().setOnTouchListener(null);
            destroyTouchControlView();
            mIsActivated = false;
        }
    }

    public void enableDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
        updateTouchControlViewAppearance();
    }

    @Override
    public void brakeIfFling() {
        if (mXFling == null || mYFling == null) return;
        stopAllAnimations();
        mScaleVelocity = 3000f / Math.max(Math.abs(mXVelocity), Math.abs(mYVelocity));
        startXAnimation();
        startYAnimation();
        mXFling.setFriction(25f);
        mYFling.setFriction(25f);
    }

    protected View.OnTouchListener getDragTouchListener() {
        return mDragTouchListener;
    }

    protected int getTouchAreaDiameter() {
        return mTouchAreaDiameter;
    }

    protected void updateTouchControlViewAppearance() {
        View dragView = getDragView();
        if (null != dragView) {
            if (mIsDebugMode) {
                dragView.setBackgroundColor(0x44FF0000);
            } else {
                dragView.setBackgroundColor(0x00000000);
            }
        }
    }

    protected PointF convertCornerToCenter(@NonNull PointF cornerPosition) {
        View dragView = getDragView();
        return new PointF(
                cornerPosition.x + (dragView != null ? dragView.getWidth() / 2f : 0),
                cornerPosition.y + (dragView != null ? dragView.getHeight() / 2f : 0)
        );
    }

    protected PointF convertCenterToCorner(@NonNull PointF centerPosition) {
        View dragView = getDragView();
        return new PointF(
                centerPosition.x - (dragView != null ? dragView.getWidth() / 2f : 0),
                centerPosition.y - (dragView != null ? dragView.getHeight() / 2f : 0)
        );
    }

    protected abstract void createTouchControlView(@NonNull final Point dragStartCenterPosition);

    protected abstract void destroyTouchControlView();

    protected abstract View getDragView();

    protected abstract PointF getDragViewCenterPosition();

    protected abstract void moveDragViewTo(PointF position);

    protected float getDefaultScaleVelocity() {
        return 3.8f;
    }

    protected float getDefaultFriction() {
        return 2f;
    }

    protected FlingAnimation createAnimation(
            float startValue,
            float startVelocity,
            float minValue,
            float maxValue
    ) {
        return new FlingAnimation(new FloatValueHolder(startValue))
                .setStartVelocity(mScaleVelocity * startVelocity)
                .setMinValue(minValue)
                .setMaxValue(maxValue)
                .setFriction(mFriction);
    }

    protected float getXVelocity() {
        return mXVelocity;
    }

    protected float getYVelocity() {
        return mYVelocity;
    }

    private boolean isTouchWithinSlopOfOriginalTouch(float dx, float dy) {
        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        Log.d(TAG, "Drag distance " + distance + " vs slop allowance " + mTapTouchSlop);
        return distance < mTapTouchSlop;
    }

    private void onActionDown(MotionEvent motionEvent) {
        mIsDragging = false;
        mIsDetachOriginal = false;

        // Velocity tracker
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
        computeVelocityTracker(motionEvent);

        mOriginalViewPosition = getDragViewCenterPosition();
        mCurrentViewPosition = new PointF(mOriginalViewPosition.x, mOriginalViewPosition.y);
        mOriginalTouchPosition.set(motionEvent.getRawX(), motionEvent.getRawY());

        mDragListener.onPress(mCurrentViewPosition.x, mCurrentViewPosition.y);
    }

    private void onActionMove(MotionEvent motionEvent) {
        float dragDeltaX = motionEvent.getRawX() - mOriginalTouchPosition.x;
        float dragDeltaY = motionEvent.getRawY() - mOriginalTouchPosition.y;

        mCurrentViewPosition = new PointF(
                mOriginalViewPosition.x + dragDeltaX,
                mOriginalViewPosition.y + dragDeltaY
        );

        computeVelocityTracker(motionEvent);

        if (!mIsDragging) {
            mIsDragging = true;
            mDragListener.onDragStart(mCurrentViewPosition.x, mCurrentViewPosition.y);
        } else {
            moveDragViewTo(mCurrentViewPosition);
            mDragListener.onDragTo(mCurrentViewPosition.x, mCurrentViewPosition.y, isTouchWithinSlopOfOriginalTouch(dragDeltaX, dragDeltaY));

            if (!isTouchWithinSlopOfOriginalTouch(dragDeltaX, dragDeltaY)) {
                mIsDetachOriginal = true;
            }
        }
    }

    private void onActionUp(MotionEvent motionEvent) {
        float dragDeltaX = motionEvent.getRawX() - mOriginalTouchPosition.x;
        float dragDeltaY = motionEvent.getRawY() - mOriginalTouchPosition.y;

        mCurrentViewPosition = new PointF(
                mOriginalViewPosition.x + dragDeltaX,
                mOriginalViewPosition.y + dragDeltaY
        );

        if (!mIsDetachOriginal && isTouchWithinSlopOfOriginalTouch(dragDeltaX, dragDeltaY)) {
            mDragListener.onTap();
        } else {
            mX = mCurrentViewPosition.x;
            mY = mCurrentViewPosition.y;

            if (isUseVelocityTracker()) {
                mScaleVelocity = getDefaultScaleVelocity();
                startXAnimation();
                startYAnimation();
            } else {
                release();
            }
        }
    }


    private void startXAnimation() {
        mXFling = createAnimation(mX, mXVelocity, -100, mScreenWidth + 100);
        mXFling.addUpdateListener(mXAnimationUpdate);
        mXFling.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                stopAllAnimations();
            }
        });
        mXFling.start();
    }

    private void startYAnimation() {
        mYFling = createAnimation(mY, mYVelocity, -100, mScreenHeight + 100);
        mYFling.addUpdateListener(mYAnimationUpdate);
        mYFling.start();
    }

    private synchronized void stopAllAnimations() {
        if (mXFling != null) {
            mXFling.cancel();
            mXFling = null;
        }
        if (mYFling != null) {
            mYFling.cancel();
            mYFling = null;
        }
        release();
    }

    private void release() {
        if (mDragListener != null) {
            mDragListener.onReleasedAt(mCurrentViewPosition.x, mCurrentViewPosition.y);
        }
    }

    protected boolean isUseVelocityTracker() {
        return Math.abs(mXVelocity) > 70 && Math.abs(mYVelocity) > 70;
    }

    private void onUpdatePosition(float newX, float newY) {
        mX = newX;
        mY = newY;

        moveDragViewTo(new PointF(newX, newY));
        if (mDragListener != null)
            mDragListener.onDragTo(newX, newY, false);
    }

    private void computeVelocityTracker(MotionEvent motionEvent) {
        int index = motionEvent.getActionIndex();
        int pointerId = motionEvent.getPointerId(index);
        mVelocityTracker.addMovement(motionEvent);
        mVelocityTracker.computeCurrentVelocity(400, 8000f);
        mXVelocity = mVelocityTracker.getXVelocity(pointerId);
        mYVelocity = mVelocityTracker.getYVelocity(pointerId);
    }
}

