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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * {@code View} that draws a triangle selector icon at a given horizontal position within its bounds.
 * A {@code TabSelectorView} is like a horizontal rail upon which its triangle selector can slide
 * left/right.
 *
 * Class is public to allow for XML use.
 */
public class TabSelectorView extends View {

    private static final String TAG = "HoverMenuTabSelectorView";

    private static final int DEFAULT_SELECTOR_WIDTH_DP = 24;
    private static final int DEFAULT_SELECTOR_HEIGHT_DP = 16;

    private int mSelectorWidthPx;
    private int mSelectorHeightPx;
    private int mDesiredSelectorCenterLocationPx; // the selector position that the client wants
    private int mLeftMostSelectorLocationPx; // based on mLeftBoundOffset and mSelectorWidthPx;
    private int mRightMostSelectorLocationPx; // based on mRightBoundOffsetPx and mSelectorWidthPx;

    private Path mSelectorPaintPath;
    private Paint mSelectorPaint;

    public TabSelectorView(Context context) {
        this(context, null);
    }

    public TabSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mSelectorWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SELECTOR_WIDTH_DP, getResources().getDisplayMetrics());
        mSelectorHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SELECTOR_HEIGHT_DP, getResources().getDisplayMetrics());
        setSelectorPosition(mSelectorWidthPx / 2);

        mSelectorPaint = new Paint();
        mSelectorPaint.setColor(getResources().getColor(R.color.hover_navigator_color));
        mSelectorPaint.setStyle(Paint.Style.FILL);
    }

    public void setSelectorColor(@ColorInt int color) {
        mSelectorPaint.setColor(color);
        invalidate();
    }

    /**
     * Sets the pixel position of the center of the selector icon. The position given will be
     * clamped to available space in this View.
     *
     * @param position horizontal pixel position
     */
    public void setSelectorPosition(int position) {
        mDesiredSelectorCenterLocationPx = position;
        invalidateSelectorPath();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mSelectorHeightPx);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            invalidateSelectorPath();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mSelectorPaintPath, mSelectorPaint);
    }

    private void invalidateSelectorPath() {
        mLeftMostSelectorLocationPx = getPaddingLeft() + (mSelectorWidthPx / 2);
        mRightMostSelectorLocationPx = getWidth() - getPaddingRight() - (mSelectorWidthPx / 2);

        int selectorCenterLocationPx = clampSelectorPosition(mDesiredSelectorCenterLocationPx);

        mSelectorPaintPath = new Path();
        mSelectorPaintPath.moveTo(selectorCenterLocationPx, 0); // top of triangle
        mSelectorPaintPath.lineTo(selectorCenterLocationPx + (mSelectorWidthPx / 2), mSelectorHeightPx); // bottom right of triangle
        mSelectorPaintPath.lineTo(selectorCenterLocationPx - (mSelectorWidthPx / 2), mSelectorHeightPx); // bottom left of triangle
        mSelectorPaintPath.lineTo(selectorCenterLocationPx, 0); // back to origin

        invalidate();
    }

    private int clampSelectorPosition(int position) {
        if (position < mLeftMostSelectorLocationPx) {
            return mLeftMostSelectorLocationPx;
        } else if (position > mRightMostSelectorLocationPx) {
            return mRightMostSelectorLocationPx;
        } else {
            return position;
        }
    }
}
