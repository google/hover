package io.mattcarroll.hover;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

public class TabMessageView extends HoverFrameLayout {
    private static final String TAG = "TabMessageView";

    private final FloatingTab mFloatingTab;
    private SideDock mSideDock;
    private View mMessageView;

    private final FloatingTab.OnFloatingTabChangeListener mOnFloatingTabChangeListener = new FloatingTab.OnFloatingTabChangeListener() {
        private static final int DEFAULT_SIDE = SideDock.SidePosition.LEFT;

        private Point mLastPosition;
        private int mLastSide;

        @Override
        public void onPositionChange(View view) {
            if (!(view instanceof FloatingTab)) {
                return;
            }
            final Point position = ((FloatingTab) view).getPosition();
            final Integer side = getSide();
            if (side.equals(mLastSide) && position.equals(mLastPosition) || getWidth() == 0) {
                return;
            }
            Log.d(TAG, mFloatingTab + " tab moved to " + position);
            final float tabSizeHalf = mFloatingTab.getTabSize() / 2f;
            if (side == SideDock.SidePosition.RIGHT) {
                setX(position.x - tabSizeHalf - getWidth());
            } else {
                setX(position.x + tabSizeHalf);
            }
            setY(position.y - tabSizeHalf);
            mLastPosition = position;
            mLastSide = side;
        }

        @Override
        public void onDockChange(@NonNull Dock dock) {
            if (dock instanceof SideDock) {
                final SideDock sideDock = (SideDock) dock;
                if (sideDock.sidePosition() != mSideDock.sidePosition()) {
                    appear(sideDock, null);
                }
            }
        }

        private int getSide() {
            if (mSideDock != null) {
                return mSideDock.sidePosition().getSide();
            }
            return DEFAULT_SIDE;
        }
    };

    public TabMessageView(@NonNull Context context, @NonNull FloatingTab floatingTab) {
        super(context);
        mFloatingTab = floatingTab;
        setVisibility(GONE);

        // To prevent child's shadow clipping
        setClipToPadding(false);
        setClipChildren(false);
        setPadding(10, 20, 10, 20);
    }

    public void setMessageView(@Nullable View view) {
        if (view == mMessageView) {
            return;
        }
        removeAllViews();
        mMessageView = view;
        if (mMessageView != null) {
            addView(mMessageView);
        }
    }

    @Nullable
    public View getMessageView() {
        return mMessageView;
    }

    public void appear(final SideDock dock, @Nullable final Runnable onAppeared) {
        mSideDock = dock;
        mFloatingTab.addOnPositionChangeListener(mOnFloatingTabChangeListener);
        if (getVisibility() != View.VISIBLE) {
            final AnimationSet animation = new AnimationSet(true);
            final AlphaAnimation alpha = new AlphaAnimation(0, 1);
            final float fromXDelta = getResources().getDimensionPixelSize(R.dimen.hover_message_animate_translation_x)
                    * (dock.sidePosition().getSide() == SideDock.SidePosition.LEFT ? -1 : 1);
            final float fromYDelta = getResources().getDimensionPixelSize(R.dimen.hover_message_animate_translation_y);
            TranslateAnimation translate = new TranslateAnimation(fromXDelta, 0, fromYDelta, 0);
            animation.setDuration(300);
            animation.setInterpolator(new LinearOutSlowInInterpolator());
            animation.addAnimation(alpha);
            animation.addAnimation(translate);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (onAppeared != null) {
                        onAppeared.run();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            startAnimation(animation);
            setVisibility(VISIBLE);
        }
    }

    public void disappear(final boolean withAnimation) {
        disappear(withAnimation, 1);
    }

    public void disappear(final boolean withAnimation, float startAlpha) {
        mFloatingTab.removeOnPositionChangeListener(mOnFloatingTabChangeListener);
        mSideDock = null;
        if (withAnimation && getVisibility() == View.VISIBLE) {
            final AnimationSet animation = new AnimationSet(true);
            final AlphaAnimation alpha = new AlphaAnimation(startAlpha, 0);
            alpha.setDuration(300);
            animation.addAnimation(alpha);
            startAnimation(animation);
        }
        setVisibility(GONE);
    }

    public void moveCenterTo(@NonNull Point floatPosition) {
        Point cornerPosition = convertCenterToCorner(floatPosition);
        setX(cornerPosition.x);
        setY(cornerPosition.y);
        notifyListenersOfPositionChange(this);
    }

    private Point convertCenterToCorner(@NonNull Point centerPosition) {
        return new Point(
                (int) (centerPosition.x - (getWidth() / 2)),
                (int) (centerPosition.y - (getHeight() / 2))
        );
    }
}
