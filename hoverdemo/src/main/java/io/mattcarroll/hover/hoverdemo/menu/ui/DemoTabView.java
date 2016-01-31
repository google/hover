package io.mattcarroll.hover.hoverdemo.menu.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Visual representation of a top-level tab in a Hover menu.
 */
public class DemoTabView extends View {

    private int mBackgroundColor = 0xFFFF9600;
    private int mForegroundColor = 0xFF000000;

    private ShapeDrawable mCircleDrawable;
    private Drawable mIconDrawable;

    public DemoTabView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mCircleDrawable = new ShapeDrawable(new OvalShape());
        mCircleDrawable.getPaint().setColor(mBackgroundColor);
        mCircleDrawable.getPaint().setStyle(Paint.Style.FILL);
    }

    public void setForegroundColor(int foregroundColor) {
        mForegroundColor = foregroundColor;
        if (null != mIconDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mIconDrawable.setTint(foregroundColor);
        }

        invalidate();
    }

    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        mCircleDrawable.getPaint().setColor(mBackgroundColor);

        invalidate();
    }

    public void setIcon(@Nullable Drawable icon) {
        mIconDrawable = icon;
        updateIconBounds(getWidth(), getHeight());

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Make circle as large as View minus padding.
        mCircleDrawable.setBounds(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());

        // Re-size the icon as necessary.
        updateIconBounds(w, h);

        invalidate();
    }

    private void updateIconBounds(int width, int height) {
        if (null != mIconDrawable) {
            mIconDrawable.setBounds(0, 0, width, height);
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
