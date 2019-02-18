package io.mattcarroll.hover;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

class HoverViewStateAnchored extends BaseHoverViewState {

    private static final String TAG = "HoverViewStateAnchored";

    private FloatingTab mSelectedTab;
    private HoverMenu.Section mSelectedSection;
    private final BaseTouchController.TouchListener mTouchListener = new BaseTouchController.TouchListener() {
        @Override
        public void onPress() {
        }

        @Override
        public void onTap() {
            mHoverView.notifyOnTap(HoverViewStateAnchored.this);
        }
    };

    @Override
    public void takeControl(@NonNull HoverView hoverView, final Runnable onStateChanged) {
        super.takeControl(hoverView, onStateChanged);
        Log.d(TAG, "Taking control.");
        mHoverView.makeUntouchableInWindow();
        mHoverView.clearFocus();
        final int pointMargin = hoverView.getContext().getResources().getDimensionPixelSize(R.dimen.hover_tab_anchor_margin);
        final Point anchorPoint = new Point(
                mHoverView.mScreen.getWidth() - pointMargin,
                mHoverView.mScreen.getHeight() - pointMargin
        );

        mSelectedSection = mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId);
        if (mSelectedSection == null) {
            mSelectedSection = mHoverView.mMenu.getSection(0);
        }
        mSelectedTab = mHoverView.mScreen.getChainedTab(mSelectedSection.getId());
        if (mSelectedTab == null) {
            mSelectedTab = mHoverView.mScreen.createChainedTab(mSelectedSection);
        }
        mSelectedTab.setDock(new PositionDock(anchorPoint));
        mSelectedTab.dock(new Runnable() {
            @Override
            public void run() {
                if (!hasControl()) {
                    return;
                }
                activateTouchController();
                onStateChanged.run();
            }
        });
    }

    @Override
    public void giveUpControl(@NonNull HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        deactivateTouchController();
        super.giveUpControl(nextState);
    }

    private void activateTouchController() {
        final Rect visibleRect = new Rect();
        mSelectedTab.getGlobalVisibleRect(visibleRect);
        mHoverView.mDragger.activate(mTouchListener, visibleRect);
    }

    private void deactivateTouchController() {
        mHoverView.mDragger.deactivate();
    }

    @Override
    public boolean respondsToBackButton() {
        return false;
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public HoverViewStateType getStateType() {
        return HoverViewStateType.ANCHORED;
    }
}
