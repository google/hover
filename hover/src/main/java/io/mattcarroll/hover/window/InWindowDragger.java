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
package io.mattcarroll.hover.window;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import io.mattcarroll.hover.Dragger;
import io.mattcarroll.hover.base.DraggerImpl;

/**
 * {@link Dragger} implementation that works within a {@code Window}.
 */
public class InWindowDragger extends DraggerImpl {

    private static final String TAG = "InWindowDragger";

    private final Context mContext;
    private final WindowViewController mWindowViewController;
    private View mDragView;

    public InWindowDragger(@NonNull Context context,
                           @NonNull WindowViewController windowViewController,
                           int touchAreaDiameter,
                           float tapTouchSlop) {
        super(touchAreaDiameter, tapTouchSlop);
        mContext = context;
        mWindowViewController = windowViewController;
    }

    @Override
    protected void createTouchControlView(@NonNull final Point dragStartCenterPosition) {
        // TODO: define dimen size
        mDragView = new View(mContext);
        mWindowViewController.addViewDragger(getTouchAreaDiameter(), getTouchAreaDiameter(), true, mDragView);
        mWindowViewController.moveViewTo(mDragView, dragStartCenterPosition.x - (getTouchAreaDiameter() / 2), dragStartCenterPosition.y - (getTouchAreaDiameter() / 2));
        mDragView.setOnTouchListener(getDragTouchListener());

        updateTouchControlViewAppearance();
    }

    @Override
    protected View getDragView() {
        return mDragView;
    }

    @Override
    protected void destroyTouchControlView() {
        mWindowViewController.removeView(mDragView);
        mDragView = null;
    }

    @Override
    protected PointF getDragViewCenterPosition() {
        Point cornerPosition = mWindowViewController.getViewPosition(mDragView);
        return convertCornerToCenter(new PointF(
                cornerPosition.x,
                cornerPosition.y
        ));
    }

    @Override
    protected void moveDragViewTo(PointF centerPosition) {
        if (mDragView == null) return;
        Log.d(TAG, "Center position: " + centerPosition);
        PointF cornerPosition = convertCenterToCorner(centerPosition);
        Log.d(TAG, "Corner position: " + cornerPosition);
        mWindowViewController.moveViewTo(mDragView, (int) cornerPosition.x, (int) cornerPosition.y);
    }
}
