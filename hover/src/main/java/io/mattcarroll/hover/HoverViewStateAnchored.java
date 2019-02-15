package io.mattcarroll.hover;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.Log;

public class HoverViewStateAnchored extends BaseHoverViewState {

    private static final String TAG = "HoverViewStateAnchored";
    private static final int ANCHOR_TAB_X_OFFSET_IN_PX = 100;
    private static final int ANCHOR_TAB_Y_OFFSET_IN_PX = 100;

    private FloatingTab mSelectedTab;
    private Point mDock;

    @Override
    public void takeControl(@NonNull HoverView hoverView, final Runnable onStateChanged) {
        super.takeControl(hoverView, onStateChanged);
        Log.d(TAG, "Taking control.");
        mHoverView.makeUntouchableInWindow();
        mHoverView.clearFocus();
        mDock = new Point(
                mHoverView.mScreen.getWidth() - ANCHOR_TAB_X_OFFSET_IN_PX,
                ANCHOR_TAB_Y_OFFSET_IN_PX
        );

        HoverMenu.Section section = mHoverView.mMenu.getSection(0);
        mSelectedTab = mHoverView.mScreen.createChainedTab(section);
        mSelectedTab.setDock(new PositionDock(mDock));
        mSelectedTab.dock(new Runnable() {
            @Override
            public void run() {
                if (!hasControl()) {
                    return;
                }
                onStateChanged.run();
            }
        });
    }

    @Override
    public void giveUpControl(@NonNull HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
        super.giveUpControl(nextState);
    }

    @Override
    public boolean respondsToBackButton() {
        return false;
    }

    @Override
    public void onBackPressed() {
    }
}
