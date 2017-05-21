package io.mattcarroll.hover.defaulthovermenu.ziggle;

import android.support.annotation.NonNull;

/**
 * TODO
 */
interface HoverMenuController {
    void takeControl(@NonNull HoverTab floatingIcon);

    void giveControlTo(@NonNull HoverMenuController otherController);
}
