package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.view.View.GONE;

/**
 * TODO
 */
class HoverMenuViewStateClosed extends BaseHoverMenuViewState {

    private static final String TAG = "HoverMenuViewStateClosed";

    private HoverMenuView mHoverMenuView;

    @Override
    public void takeControl(@NonNull HoverMenuView hoverMenuView) {
        Log.d(TAG, "Taking control.");
        super.takeControl(hoverMenuView);
        mHoverMenuView = hoverMenuView;
        mHoverMenuView.mState = this;
        mHoverMenuView.clearFocus();
        mHoverMenuView.mScreen.getContentDisplay().setVisibility(GONE);

        String selectedSectionId = null != mHoverMenuView.mSelectedSectionId
                ? mHoverMenuView.mSelectedSectionId.toString()
                : null;
        final FloatingTab primaryTab = mHoverMenuView.mScreen.getChainedTab(selectedSectionId);
        if (null != primaryTab) {
            primaryTab.disappear(new Runnable() {
                @Override
                public void run() {
                    mHoverMenuView.mScreen.destroyChainedTab(primaryTab);
                }
            });
        }

        mHoverMenuView.makeUntouchableInWindow();
    }

    private void changeState(@NonNull HoverMenuViewState nextState) {
        mHoverMenuView.setState(nextState);
        mHoverMenuView = null;
    }

    @Override
    public void expand() {
        if (null != mHoverMenuView.mMenu) {
            Log.d(TAG, "Expanding.");
            changeState(mHoverMenuView.mExpanded);
        } else {
            Log.d(TAG, "Asked to expand, but there is no menu set. Can't expand until a menu is available.");
        }
    }

    @Override
    public void collapse() {
        if (null != mHoverMenuView.mMenu) {
            Log.d(TAG, "Collapsing.");
            changeState(mHoverMenuView.mCollapsed);
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
        mHoverMenuView.mMenu = menu;

        // If the menu is null then there is nothing to restore.
        if (null == menu) {
            return;
        }

        mHoverMenuView.restoreVisualState();

        if (null == mHoverMenuView.mSelectedSectionId || null == mHoverMenuView.mMenu.getSection(mHoverMenuView.mSelectedSectionId)) {
            mHoverMenuView.mSelectedSectionId = mHoverMenuView.mMenu.getSection(0).getId();
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
