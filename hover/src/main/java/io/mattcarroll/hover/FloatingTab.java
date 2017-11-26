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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * {@code FloatingTab} is the cornerstone of a {@link HoverView}.  When a {@code HoverView} is
 * collapsed, it is reduced to a single {@code FloatingTab} that the user can drag and drop.  When
 * a {@code HoverView} is expanded, that one {@code FloatingTab} slides to a row of tabs that appear
 * and offer a menu system.
 *
 * A {@code FloatingTab} can move around the screen in various ways. A {@code FloatingTab} can place
 * itself at a "dock position", or slide from its current position to its "dock position", or
 * position itself at an arbitrary location on screen.
 *
 * {@code FloatingTab}s position themselves based on their center.
 */
class FloatingTab extends FrameLayout {

    private static final String TAG = "FloatingTab";

    private final String mId;
    private int mTabSize;
    private View mTabView;
    private Dock mDock;
    private ObjectAnimator mXAnimation;
    private ObjectAnimator mYAnimation;
    private final Set<OnPositionChangeListener> mOnPositionChangeListeners = new CopyOnWriteArraySet<>();

    private final OnLayoutChangeListener mOnLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            notifyListenersOfPositionChange();
        }
    };

    public FloatingTab(@NonNull Context context, @NonNull String tabId) {
        super(context);
        mId = tabId;
        mTabSize = getResources().getDimensionPixelSize(R.dimen.hover_tab_size);

        int padding = getResources().getDimensionPixelSize(R.dimen.hover_tab_margin);
        setPadding(padding, padding, padding, padding);
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

    public void enableDebugMode(boolean debugMode) {
        if (debugMode) {
            setBackgroundColor(0x8800FF00);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void appear(@Nullable final Runnable onAppeared) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.0f, 1.0f);
        scaleX.setDuration(250);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.0f, 1.0f);
        scaleY.setDuration(250);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (null != onAppeared) {
                    onAppeared.run();
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

    public void appearImmediate() {
        setVisibility(VISIBLE);
    }

    public void disappear(@Nullable final Runnable onDisappeared) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.0f);
        scaleX.setDuration(250);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.0f);
        scaleY.setDuration(250);
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);

                if (null != onDisappeared) {
                    onDisappeared.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
    }

    public void disappearImmediate() {
        setVisibility(GONE);
    }

    @NonNull
    public String getTabId() {
        return mId;
    }

    public int getTabSize() {
        return mTabSize;
    }

    public void setTabView(@Nullable View view) {
        if (view == mTabView) {
            // If Tab View hasn't changed, no need to do anything.
            return;
        }

        removeAllViews();

        mTabView = view;
        if (null != mTabView) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            addView(mTabView, layoutParams);
        }
    }

    // Returns the center position of this tab.
    @NonNull
    public Point getPosition() {
        return new Point(
                (int) (getX() + (getTabSize() / 2)),
                (int) (getY() + (getTabSize() / 2))
        );
    }

    @Nullable
    public Point getDockPosition() {
        return mDock.position();
    }

    public void setDock(@NonNull Dock dock) {
        mDock = dock;
        notifyListenersOfDockChange();
    }

    public void dock() {
        dock(null);
    }

    public void dock(@Nullable final Runnable onDocked) {
        Point destinationCornerPosition = convertCenterToCorner(mDock.position());
        Log.d(TAG, "Docking to destination point: " + destinationCornerPosition);

        if (null != mXAnimation) {
            mXAnimation.cancel();
        }
        mXAnimation = ObjectAnimator.ofFloat(this, "x", destinationCornerPosition.x);
        mXAnimation.setDuration(500);
        mXAnimation.setInterpolator(new OvershootInterpolator());

        if (null != mYAnimation) {
            mYAnimation.cancel();
        }
        mYAnimation = ObjectAnimator.ofFloat(this, "y", destinationCornerPosition.y);
        mYAnimation.setDuration(500);
        mYAnimation.setInterpolator(new OvershootInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(mXAnimation).with(mYAnimation);
        animatorSet.start();

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (null != onDocked) {
                    onDocked.run();
                }
                notifyListenersOfPositionChange();
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });

        mXAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                notifyListenersOfPositionChange();
            }
        });
    }

    public void dockImmediately() {
        moveTo(mDock.position());
    }

    public void moveTo(@NonNull Point floatPosition) {
        Point cornerPosition = convertCenterToCorner(floatPosition);
        setX(cornerPosition.x);
        setY(cornerPosition.y);
    }

    private Point convertCenterToCorner(@NonNull Point centerPosition) {
        return new Point(
                centerPosition.x - (getTabSize() / 2),
                centerPosition.y - (getTabSize() / 2)
        );
    }

    public void addOnPositionChangeListener(@Nullable OnPositionChangeListener listener) {
        mOnPositionChangeListeners.add(listener);
    }

    public void removeOnPositionChangeListener(@NonNull OnPositionChangeListener listener) {
        mOnPositionChangeListeners.remove(listener);
    }

    private void notifyListenersOfPositionChange() {
        Point position = getPosition();
        for (OnPositionChangeListener listener : mOnPositionChangeListeners) {
            listener.onPositionChange(position);
        }
    }

    private void notifyListenersOfDockChange() {
        for (OnPositionChangeListener listener : mOnPositionChangeListeners) {
            listener.onDockChange(mDock.position());
        }
    }

    // This method is declared in this class simply to make it clear that its part of our public
    // contract and not just an inherited method.
    public void setOnClickListener(@Nullable View.OnClickListener onClickListener) {
        super.setOnClickListener(onClickListener);
    }

    public interface OnPositionChangeListener {
        void onPositionChange(@NonNull Point tabPosition);

        void onDockChange(@NonNull Point dockPosition);
    }
}
