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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import io.mattcarroll.hover.utils.ViewUtils;

/**
 * Fullscreen View that provides an exit "drop zone" for users to exit the Hover Menu.
 */
class ExitView extends RelativeLayout {

    private static final String TAG = "ExitView";
    private static final int MAX_MOVEMENT = 100;

    private int mExitRadiusInPx;
    private CloseImageView mExitIcon;

    private int mYBelow;
    private int mYAbove;

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

        mExitRadiusInPx = getResources().getDimensionPixelSize(R.dimen.hover_exit_radius);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int marginBottom = (int) getContext().getResources().getDimension(R.dimen.hover_exit_margin_bottom);
        mYBelow = getMeasuredHeight() + marginBottom;
        mYAbove = getMeasuredHeight() - marginBottom;
    }

    public void prepareExit(Point point) {
        setVisibility(VISIBLE);
        receiveTabPosition(point);
    }

    private boolean mIsTouchInExitView = false;
    private Point mLastPosition = new Point(0, 0);

    public boolean receiveTabPosition(Point position) {
        if (mIsTouchInExitView && calculateDistance(mLastPosition, position) < 10f)
            return mIsTouchInExitView;
        mLastPosition = position;

        float x = ((position.x - getMeasuredWidth() / 2.0f) * 1.0f / getMeasuredWidth()) * MAX_MOVEMENT;
        float y = ((position.y - mYAbove) * 1.0f / getMeasuredHeight() * 1.0f) * MAX_MOVEMENT;

        if (!mIsTouchInExitView) {
            mExitIcon.setX(x + (getMeasuredWidth() - mExitIcon.getMeasuredWidth()) / 2f);
            mExitIcon.setY(mYAbove + y - mExitIcon.getMeasuredHeight() / 2f);
        }

        if (!mIsTouchInExitView && isInExitZone(position)) {
            ViewUtils.scale(mExitIcon, 1.65f);

            mIsTouchInExitView = true;
            mExitIcon.highlight();

        } else if (mIsTouchInExitView && !isInExitZone(position)) {
            ViewUtils.scale(mExitIcon, 1f);

            mIsTouchInExitView = false;
            mExitIcon.unhighlight();

            retrieveExitIcon();
        }

        return mIsTouchInExitView;
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
        ViewParent viewParent = mExitIcon.getParent();
        ((ViewGroup) viewParent).removeView(mExitIcon);
        addView(mExitIcon);
        setVisibility(GONE);
    }

    public boolean isInExitZone(@NonNull Point position) {
        Point exitCenter = getExitZoneCenter();
        double distanceToExit = calculateDistance(position, exitCenter);
        Log.d(TAG, "Drop point: " + position + ", Exit center: " + exitCenter + ", Distance: " + distanceToExit);
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
