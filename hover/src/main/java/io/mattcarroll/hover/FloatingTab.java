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
 * TODO
 */
class FloatingTab extends FrameLayout implements Tab {

    private static final String TAG = "FloatingTab";

    private final String mId;
    private int mTabSize;
    private View mTabView;
    private Point mDock;
    private final Set<OnPositionChangeListener> mOnPositionChangeListeners = new CopyOnWriteArraySet<OnPositionChangeListener>();

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
    @Override
    public String getTabId() {
        return mId;
    }

    public int getTabSize() {
        return mTabSize;
    }

    @Override
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
//                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
//            layoutParams.gravity = Gravity.CENTER;
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
    @Override
    public Point getDockPosition() {
        return mDock;
    }

    public void setDockPosition(@NonNull Point dock) {
        mDock = dock;
        notifyListenersOfDockChange();
    }

    public void dockTo(@NonNull Point dock) {
        dockTo(dock, null);
    }

    public void dockTo(@NonNull Point dock, @Nullable final Runnable onDocked) {
        // TODO: the dock can be changed independent of position, so we can't ignore the incoming
        // TODO: dock just because it matches our existing one.  figure out a way to not do needless
        // TODO: dock animations if the same/similar value is provided.  Should probably have a
        // TODO: dockTo(newDock) like this, but also a sendToDock() or maybe just dock().

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

        xAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                notifyListenersOfPositionChange();
            }
        });
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

    @Override
    public void addOnPositionChangeListener(@Nullable OnPositionChangeListener listener) {
        mOnPositionChangeListeners.add(listener);
    }

    @Override
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
            listener.onDockChange(mDock);
        }
    }

    // This method is declared in this class simply to make it clear that its part of our public
    // contract and not just an inherited method.
    public void setOnClickListener(@Nullable View.OnClickListener onClickListener) {
        super.setOnClickListener(onClickListener);
    }
}
