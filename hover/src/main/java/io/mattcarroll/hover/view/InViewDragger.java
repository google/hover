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

import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import io.mattcarroll.hover.Dragger;
import io.mattcarroll.hover.R;

/**
 * {@link Dragger} implementation that works within a {@link ViewGroup}.
 */
public class InViewDragger extends Dragger {
    private static final String TAG = "InViewDragger";

    private final ViewGroup mContainer;

    public InViewDragger(@NonNull ViewGroup container, int touchSlop) {
        super(touchSlop);
        mContainer = container;
    }

    @Override
    public View createTouchView(@NonNull Rect rect) {
        View dragView = new View(mContainer.getContext());
        dragView.setId(R.id.hover_drag_view);
        final int width = rect.right - rect.left;
        final int height = rect.bottom - rect.top;
        dragView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        mContainer.addView(dragView);
        return dragView;
    }

    @Override
    public void destroyTouchView(@NonNull View touchView) {
        mContainer.removeView(touchView);
    }

    @Override
    public PointF getTouchViewPosition(@NonNull View touchView) {
        return new PointF(
                touchView.getX(),
                touchView.getY()
        );
    }

    @Override
    public void moveTouchViewTo(@NonNull View touchView, @NonNull PointF position) {
        touchView.setX(position.x);
        touchView.setY(position.y);
    }
}
