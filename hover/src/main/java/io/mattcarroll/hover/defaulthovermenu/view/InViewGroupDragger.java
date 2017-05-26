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
package io.mattcarroll.hover.defaulthovermenu.view;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import io.mattcarroll.hover.defaulthovermenu.Dragger;

/**
 * {@link Dragger} implementation that works within a {@link ViewGroup}.
 */
public class InViewGroupDragger implements Dragger {

    private static final String TAG = "InViewGroupDragger";

    private final ViewGroup mContainer;
    private View mDragView;
    private boolean mIsActivated;
    private boolean mIsDragging;
    private boolean mIsDebugMode = false;
    private DragListener mDragListener;
    private final int mTouchAreaDiameter;
    private int mTapTouchSlop;
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
                    if (!mIsDragging) {
                        mDragListener.onTap();
                    } else {
                        mDragListener.onReleasedAt(mCurrentViewPosition.x, mCurrentViewPosition.y);
                    }

                    return true;
            }

            return false;
        }
    };

    public InViewGroupDragger(@NonNull ViewGroup container, int touchAreaDiameter, int touchSlop) {
        mContainer = container;
        mTouchAreaDiameter = touchAreaDiameter;
        mTapTouchSlop = touchSlop;
    }

    @Override
    public void enableDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
        updateTouchControlViewAppearance();
    }

    @Override
    public void activate(@NonNull DragListener dragListener, @NonNull Point dragStartCenterPosition) {
        if (!mIsActivated) {
            mDragListener = dragListener;
            createTouchControlView(dragStartCenterPosition);
            mIsActivated = true;
        }
    }

    @Override
    public void activate(@NonNull DragListener dragListener, @NonNull Rect bounds) {
        // TODO: delete this.
    }

    @Override
    public void deactivate() {
        if (mIsActivated) {
            destroyTouchControlView();
            mIsActivated = false;
        }
    }

    private void createTouchControlView(@NonNull final Point dragStartCenterPosition) {
        // TODO: define dimen size
        mDragView = new View(mContainer.getContext());
        mDragView.setLayoutParams(new ViewGroup.LayoutParams(mTouchAreaDiameter, mTouchAreaDiameter));
        mContainer.addView(mDragView);
        mDragView.setOnTouchListener(mDragTouchListener);

        // Run layout listener once to position the drag view based on the width/height of the drag view.
        mDragView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mDragView.removeOnLayoutChangeListener(this);

                moveDragViewTo(new PointF(dragStartCenterPosition.x, dragStartCenterPosition.y));
                updateTouchControlViewAppearance();
            }
        });
    }

    private void destroyTouchControlView() {
        mContainer.removeView(mDragView);
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
        float dx = mCurrentViewPosition.x - mOriginalViewPosition.x;
        float dy = mCurrentViewPosition.y - mOriginalViewPosition.y;
        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

        return distance < mTapTouchSlop;
    }

    private PointF getDragViewCenterPosition() {
        return convertCornerToCenter(new PointF(
                mDragView.getX(),
                mDragView.getY()
        ));
    }

    private void moveDragViewTo(PointF centerPosition) {
        Log.d(TAG, "Center position: " + centerPosition);
        PointF cornerPosition = convertCenterToCorner(centerPosition);
        Log.d(TAG, "Corner position: " + cornerPosition);
        mDragView.setX(cornerPosition.x);
        mDragView.setY(cornerPosition.y);
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
