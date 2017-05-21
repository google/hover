package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.mattcarroll.hover.R;

/**
 * TODO
 */

public class FloatingTab extends FrameLayout implements Tab {

    private static final String TAG = "FloatingTab";

    private int mTabSize;
    private Point mDock;
    private final Set<OnPositionChangeListener> mOnPositionChangeListeners = new CopyOnWriteArraySet<OnPositionChangeListener>();

    private final OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            notifyOnPositionChangeListeners();
        }
    };

    public FloatingTab(@NonNull Context context) {
        super(context);
        mTabSize = getResources().getDimensionPixelSize(R.dimen.floating_icon_size);
        setBackgroundColor(0x8800FF00);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Make this View the desired size.
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = mTabSize;
        layoutParams.height = mTabSize;
        setLayoutParams(layoutParams);

        addOnLayoutChangeListener(mOnLayoutChangeListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnLayoutChangeListener(mOnLayoutChangeListener);
    }

    @Override
    public void setTabView(@Nullable View view) {
        removeAllViews();

        if (null != view) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.gravity = Gravity.CENTER;
            addView(view, layoutParams);
        }
    }

    // Returns the center position of this tab.
    @NonNull
    public Point getPosition() {
        return new Point(
                (int) (getX() + (getSize() / 2)),
                (int) (getY() + (getSize() / 2))
        );
    }

    @Nullable
    @Override
    public Point getDockPosition() {
        return mDock;
    }

    @NonNull
    public Rect getBounds() {
        Point cornerPosition = getCornerPosition();
        return new Rect(cornerPosition.x, cornerPosition.y, cornerPosition.x + getSize(), cornerPosition.y + getSize());
    }

    private Point getCornerPosition() {
        return new Point(
                (int) getX(),
                (int) getY()
        );
    }

    public void dockTo(@NonNull Point dock) {
        dockTo(dock, null);
    }

    public void dockTo(@NonNull Point dock, @Nullable final Runnable onDocked) {
        mDock = dock;
        Point destinationCornerPosition = convertCenterToCorner(mDock);
        Log.d(TAG, "Docking to destination point: " + destinationCornerPosition);

        ObjectAnimator xAnimation = ObjectAnimator.ofFloat(this, "x", destinationCornerPosition.x);
        xAnimation.setDuration(500);
        xAnimation.setInterpolator(new OvershootInterpolator());
        ObjectAnimator yAnimation = ObjectAnimator.ofFloat(this, "y", destinationCornerPosition.y);
        yAnimation.setDuration(500);
        yAnimation.setInterpolator(new OvershootInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(xAnimation).with(yAnimation);
        animatorSet.start();

        if (null != onDocked) {
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { }

                @Override
                public void onAnimationEnd(Animator animation) {
                    onDocked.run();
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
        }
    }

    public void moveTo(@NonNull Point floatPosition) {
        Point cornerPosition = convertCenterToCorner(floatPosition);
        setX(cornerPosition.x);
        setY(cornerPosition.y);
    }

    private Point convertCenterToCorner(@NonNull Point centerPosition) {
        return new Point(
                centerPosition.x - (getSize() / 2),
                centerPosition.y - (getSize() / 2)
        );
    }

    @Override
    public void addOnPositionChangeListener(@Nullable OnPositionChangeListener listener) {
        mOnPositionChangeListeners.add(listener);
    }

    @Override
    public void removeOnPositionChangeListener(@NonNull OnPositionChangeListener listener) {
        mOnPositionChangeListeners.remove(listener);
    }

    private void notifyOnPositionChangeListeners() {
        Point position = getPosition();
        for (OnPositionChangeListener listener : mOnPositionChangeListeners) {
            listener.onPositionChange(position);
        }
    }

    // This method is declared in this class simply to make it clear that its part of our public
    // contract and not just an inherited method.
    public void setOnClickListener(@Nullable View.OnClickListener onClickListener) {
        super.setOnClickListener(onClickListener);
    }

    private int getSize() {
        return mTabSize;
    }
}
