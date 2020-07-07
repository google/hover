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
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import io.mattcarroll.hover.physics.AnimationViewWrapper;
import io.mattcarroll.hover.physics.FlingViewWrapper;
import io.mattcarroll.hover.utils.PointUtils;
import io.mattcarroll.hover.utils.ViewUtils;

/**
 * Fullscreen View that provides an exit "drop zone" for users to exit the Hover Menu.
 */
class ExitView extends RelativeLayout {

    private static final String TAG = "ExitView";
    private static final int MAX_MOVEMENT = 150;

    private int mExitRadiusInPx;
    private CloseImageView mExitIcon;
    private View mViewExitGradient;
    private FlingViewWrapper mAnimationViewWrapper;

    private float mMaxX;
    private float mMaxY;
    private float mMinX;
    private float mMinY;

    private boolean mIsTouchInExitView = false;
    private Point mLastPosition = new Point(0, 0);

    public ExitView(@NonNull Context context) {
        this(context, null);
    }

    public ExitView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_exit, this, true);
        mExitIcon = findViewById(R.id.view_exit);
        mViewExitGradient = findViewById(R.id.view_exit_gradient);
        mExitRadiusInPx = getResources().getDimensionPixelSize(R.dimen.hover_exit_radius);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int margin = (int) getContext().getResources().getDimension(R.dimen.hover_exit_margin_bottom);
        mMaxY = getMeasuredHeight() + margin;
        mMinY = getMeasuredHeight() - 0.7f * margin;
        mMaxX = getMeasuredWidth() / 2f + margin;
        mMinX = getMeasuredWidth() / 2f - margin;
    }

    public void prepareExit(Point point) {
        if (getVisibility() == VISIBLE) return;

        onCreateAnimationViewWrapper();

        mExitIcon.setX(getInitializePoint().x);
        mExitIcon.setY(mMinY);
        mIsTouchInExitView = false;

        mViewExitGradient.setVisibility(VISIBLE);
        setVisibility(VISIBLE);
        receiveTabPosition(point);
    }

    private void onCreateAnimationViewWrapper() {
        mAnimationViewWrapper = new FlingViewWrapper(mExitIcon)
                .setBoundValue(mMaxX, mMaxY, mMinX, mMinY - 2 * mExitIcon.getMeasuredHeight())
                .setFriction(0.5f)
                .setScaleVelocity(6f);
        mAnimationViewWrapper.onStart();
    }

    private PointF getInitializePoint() {
        return new PointF(getMeasuredWidth() / 2f - mExitIcon.getMeasuredWidth() * mExitIcon.getScaleX() / 2f,
                mMaxY - mExitIcon.getMeasuredHeight() * mExitIcon.getScaleY() / 2f);
    }

    private void updateFlingViewWrapperPosition(Point position) {
        if (mAnimationViewWrapper == null) return;

        float x = ((position.x - getMeasuredWidth() / 2.0f) * 1.0f / getMeasuredWidth()) * 1.5f * MAX_MOVEMENT;
        float y = ((mMinY - position.y) * 1.0f / getMeasuredHeight()) * MAX_MOVEMENT;

        mAnimationViewWrapper.updatePosition(new Point(
                (int) (x + (getMeasuredWidth() - mExitIcon.getMeasuredWidth()) / 2f),
                (int) (mMinY + MAX_MOVEMENT - y - mExitIcon.getMeasuredHeight() / 2f)
        ));
    }

    public boolean receiveTabPosition(Point position) {
        if (mIsTouchInExitView && calculateDistance(mLastPosition, position) < 5f)
            return mIsTouchInExitView;
        mLastPosition = position;

        if (!mIsTouchInExitView) {
            updateFlingViewWrapperPosition(position);
        }

        if (!mIsTouchInExitView && isInExitZone(position)) {
            moveInExitZone();
        } else if (mIsTouchInExitView && !isInExitZone(position)) {
            moveOutExitZone();
        }

        return mIsTouchInExitView;
    }

    private void moveInExitZone() {
        ViewUtils.scale(mExitIcon, 1.35f);
        mAnimationViewWrapper.onPause();
        mIsTouchInExitView = true;
        mExitIcon.highlight();
    }

    private void moveOutExitZone() {
        ViewUtils.scale(mExitIcon, 1f);
        mAnimationViewWrapper.onResume();
        mIsTouchInExitView = false;
        mExitIcon.unhighlight();
        retrieveExitIcon();
    }

    public Point getExitViewPosition() {
        return new Point((int) mExitIcon.getX() + mExitIcon.getMeasuredWidth() / 2,
                (int) mExitIcon.getY() + mExitIcon.getMeasuredHeight() / 2);
    }

    public void bringExitIconToFront(ViewGroup container) {
        ((ViewGroup) mExitIcon.getParent()).removeView(mExitIcon);
        container.addView(mExitIcon);
    }

    private void retrieveExitIcon() {
        ViewParent viewParent = mExitIcon.getParent();
        ((ViewGroup) viewParent).removeView(mExitIcon);
        addView(mExitIcon);
    }

    public void releaseExit() {
        retrieveExitIcon();
        setVisibility(GONE);
        if (mAnimationViewWrapper != null) {
            mAnimationViewWrapper.onDestroy();
        }
    }

    public void releaseExit(Runnable onFinished) {
        releaseExit(new ArrayList<View>(), onFinished);
    }

    public <T extends View> void releaseExit(final List<T> followers, final Runnable onFinished) {
        mAnimationViewWrapper.setOnActionListener(new AnimationViewWrapper.OnActionListener() {
            int mX = (int) mExitIcon.getX();
            int mY = (int) mExitIcon.getY();

            @Override
            public void onMoveToX(int x) {
                following(x, mY);
                mX = x;
            }

            @Override
            public void onMoveToY(int y) {
                following(mX, y);
                mY = y;
            }

            @Override
            public void onEndX() {
                // No-op
            }

            @Override
            public void onEndY() {
                setVisibility(GONE);
                mAnimationViewWrapper.setOnActionListener(null);
                if (mAnimationViewWrapper != null) {
                    mAnimationViewWrapper.onDestroy();
                }
                moveOutExitZone();
                if (onFinished != null) {
                    onFinished.run();
                }
            }

            private void following(int x, int y) {
                if (mX == y && mY == y) return;
                for (int i = 0; i < followers.size(); i++) {
                    followers.get(i).setX(x);
                    followers.get(i).setY(y);
                }
            }
        });

        mViewExitGradient.setVisibility(GONE);
        mAnimationViewWrapper.setFriction(1f);
        mAnimationViewWrapper.setScaleVelocity(10f);
        if (mAnimationViewWrapper.isPause()) {
            mAnimationViewWrapper.onResume();
        }
        mAnimationViewWrapper.updatePosition(PointUtils.parse(getInitializePoint()));
    }

    public boolean isInExitZone(@NonNull Point position) {
        Point exitCenter = getExitZoneCenter();
        double distanceToExit = calculateDistance(position, exitCenter);
        return distanceToExit <= mExitRadiusInPx;
    }

    private Point getExitZoneCenter() {
        return new Point(
                (int) (mExitIcon.getX() + (mExitIcon.getWidth() / 2)),
                (int) (mExitIcon.getY() + (mExitIcon.getHeight() / 2))
        );
    }

    private double calculateDistance(@NonNull Point p1, @NonNull Point p2) {
        return Math.sqrt(
                Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2)
        );
    }
}
