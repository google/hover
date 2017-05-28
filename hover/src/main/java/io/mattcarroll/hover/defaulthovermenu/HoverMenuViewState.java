package io.mattcarroll.hover.defaulthovermenu;

import android.support.annotation.NonNull;

/**
 * TODO
 */
interface HoverMenuViewState {
    void takeControl(@NonNull Screen screen, @NonNull String primaryTabId);

    void giveControlTo(@NonNull HoverMenuViewState otherController);
}
