package io.mattcarroll.hover.hoverdemo.menu.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;

import java.lang.reflect.Type;

/**
 * Visual representation of a top-level tab in a Hover menu.
 */
public class DemoTabViewHack extends View {

    private int mBackgroundColor;
    private Integer mForegroundColor;

    private Drawable mCircleDrawable;
    private Drawable mIconDrawable;
    private int mIconInsetLeft, mIconInsetTop, mIconInsetRight, mIconInsetBottom;

    public DemoTabViewHack(Context context, Drawable backgroundDrawable, Drawable iconDrawable) {
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
        mCircleDrawable.setTint(mBackgroundColor);
    }

    public void setTabForegroundColor(@ColorInt Integer foregroundColor) {
        mForegroundColor = foregroundColor;
        if (null != mForegroundColor) {
            mIconDrawable.setTint(mForegroundColor);
        } else {
            mIconDrawable.setTintList(null);
        }
    }

//    public void setForegroundColor(int foregroundColor) {
//        mForegroundColor = foregroundColor;
//        if (null != mIconDrawable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mIconDrawable.setTint(foregroundColor);
//        }
//
//        invalidate();
//    }

//    public void setBackgroundColor(int backgroundColor) {
//        mBackgroundColor = backgroundColor;
//        mCircleDrawable.getPaint().setColor(mBackgroundColor);
//
//        invalidate();
//    }

    public void setIcon(@Nullable Drawable icon) {
        mIconDrawable = icon;
        if (null != mForegroundColor && null != mIconDrawable) {
            mIconDrawable.setTint(mForegroundColor);
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
