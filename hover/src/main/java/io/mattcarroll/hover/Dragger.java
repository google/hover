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
package io.mattcarroll.hover;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

/**
 * Reports user drag behavior on the screen to a {@link DragListener}.
 */
public abstract class Dragger extends BaseTouchController {
    private static final String TAG = "Dragger";

    private final int mTapTouchSlop;
    private DragListener mDragListener;
    private boolean mIsDragging;

    private PointF mOriginalViewPosition = new PointF();
    private PointF mCurrentViewPosition = new PointF();
    private PointF mOriginalTouchPosition = new PointF();

    protected final View.OnTouchListener mDragTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    mIsDragging = false;

                    mOriginalViewPosition = convertCornerToCenter(view, getTouchViewPosition(view));
                    mCurrentViewPosition = new PointF(mOriginalViewPosition.x, mOriginalViewPosition.y);
                    mOriginalTouchPosition.set(motionEvent.getRawX(), motionEvent.getRawY());
                    mTouchListener.onPress();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "ACTION_MOVE. motionX: " + motionEvent.getRawX() + ", motionY: " + motionEvent.getRawY());
                    float dragDeltaX = motionEvent.getRawX() - mOriginalTouchPosition.x;
                    float dragDeltaY = motionEvent.getRawY() - mOriginalTouchPosition.y;
                    mCurrentViewPosition = new PointF(
                            mOriginalViewPosition.x + dragDeltaX,
                            mOriginalViewPosition.y + dragDeltaY
                    );

                    if (mIsDragging || !isTouchWithinSlopOfOriginalTouch(dragDeltaX, dragDeltaY)) {
                        if (!mIsDragging) {
                            // Dragging just started
                            Log.d(TAG, "MOVE Start Drag.");
                            mIsDragging = true;
                            mDragListener.onDragStart(mCurrentViewPosition.x, mCurrentViewPosition.y);
                        } else {
                            mDragListener.onDragTo(mCurrentViewPosition.x, mCurrentViewPosition.y);
                        }
                    }

                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    if (!mIsDragging) {
                        Log.d(TAG, "Reporting as a tap.");
                        mTouchListener.onTap();
                    } else {
                        Log.d(TAG, "Reporting as a drag release at: " + mCurrentViewPosition);
                        mDragListener.onReleasedAt(mCurrentViewPosition.x, mCurrentViewPosition.y);
                    }
                    return true;
                default:
                    return false;
            }
        }
    };

    public Dragger(int mTapTouchSlop) {
        this.mTapTouchSlop = mTapTouchSlop;
    }

    public abstract PointF getTouchViewPosition(@NonNull View touchView);

    public void activate(@NonNull DragListener dragListener, @NonNull List<View> viewList) {
        super.activate(dragListener, viewList);
        mDragListener = dragListener;
        for (View touchView : mTouchViewMap.values()) {
            touchView.setOnTouchListener(mDragTouchListener);
        }
    }

    private boolean isTouchWithinSlopOfOriginalTouch(float dx, float dy) {
        double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        Log.d(TAG, "Drag distance " + distance + " vs slop allowance " + mTapTouchSlop);
        return distance < mTapTouchSlop;
    }

    private PointF convertCornerToCenter(View touchView, @NonNull PointF cornerPosition) {
        return new PointF(
                cornerPosition.x + (touchView.getWidth() / 2f),
                cornerPosition.y + (touchView.getHeight() / 2f)
        );
    }

    private PointF convertCenterToCorner(View touchView, @NonNull PointF centerPosition) {
        return new PointF(
                centerPosition.x - (touchView.getWidth() / 2f),
                centerPosition.y - (touchView.getHeight() / 2f)
        );
    }

    public interface DragListener extends TouchListener {
        /**
         * The user has begun dragging.
         *
         * @param x x-coordinate of the user's drag start (in the parent View's coordinate space)
         * @param y y-coordiante of the user's drag start (in the parent View's coordinate space)
         */
        void onDragStart(float x, float y);

        /**
         * The user has dragged to the given coordinates.
         *
         * @param x x-coordinate of the user's drag (in the parent View's coordinate space)
         * @param y y-coordiante of the user's drag (in the parent View's coordinate space)
         */
        void onDragTo(float x, float y);

        /**
         * The user has stopped touching the drag area.
         *
         * @param x x-coordinate of the user's release (in the parent View's coordinate space)
         * @param y y-coordiante of the user's release (in the parent View's coordinate space)
         */
        void onReleasedAt(float x, float y);
    }
}
