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
import android.graphics.Rect;
import androidx.annotation.NonNull;
import android.view.View;

import io.mattcarroll.hover.Dragger;

/**
 * {@link Dragger} implementation that works within a {@code Window}.
 */
public class InWindowDragger extends Dragger {
    private static final String TAG = "InWindowDragger";

    private final Context mContext;
    private final WindowViewController mWindowViewController;

    public InWindowDragger(@NonNull Context context,
                           @NonNull WindowViewController windowViewController,
                           int tapTouchSlop) {
        super(tapTouchSlop);
        mContext = context;
        mWindowViewController = windowViewController;
    }

    @Override
    public View createTouchView(@NonNull Rect rect) {
        View dragView = new View(mContext);
        final int width = rect.right - rect.left;
        final int height = rect.bottom - rect.top;
        mWindowViewController.addView(width, height, true, dragView);
        return dragView;
    }

    @Override
    public void destroyTouchView(@NonNull View touchView) {
        mWindowViewController.removeView(touchView);
    }

    @Override
    public PointF getTouchViewPosition(@NonNull View touchView) {
        return new PointF(mWindowViewController.getViewPosition(touchView));
    }

    @Override
    public Point getContainerSize() {
        return mWindowViewController.getWindowSize();
    }

    @Override
    public void moveTouchViewTo(@NonNull View touchView, @NonNull PointF cornerPosition) {
        mWindowViewController.moveViewTo(touchView, (int) cornerPosition.x, (int) cornerPosition.y);
    }
}
