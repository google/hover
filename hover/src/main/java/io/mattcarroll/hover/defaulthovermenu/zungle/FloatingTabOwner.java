package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.support.annotation.NonNull;

/**
 * TODO
 */
interface FloatingTabOwner {
    void takeControl(@NonNull FloatingTab floatingIcon);

    void giveControlTo(@NonNull FloatingTabOwner otherController);
}
