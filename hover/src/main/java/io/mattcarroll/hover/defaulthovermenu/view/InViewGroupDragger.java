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
    private int mTapTouchSlop;
    private PointF mViewOriginalPosition = new PointF();
    private PointF mViewCurrentPosition = new PointF();
    private PointF mPrevMotionPosition = new PointF();

    private View.OnTouchListener mDragTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    mIsDragging = false;

//                    mViewOriginalPosition = mWindowViewController.getViewPosition(mDragView);
                    mViewOriginalPosition = getDragViewPosition();
                    mViewCurrentPosition = new PointF(mViewOriginalPosition.x, mViewOriginalPosition.y);
                    mPrevMotionPosition.set(motionEvent.getRawX(), motionEvent.getRawY());

                    mDragListener.onPress(mViewCurrentPosition.x, mViewCurrentPosition.y);

                    return true;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "ACTION_MOVE. motionX: " + motionEvent.getRawX() + ", motionY: " + motionEvent.getRawY());
                    float dragDeltaX = motionEvent.getRawX() - mPrevMotionPosition.x;
                    float dragDeltaY = motionEvent.getRawY() - mPrevMotionPosition.y;

                    mViewCurrentPosition.offset(dragDeltaX, dragDeltaY);

                    mPrevMotionPosition.set((int) motionEvent.getRawX(), (int) motionEvent.getRawY());

                    if (mIsDragging || !isTouchWithinSlopOfOriginalTouch()) {
                        // Dragging just started
                        if(!mIsDragging) {
                            Log.d(TAG, "MOVE Start Drag.");
                            mIsDragging = true;
//                            Point dragViewPosition = mWindowViewController.getViewPosition(mDragView);
                            PointF dragViewPosition = getDragViewPosition();
                            mDragListener.onDragStart(dragViewPosition.x, dragViewPosition.y);
                        } else {
//                            mWindowViewController.moveViewTo(mDragView, (int) mViewCurrentPosition.x, (int) mViewCurrentPosition.y);
                            moveDragViewTo(mViewCurrentPosition);
                            mDragListener.onDragTo(mViewCurrentPosition.x, mViewCurrentPosition.y);
                        }
                    }

                    return true;
                case MotionEvent.ACTION_UP:
                    if (!mIsDragging) {
                        mDragListener.onTap();
                    } else {
                        mDragListener.onReleasedAt(mViewCurrentPosition.x, mViewCurrentPosition.y);
                    }

                    return true;
            }

            return false;
        }
    };

    public InViewGroupDragger(@NonNull ViewGroup container, int touchSlop) {
        mContainer = container;
        mTapTouchSlop = touchSlop;
    }

    public void setDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
        updateTouchControlViewAppearance();
    }

    @Override
    public void activate(@NonNull DragListener dragListener, @NonNull Rect controlBounds) {
        if (!mIsActivated) {
            mDragListener = dragListener;
            createTouchControlView(controlBounds);
            mIsActivated = true;
        }
    }

    @Override
    public void deactivate() {
        if (mIsActivated) {
            destroyTouchControlView();
            mIsActivated = false;
        }
    }

    private void createTouchControlView(Rect bounds) {
        mDragView = new View(mContainer.getContext());
        mDragView.setLayoutParams(new ViewGroup.LayoutParams(bounds.width(), bounds.height()));
        mContainer.addView(mDragView);
        mDragView.setOnTouchListener(mDragTouchListener);
        moveDragViewTo(new PointF(bounds.left, bounds.top));

        updateTouchControlViewAppearance();
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
        float dx = mViewCurrentPosition.x - mViewOriginalPosition.x;
        float dy = mViewCurrentPosition.y - mViewOriginalPosition.y;
        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

        return distance < mTapTouchSlop;
    }

    private PointF getDragViewPosition() {
        return new PointF(mDragView.getX(), mDragView.getY());
    }

    private void moveDragViewTo(PointF position) {
        mDragView.setX(position.x);
        mDragView.setY(position.y);
    }
}
