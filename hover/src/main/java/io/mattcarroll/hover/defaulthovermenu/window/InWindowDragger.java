/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mattcarroll.hover.defaulthovermenu.window;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import io.mattcarroll.hover.defaulthovermenu.Dragger;

/**
 * {@link Dragger} implementation that works within a {@code Window}.
 */
public class InWindowDragger implements Dragger {

    private static final String TAG = "InWindowDragger";

    private Context mContext;
    private WindowViewController mWindowViewController;
    private View mDragView;
    private Dragger.DragListener mDragListener;
    private final int mTouchAreaDiameter;
    private float mTapTouchSlop;
    private boolean mIsActivated;
    private boolean mIsDragging;
    private boolean mIsDebugMode;

    private PointF mOriginalViewPosition = new PointF();
    private PointF mCurrentViewPosition = new PointF();
    private PointF mOriginalTouchPosition = new PointF();

    private View.OnTouchListener mDragTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    mIsDragging = false;

                    mOriginalViewPosition = getDragViewCenterPosition();
                    mCurrentViewPosition = new PointF(mOriginalViewPosition.x, mOriginalViewPosition.y);
                    mOriginalTouchPosition.set(motionEvent.getRawX(), motionEvent.getRawY());

                    mDragListener.onPress(mCurrentViewPosition.x, mCurrentViewPosition.y);

                    return true;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "ACTION_MOVE. motionX: " + motionEvent.getRawX() + ", motionY: " + motionEvent.getRawY());
                    float dragDeltaX = motionEvent.getRawX() - mOriginalTouchPosition.x;
                    float dragDeltaY = motionEvent.getRawY() - mOriginalTouchPosition.y;
                    mCurrentViewPosition = new PointF(
                            mOriginalViewPosition.x + dragDeltaX,
                            mOriginalViewPosition.y + dragDeltaY
                    );

                    if (mIsDragging || !isTouchWithinSlopOfOriginalTouch()) {
                        if(!mIsDragging) {
                            // Dragging just started
                            Log.d(TAG, "MOVE Start Drag.");
                            mIsDragging = true;
                            mDragListener.onDragStart(mCurrentViewPosition.x, mCurrentViewPosition.y);
                        } else {
                            moveDragViewTo(mCurrentViewPosition);
                            mDragListener.onDragTo(mCurrentViewPosition.x, mCurrentViewPosition.y);
                        }
                    }

                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    if (!mIsDragging) {
                        Log.d(TAG, "Reporting as a tap.");
                        mDragListener.onTap();
                    } else {
                        Log.d(TAG, "Reporting as a drag release at: " + mCurrentViewPosition);
                        mDragListener.onReleasedAt(mCurrentViewPosition.x, mCurrentViewPosition.y);
                    }

                    return true;
            }

            return false;
        }
    };

    /**
     * Note: {@code view} must already be added to the {@code Window}.
     * @param context context
     * @param windowViewController windowViewController
     * @param tapTouchSlop tapTouchSlop
     */
    public InWindowDragger(@NonNull Context context,
                           @NonNull WindowViewController windowViewController,
                           int touchAreaDiameter,
                           float tapTouchSlop) {
        mContext = context;
        mWindowViewController = windowViewController;
        mTouchAreaDiameter = touchAreaDiameter;
        mTapTouchSlop = tapTouchSlop;
    }

    public void activate(@NonNull DragListener dragListener, @NonNull Point dragStartCenterPosition) {
        if (!mIsActivated) {
            Log.d(TAG, "Activating.");
            createTouchControlView(dragStartCenterPosition);
            mDragListener = dragListener;
            mDragView.setOnTouchListener(mDragTouchListener);
            mIsActivated = true;
        }
    }

    public void deactivate() {
        if (mIsActivated) {
            Log.d(TAG, "Deactivating.");
            mDragView.setOnTouchListener(null);
            destroyTouchControlView();
            mIsActivated = false;
        }
    }

    @Override
    public void enableDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
        updateTouchControlViewAppearance();
    }

    private void createTouchControlView(@NonNull final Point dragStartCenterPosition) {
        // TODO: define dimen size
        mDragView = new View(mContext);
        mWindowViewController.addView(mTouchAreaDiameter, mTouchAreaDiameter, true, mDragView);
        mWindowViewController.moveViewTo(mDragView, dragStartCenterPosition.x - (mTouchAreaDiameter / 2), dragStartCenterPosition.y - (mTouchAreaDiameter / 2));
        mDragView.setOnTouchListener(mDragTouchListener);

        updateTouchControlViewAppearance();

        // Run layout listener once to position the drag view based on the width/height of the drag view.
//        mDragView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                mDragView.removeOnLayoutChangeListener(this);
//
////                moveDragViewTo(new PointF(dragStartCenterPosition.x, dragStartCenterPosition.y));
//                mWindowViewController.moveViewTo(mDragView, dragStartCenterPosition.x - (mTouchAreaDiameter / 2), dragStartCenterPosition.y - (mTouchAreaDiameter / 2));
//                updateTouchControlViewAppearance();
//            }
//        });
    }

    // TODO: this was old implementation. remove it.
    private void createTouchControlView(Rect bounds) {
        mDragView = new View(mContext);
        int width = bounds.width();
        int height = bounds.height();
        mWindowViewController.addView(width, height, true, mDragView);
        mWindowViewController.moveViewTo(mDragView, bounds.left, bounds.top);

        updateTouchControlViewAppearance();
    }

    private void destroyTouchControlView() {
        mWindowViewController.removeView(mDragView);
        mDragView = null;
    }

    private void updateTouchControlViewAppearance() {
        if (null != mDragView) {
            if (mIsDebugMode) {
                mDragView.setBackgroundColor(0x44FF0000);
            } else {
                mDragView.setBackgroundColor(0x00000000);
            }
        }
    }

    private boolean isTouchWithinSlopOfOriginalTouch() {
        float dx = mCurrentViewPosition.x - mOriginalTouchPosition.x;
        float dy = mCurrentViewPosition.y - mOriginalTouchPosition.y;
        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        Log.d(TAG, "Drag distance " + distance + " vs slop allowance " + mTapTouchSlop);

        return distance < mTapTouchSlop;
    }

    private PointF getDragViewCenterPosition() {
        Point cornerPosition = mWindowViewController.getViewPosition(mDragView);
        return convertCornerToCenter(new PointF(
                cornerPosition.x,
                cornerPosition.y
        ));
    }

    private void moveDragViewTo(PointF centerPosition) {
        Log.d(TAG, "Center position: " + centerPosition);
        PointF cornerPosition = convertCenterToCorner(centerPosition);
        Log.d(TAG, "Corner position: " + cornerPosition);
        mWindowViewController.moveViewTo(mDragView, (int) cornerPosition.x, (int) cornerPosition.y);
    }

    private PointF convertCornerToCenter(@NonNull PointF cornerPosition) {
        return new PointF(
                cornerPosition.x + (mDragView.getWidth() / 2),
                cornerPosition.y + (mDragView.getHeight() / 2)
        );
    }

    private PointF convertCenterToCorner(@NonNull PointF centerPosition) {
        return new PointF(
                centerPosition.x - (mDragView.getWidth() / 2),
                centerPosition.y - (mDragView.getHeight() / 2)
        );
    }
}
