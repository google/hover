package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.mattcarroll.hover.R;

/**
 * TODO:
 */
public class ChainedTab extends FrameLayout implements Tab {

    private static final String TAG = "ChainedTab";

    private final int mTabSize;
    private Point mAnchorPosition;
    private Tab mPredecessorTab;
    private final Set<OnPositionChangeListener> mOnPositionChangeListeners = new CopyOnWriteArraySet<OnPositionChangeListener>();

    private final OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            Log.d(TAG, ChainedTab.this.hashCode() + " is now at x: " + left);
            notifyOnPositionChangeListeners();
        }
    };

    private final OnPositionChangeListener mOnPredecessorPositionChange = new OnPositionChangeListener() {
        @Override
        public void onPositionChange(@NonNull Point position) {
            Log.d(TAG, hashCode() + "'s predecessor moved to: " + position);
            moveToChainedPosition();
        }
    };

    public ChainedTab(@NonNull Context context) {
        super(context);
        mTabSize = getResources().getDimensionPixelSize(R.dimen.floating_icon_size);
        setBackgroundColor(0x88FF00FF);
        setVisibility(GONE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Make this View the desired size.
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
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

    @NonNull
    @Override
    public Point getPosition() {
        return new Point(
                ((int) getX() + (mTabSize / 2)),
                ((int) getY() + (mTabSize / 2))
        );
    }

    @NonNull
    public Point getDockPosition() {
        return null != mAnchorPosition ?
                mAnchorPosition :
                getPosition();
    }

    public void chainTo(@NonNull Tab tab) {
        chainTo(tab, null);
    }

    public void chainTo(@NonNull Tab tab, @Nullable final Runnable onChained) {
        if (null != mPredecessorTab) {
            mPredecessorTab.removeOnPositionChangeListener(mOnPredecessorPositionChange);
        }

        Log.d(TAG, hashCode() + " is now chained to " + tab.hashCode());
        mPredecessorTab = tab;
        moveToChainedPosition();
        mPredecessorTab.addOnPositionChangeListener(mOnPredecessorPositionChange);

        if (getVisibility() != VISIBLE) {
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(ChainedTab.this, "scaleX", 0.0f, 1.0f);
            scaleX.setDuration(250);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(ChainedTab.this, "scaleY", 0.0f, 1.0f);
            scaleY.setDuration(250);
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.start();


            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (null != onChained) {
                        onChained.run();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

            setVisibility(VISIBLE);
        }
    }

    private void moveToChainedPosition() {
        mAnchorPosition = getMyChainPositionRelativeTo(mPredecessorTab);

        if (VISIBLE == getVisibility()) {
            animateTo(mAnchorPosition);
        } else {
            moveTo(mAnchorPosition);
        }
    }

    private Point getMyChainPositionRelativeTo(@NonNull Tab tab) {
        Point predecessorTabPosition = tab.getDockPosition();
        Log.d(TAG, "Predecessor position: " + predecessorTabPosition);
        return new Point(
                predecessorTabPosition.x - 200, // TODO: configurable spacing
                predecessorTabPosition.y
        );
    }

    private void moveTo(@NonNull Point centerPosition) {
        Point cornerPosition = convertCenterToCorner(centerPosition);
        Log.d(TAG, "Setting my corner position to: " + cornerPosition);
        setX(cornerPosition.x);
        setY(cornerPosition.y);
    }

    private void animateTo(@NonNull Point centerPosition) {
        Point oldCornerPosition = convertCenterToCorner(getPosition());
        final Point newCornerPosition = convertCenterToCorner(centerPosition);

//        setX(newCornerPosition.x);
//        setY(newCornerPosition.y);
//        notifyOnPositionChangeListeners();

        Log.d(TAG, hashCode() + " animating my corner position to: " + newCornerPosition);
        ObjectAnimator slideX = ObjectAnimator.ofFloat(this, "translationX", oldCornerPosition.x, newCornerPosition.x);
        ObjectAnimator slideY = ObjectAnimator.ofFloat(this, "translationY", oldCornerPosition.y, newCornerPosition.y);
        AnimatorSet animation = new AnimatorSet();
        animation.playTogether(slideX, slideY);
        animation.setDuration(150);
        animation.start();
    }

    private Point convertCenterToCorner(@NonNull Point centerPosition) {
        return new Point(
                centerPosition.x - (mTabSize / 2),
                centerPosition.y - (mTabSize / 2)
        );
    }

    public void unchain() {
        unchain(null);
    }

    public void unchain(@Nullable final Runnable onUnchained) {
        mPredecessorTab.removeOnPositionChangeListener(mOnPredecessorPositionChange);

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ChainedTab.this, "scaleX", 0.0f);
        scaleX.setDuration(250);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ChainedTab.this, "scaleY", 0.0f);
        scaleY.setDuration(250);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);

                if (null != onUnchained) {
                    onUnchained.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
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
}
