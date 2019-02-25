package io.mattcarroll.hover;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

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
    public void takeControl(@NonNull final HoverView hoverView, final Runnable onStateChanged) {
        super.takeControl(hoverView, onStateChanged);
        Log.d(TAG, "Taking control.");
        mHoverView.makeUntouchableInWindow();
        mHoverView.clearFocus();

        mSelectedSection = mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId);
        if (mSelectedSection == null) {
            mSelectedSection = mHoverView.mMenu.getSection(0);
        }
        mSelectedTab = mHoverView.mScreen.getChainedTab(mSelectedSection.getId());
        if (mSelectedTab == null) {
            mSelectedTab = mHoverView.mScreen.createChainedTab(mSelectedSection);
        }

        mSelectedTab.shrink();
        mSelectedTab.setSelected(true);
        final int anchorMarginX = hoverView.getContext().getResources().getDimensionPixelSize(R.dimen.hover_tab_anchor_margin_x) + (mSelectedTab.getTabSize() / 2);
        final int anchorMarginY = hoverView.getContext().getResources().getDimensionPixelSize(R.dimen.hover_tab_anchor_margin_y) + (mSelectedTab.getTabSize() / 2);
        final Point anchorPoint = mHoverView.getScreenSize();
        anchorPoint.offset(-anchorMarginX, -anchorMarginY);
        mSelectedTab.setDock(new PositionDock(anchorPoint));
        mSelectedTab.dock(new Runnable() {
            @Override
            public void run() {
                if (!hasControl() || !mHoverView.mIsAddedToWindow) {
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
        mSelectedTab.expand();
        mSelectedTab.setSelected(false);
        super.giveUpControl(nextState);
    }

    private void activateTouchController() {
        final ArrayList<View> list = new ArrayList<>();
        list.add(mSelectedTab);
        mHoverView.mDragger.activate(mTouchListener, list);
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
