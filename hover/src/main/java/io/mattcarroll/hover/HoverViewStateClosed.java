package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.view.View.GONE;

/**
 * TODO
 */
class HoverViewStateClosed extends BaseHoverViewState {

    private static final String TAG = "HoverMenuViewStateClosed";

    private HoverView mHoverView;

    @Override
    public void takeControl(@NonNull HoverView hoverView) {
        Log.d(TAG, "Taking control.");
        super.takeControl(hoverView);
        mHoverView = hoverView;
        mHoverView.mState = this;
        mHoverView.clearFocus();
        mHoverView.mScreen.getContentDisplay().setVisibility(GONE);

        String selectedSectionId = null != mHoverView.mSelectedSectionId
                ? mHoverView.mSelectedSectionId.toString()
                : null;
        final FloatingTab primaryTab = mHoverView.mScreen.getChainedTab(selectedSectionId);
        if (null != primaryTab) {
            primaryTab.disappear(new Runnable() {
                @Override
                public void run() {
                    mHoverView.mScreen.destroyChainedTab(primaryTab);
                }
            });
        }

        mHoverView.makeUntouchableInWindow();
    }

    private void changeState(@NonNull HoverViewState nextState) {
        mHoverView.setState(nextState);
        mHoverView = null;
    }

    @Override
    public void expand() {
        if (null != mHoverView.mMenu) {
            Log.d(TAG, "Expanding.");
            changeState(mHoverView.mExpanded);
        } else {
            Log.d(TAG, "Asked to expand, but there is no menu set. Can't expand until a menu is available.");
        }
    }

    @Override
    public void collapse() {
        if (null != mHoverView.mMenu) {
            Log.d(TAG, "Collapsing.");
            changeState(mHoverView.mCollapsed);
        } else {
            Log.d(TAG, "Asked to collapse, but there is no menu set. Can't collapse until a menu is available.");
        }
    }

    @Override
    public void close() {
        Log.d(TAG, "Instructed to close, but Hover is already closed.");
    }

    @Override
    public void setMenu(@Nullable final HoverMenu menu) {
        mHoverView.mMenu = menu;

        // If the menu is null then there is nothing to restore.
        if (null == menu) {
            return;
        }

        mHoverView.restoreVisualState();

        if (null == mHoverView.mSelectedSectionId || null == mHoverView.mMenu.getSection(mHoverView.mSelectedSectionId)) {
            mHoverView.mSelectedSectionId = mHoverView.mMenu.getSection(0).getId();
        }
    }

    @Override
    public boolean respondsToBackButton() {
        return false;
    }

    @Override
    public void onBackPressed() {
        // No-op
    }
}
