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
package io.mattcarroll.hover.hoverdemo.hovermenu.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;

/**
 * Visual representation of a top-level tab in a Hover menu.
 */
public class DemoTabView extends View {

    private int mBackgroundColor;
    private Integer mForegroundColor;

    private Drawable mCircleDrawable;
    private Drawable mIconDrawable;
    private int mIconInsetLeft, mIconInsetTop, mIconInsetRight, mIconInsetBottom;

    public DemoTabView(Context context, Drawable backgroundDrawable, Drawable iconDrawable) {
        super(context);
        mCircleDrawable = backgroundDrawable;
        mIconDrawable = iconDrawable;
        init();
    }

    private void init() {
        int insetsDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getContext().getResources().getDisplayMetrics());
        mIconInsetLeft = mIconInsetTop = mIconInsetRight = mIconInsetBottom = insetsDp;
    }

    public void setTabBackgroundColor(@ColorInt int backgroundColor) {
        mBackgroundColor = backgroundColor;
        mCircleDrawable.setColorFilter(mBackgroundColor, PorterDuff.Mode.SRC_ATOP);
    }

    public void setTabForegroundColor(@ColorInt Integer foregroundColor) {
        mForegroundColor = foregroundColor;
        if (null != mForegroundColor) {
            mIconDrawable.setColorFilter(mForegroundColor, PorterDuff.Mode.SRC_ATOP);
        } else {
            mIconDrawable.setColorFilter(null);
        }
    }

    public void setIcon(@Nullable Drawable icon) {
        mIconDrawable = icon;
        if (null != mForegroundColor && null != mIconDrawable) {
            mIconDrawable.setColorFilter(mForegroundColor, PorterDuff.Mode.SRC_ATOP);
        }
        updateIconBounds();

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Make circle as large as View minus padding.
        mCircleDrawable.setBounds(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());

        // Re-size the icon as necessary.
        updateIconBounds();

        invalidate();
    }

    private void updateIconBounds() {
        if (null != mIconDrawable) {
            Rect bounds = new Rect(mCircleDrawable.getBounds());
            bounds.set(bounds.left + mIconInsetLeft, bounds.top + mIconInsetTop, bounds.right - mIconInsetRight, bounds.bottom - mIconInsetBottom);
            mIconDrawable.setBounds(bounds);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCircleDrawable.draw(canvas);
        if (null != mIconDrawable) {
            mIconDrawable.draw(canvas);
        }
    }
}
