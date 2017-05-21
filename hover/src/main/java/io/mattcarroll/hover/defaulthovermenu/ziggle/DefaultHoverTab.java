package io.mattcarroll.hover.defaulthovermenu.ziggle;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import io.mattcarroll.hover.R;

/**
 * TODO
 */
public class DefaultHoverTab extends FrameLayout implements HoverTab {

    private int mTabSize;
    private View mTabView;

    public DefaultHoverTab(@NonNull Context context) {
        super(context);
        mTabSize = context.getResources().getDimensionPixelSize(R.dimen.floating_icon_size);
    }

    @Override
    public int getTabWidth() {
        return mTabSize;
    }

    @Override
    public int getTabHeight() {
        return mTabSize;
    }

    @Override
    public Point getPosition() {
        return convertCornerToCenter(
                new Point((int) getX(), (int) getY())
        );
    }

    @Override
    public void moveTo(@NonNull Point centerPosition) {
        Point cornerPosition = convertCenterToCorner(centerPosition);
        setX(cornerPosition.x);
        setY(cornerPosition.y);
    }

    @Override
    public void slideTo(@NonNull Point position) {
        slideTo(position, null);
    }

    @Override
    public void slideTo(@NonNull Point centerPosition, @Nullable final Runnable callback) {
        Point cornerPosition = convertCenterToCorner(centerPosition);

        ObjectAnimator xAnimation = ObjectAnimator.ofFloat(this, "x", getX(), cornerPosition.x);
        xAnimation.setDuration(500);
        xAnimation.setInterpolator(new OvershootInterpolator());
        ObjectAnimator yAnimation = ObjectAnimator.ofFloat(this, "y", getY(), cornerPosition.y);
        yAnimation.setDuration(500);
        yAnimation.setInterpolator(new OvershootInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(xAnimation).with(yAnimation);
        animatorSet.start();

        if (null != callback) {
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { }

                @Override
                public void onAnimationEnd(Animator animation) {
                    callback.run();
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
        }
    }

    @Override
    public void setTabView(@NonNull View view) {
        if (null != mTabView) {
            removeView(mTabView);
        }

        mTabView = view;
        FrameLayout.LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        addView(mTabView, layoutParams);
    }

    private Point convertCenterToCorner(@NonNull Point centerPosition) {
        return new Point(
                centerPosition.x - (getTabWidth() / 2),
                centerPosition.y - (getTabHeight() / 2)
        );
    }

    private Point convertCornerToCenter(@NonNull Point cornerPosition) {
        return new Point(
                cornerPosition.x + (getTabWidth() / 2),
                cornerPosition.y + (getTabHeight() / 2)
        );
    }
}
