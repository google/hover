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
package io.mattcarroll.hover.view;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.animation.FlingAnimation;
import android.support.animation.FloatValueHolder;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import io.mattcarroll.hover.Dragger;
import io.mattcarroll.hover.base.DraggerImpl;

/**
 * {@link Dragger} implementation that works within a {@link ViewGroup}.
 */
public class InViewDragger extends DraggerImpl {

    private static final String TAG = "InViewDragger";

    private final ViewGroup mContainer;
    private View mDragView;

    public InViewDragger(@NonNull ViewGroup container, int touchAreaDiameter, int touchSlop) {
        super(touchAreaDiameter, touchSlop);
        mContainer = container;
    }

    @Override
    protected View getDragView() {
        return mDragView;
    }

    @Override
    protected PointF getDragViewCenterPosition() {
        return convertCornerToCenter(new PointF(
                mDragView.getX(),
                mDragView.getY()
        ));
    }

    @Override
    protected void createTouchControlView(@NonNull Point dragStartCenterPosition) {
        mDragView = new View(mContainer.getContext());
        mDragView.setLayoutParams(new ViewGroup.LayoutParams(getTouchAreaDiameter(), getTouchAreaDiameter()));
        mDragView.setOnTouchListener(getDragTouchListener());
        mDragView.setBackgroundColor(Color.RED);
        mContainer.addView(mDragView);

        moveDragViewTo(new PointF(dragStartCenterPosition.x - getTouchAreaDiameter() / 2f, dragStartCenterPosition.y - getTouchAreaDiameter() / 2f));
        updateTouchControlViewAppearance();
    }

    @Override
    protected void destroyTouchControlView() {
        mContainer.removeView(mDragView);

        mDragView.setOnTouchListener(null);
        mDragView = null;
    }

    @Override
    protected void moveDragViewTo(PointF centerPosition) {
        PointF cornerPosition = convertCenterToCorner(centerPosition);
        mDragView.setX(cornerPosition.x);
        mDragView.setY(cornerPosition.y);
    }

    @Override
    protected FlingAnimation createAnimation(float startValue, float startVelocity, float minValue, float maxValue) {
        float scaleVelocity = 12000f / Math.max(Math.abs(getXVelocity()), Math.abs(getYVelocity()));
        return new FlingAnimation(new FloatValueHolder(startValue))
                .setStartVelocity(scaleVelocity * startVelocity)
                .setMinValue(minValue)
                .setMaxValue(maxValue)
                .setFriction(getDefaultFriction());
    }
}
