package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * TODO
 */
public class HoverMenuViewStateClosed implements HoverMenuViewState {

    private static final String TAG = "HoverMenuViewStateClosed";

    private Screen mScreen;
    private String mPrimaryTabId;

    @Override
    public void takeControl(@NonNull Screen screen, @NonNull String primaryTabId) {
        Log.d(TAG, "Taking control. Primary tab ID: " + primaryTabId);
        mScreen = screen;
        mPrimaryTabId = primaryTabId;

        final FloatingTab primaryTab = mScreen.getChainedTab(primaryTabId);
        if (null != primaryTab) {
            primaryTab.disappear(new Runnable() {
                @Override
                public void run() {
                    mScreen.destroyChainedTab(primaryTab);
                }
            });
        }
    }

    @Override
    public void giveControlTo(@NonNull HoverMenuViewState otherController) {
        Log.d(TAG, "Giving control to " + otherController);
        otherController.takeControl(mScreen, mPrimaryTabId);
    }
}
