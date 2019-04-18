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

import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Reports user drag behavior on the screen to a {@link DragListener}.
 */
public abstract class Dragger extends BaseTouchController {
    private static final String TAG = "Dragger";

    private final int mTapTouchSlop;

    public Dragger(int mTapTouchSlop) {
        this.mTapTouchSlop = mTapTouchSlop;
    }

    public abstract PointF getTouchViewPosition(@NonNull View touchView);

    public abstract Point getContainerSize();

    @Override
    protected TouchDetector createTouchDetector(final View originalView, TouchListener touchListener) {
        if (touchListener instanceof DragListener) {
            return new DragDetector(originalView, (DragListener) touchListener);
        } else {
            return super.createTouchDetector(originalView, touchListener);
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
         * @param view the view that is being dragged
         * @param x    x-coordinate of the user's drag start (in the parent View's coordinate space)
         * @param y    y-coordiante of the user's drag start (in the parent View's coordinate space)
         */
        void onDragStart(View view, float x, float y);

        /**
         * The user has dragged to the given coordinates.
         *
         * @param view the view that is being dragged
         * @param x    x-coordinate of the user's drag (in the parent View's coordinate space)
         * @param y    y-coordiante of the user's drag (in the parent View's coordinate space)
         */
        void onDragTo(View view, float x, float y);

        /**
         * The user has stopped touching the drag area.
         *
         * @param view the view that is being dragged
         * @param x    x-coordinate of the user's release (in the parent View's coordinate space)
         * @param y    y-coordiante of the user's release (in the parent View's coordinate space)
         */
        void onReleasedAt(View view, float x, float y);
    }

    private class DragDetector extends TouchDetector<DragListener> {

        private final GestureDetector mGestureDetector;
        private boolean mIsDragging;
        private PointF mOriginalViewPosition = new PointF();
        private PointF mCurrentViewPosition = new PointF();
        private PointF mOriginalTouchPosition = new PointF();

        public DragDetector(final View originalView, final DragListener dragListener) {
            super(originalView, dragListener);
            mGestureDetector = new GestureDetector(null, new GestureDetector.SimpleOnGestureListener() {
                public void onLongPress(final MotionEvent e) {
                    tryDragStart("LONG_PRESS");
                }
            });
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGestureDetector.onTouchEvent(motionEvent);
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN");
                    mIsDragging = false;

                    mOriginalViewPosition = convertCornerToCenter(view, getTouchViewPosition(view));
                    mCurrentViewPosition = new PointF(mOriginalViewPosition.x, mOriginalViewPosition.y);
                    mOriginalTouchPosition.set(motionEvent.getRawX(), motionEvent.getRawY());
                    mEventListener.onTouchDown(view);
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
                        if (!tryDragStart("ACTION_MOVE")) {
                            mEventListener.onDragTo(mOriginalView, mCurrentViewPosition.x, mCurrentViewPosition.y);
                        }
                    }

                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP");
                    mEventListener.onTouchUp(view);
                    if (!mIsDragging) {
                        Log.d(TAG, "Reporting as a tap.");
                        mEventListener.onTap(mOriginalView);
                    } else {
                        Log.d(TAG, "Reporting as a drag release at: " + mCurrentViewPosition);
                        mEventListener.onReleasedAt(mOriginalView, mCurrentViewPosition.x, mCurrentViewPosition.y);
                    }
                    return true;
                default:
                    return false;
            }
        }

        private boolean tryDragStart(final String reason) {
            if (mIsDragging) {
                return false;
            }
            // Dragging is just started by reason
            Log.d(TAG, "" + reason + " starts drag.");
            mIsDragging = true;
            mEventListener.onDragStart(mOriginalView, mCurrentViewPosition.x, mCurrentViewPosition.y);
            return true;
        }
    }
}
