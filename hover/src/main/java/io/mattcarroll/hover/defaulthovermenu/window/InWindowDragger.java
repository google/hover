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

    private static final String TAG = "WindowDragWatcher";

    private Context mContext;
    private WindowViewController mWindowViewController;
    private View mDragView;
    private Dragger.DragListener mDragListener;
    private float mTapTouchSlop;
    private boolean mIsActivated;
    private boolean mIsDragging;
    private Point mViewOriginalPosition = new Point();
    private PointF mTouchOffsetFromCornerOfControlView = new PointF();
    private PointF mViewCurrentPosition = new PointF();
    private PointF mPrevMotionPosition = new PointF();
    private boolean mIsDebugMode;

    private View.OnTouchListener mDragTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    mIsDragging = false;

                    mViewOriginalPosition = mWindowViewController.getViewPosition(mDragView);
                    mViewCurrentPosition = new PointF(mViewOriginalPosition.x, mViewOriginalPosition.y);
                    mTouchOffsetFromCornerOfControlView = new PointF(mViewOriginalPosition.x - motionEvent.getRawX(), mViewCurrentPosition.y - motionEvent.getRawY());
                    mPrevMotionPosition.set(motionEvent.getRawX(), motionEvent.getRawY());

                    mDragListener.onPress(mViewCurrentPosition.x, mViewCurrentPosition.y);

                    return true;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "ACTION_MOVE. motionX: " + motionEvent.getRawX() + ", motionY: " + motionEvent.getRawY());

                    mViewCurrentPosition = new PointF(motionEvent.getRawX(), motionEvent.getRawY());
                    mViewCurrentPosition.offset(mTouchOffsetFromCornerOfControlView.x, mTouchOffsetFromCornerOfControlView.y);

                    mPrevMotionPosition.set((int) motionEvent.getRawX(), (int) motionEvent.getRawY());

                    if (mIsDragging || !isTouchWithinSlopOfOriginalTouch()) {
                        // Dragging just started
                        if(!mIsDragging) {
                            Log.d(TAG, "MOVE Start Drag.");
                            mIsDragging = true;
                            Point dragViewPosition = mWindowViewController.getViewPosition(mDragView);
                            mDragListener.onDragStart(dragViewPosition.x, dragViewPosition.y);
                        } else {
                            mWindowViewController.moveViewTo(mDragView, (int) mViewCurrentPosition.x, (int) mViewCurrentPosition.y);
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

    /**
     * Note: {@code view} must already be added to the {@code Window}.
     * @param context context
     * @param windowViewController windowViewController
     * @param tapTouchSlop tapTouchSlop
     */
    public InWindowDragger(@NonNull Context context, @NonNull WindowViewController windowViewController, float tapTouchSlop) {
        mContext = context;
        mWindowViewController = windowViewController;
        mTapTouchSlop = tapTouchSlop;
    }

    public void activate(@NonNull DragListener dragListener, @NonNull Rect controlBounds) {
        if (!mIsActivated) {
            createTouchControlView(controlBounds);
            mDragListener = dragListener;
            mDragView.setOnTouchListener(mDragTouchListener);
            mIsActivated = true;
        }
    }

    public void deactivate() {
        if (mIsActivated) {
            mDragView.setOnTouchListener(null);
            destroyTouchControlView();
            mIsActivated = false;
        }
    }

    public void setDebugMode(boolean isDebugMode) {
        mIsDebugMode = isDebugMode;
        updateTouchControlViewAppearance();
    }

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
        float dx = mViewCurrentPosition.x - mViewOriginalPosition.x;
        float dy = mViewCurrentPosition.y - mViewOriginalPosition.y;
        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

        return distance < mTapTouchSlop;
    }

}
