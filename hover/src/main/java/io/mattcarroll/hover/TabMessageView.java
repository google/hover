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

    private final OnPositionChangeListener mOnTabPositionChangeListener = new OnPositionChangeListener() {
        @Override
        public void onPositionChange(@NonNull View view) {
            final Point position = mFloatingTab.getPosition();
            Log.d(TAG, mFloatingTab + " tab moved to " + position);
            final float tabSizeHalf = mFloatingTab.getTabSize() / 2f;
            if (mSideDock != null && mSideDock.sidePosition().getSide() == SideDock.SidePosition.RIGHT) {
                setX(position.x - tabSizeHalf - getWidth());
                setY(position.y - tabSizeHalf);
            } else {
                setX(position.x + tabSizeHalf);
                setY(position.y - tabSizeHalf);
            }
            notifyListenersOfPositionChange(TabMessageView.this);
        }

        @Override
        public void onDockChange(@NonNull Dock dock) {
            if (dock instanceof SideDock) {
                final SideDock sideDock = (SideDock) dock;
                if (sideDock.sidePosition() != mSideDock.sidePosition()) {
                    appear(sideDock, null);
                }
            }
            notifyListenersOfDockChange(dock);
        }
    };

    public TabMessageView(@NonNull Context context, @NonNull View messageView, @NonNull FloatingTab floatingTab) {
        super(context);
        setClipToPadding(false);
        setClipChildren(false);
        mFloatingTab = floatingTab;
        addView(messageView);
        setVisibility(GONE);
    }

    public void appear(final SideDock dock, @Nullable final Runnable onAppeared) {
        mSideDock = dock;
        mFloatingTab.addOnPositionChangeListener(mOnTabPositionChangeListener);
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

    public void disappear(final boolean withAnimation) {
        mFloatingTab.removeOnPositionChangeListener(mOnTabPositionChangeListener);
        mSideDock = null;
        if (withAnimation) {
            final AnimationSet animation = new AnimationSet(true);
            final AlphaAnimation alpha = new AlphaAnimation(1, 0);
            alpha.setDuration(300);
            animation.addAnimation(alpha);
            startAnimation(animation);
        }
        setVisibility(GONE);
    }
}
