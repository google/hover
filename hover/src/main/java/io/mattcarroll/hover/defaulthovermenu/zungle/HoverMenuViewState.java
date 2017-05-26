package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.support.annotation.NonNull;

/**
 * TODO
 */
interface HoverMenuViewState {
    void takeControl(@NonNull Screen screen);

    void giveControlTo(@NonNull HoverMenuViewState otherController);
}
