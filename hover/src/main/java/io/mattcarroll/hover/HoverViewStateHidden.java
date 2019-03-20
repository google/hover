package io.mattcarroll.hover;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

class HoverViewStateHidden extends BaseHoverViewState {

    private static final String TAG = "HoverViewStateHidden";

    private FloatingTab mSelectedTab;

    @Override
    public void takeControl(@NonNull final HoverView hoverView, final Runnable onStateChanged) {
        super.takeControl(hoverView, onStateChanged);
        Log.d(TAG, "Taking control.");
        mHoverView.makeUntouchableInWindow();
        mHoverView.clearFocus();

        HoverMenu.Section mSelectedSection = mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId);
        if (mSelectedSection == null) {
            mSelectedSection = mHoverView.mMenu.getSection(0);
        }

        mSelectedTab = mHoverView.mScreen.getChainedTab(mSelectedSection.getId());
        if (mSelectedTab == null) {
            mSelectedTab = mHoverView.mScreen.createChainedTab(mSelectedSection);
        }

        mSelectedTab.shrink();
        mSelectedTab.setSelected(true);

        final PositionDock positionToHide = mHoverView.getPositionToHide();
        if (positionToHide == null) {
            mHoverView.setVisibility(View.GONE);
            onStateChanged.run();
            return;
        }

        mSelectedTab.setDock(positionToHide);
        mSelectedTab.dock(new Runnable() {
            @Override
            public void run() {
                if (!hasControl() || !mHoverView.mIsAddedToWindow) {
                    return;
                }
                onStateChanged.run();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mHoverView != null) {
                            mHoverView.setVisibility(View.GONE);
                        }
                    }
                }, 50);
            }
        });
    }

    @Override
    public void giveUpControl(@NonNull HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        mSelectedTab.setSelected(false);
        mSelectedTab.expand();
        mHoverView.setVisibility(View.VISIBLE);
        super.giveUpControl(nextState);
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
        return HoverViewStateType.HIDDEN;
    }
}
