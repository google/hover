package io.mattcarroll.hover;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

class HoverViewStateHidden extends BaseHoverViewState {

    private static final String TAG = "HoverViewStateHidden";

    @Override
    public void takeControl(@NonNull final HoverView hoverView, final Runnable onStateChanged) {
        super.takeControl(hoverView, onStateChanged);
        Log.d(TAG, "Taking control.");
        mHoverView.makeUntouchableInWindow();
        mHoverView.setVisibility(View.GONE);
    }

    @Override
    public void giveUpControl(@NonNull HoverViewState nextState) {
        Log.d(TAG, "Giving up control.");
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
