package io.mattcarroll.hover.defaulthovermenu.menus.serialization;

import android.support.annotation.NonNull;

import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuAction;

/**
 * Creates {@link MenuAction}s given various action IDs.
 */
public interface MenuActionFactory {

    MenuAction createShowSubmenuMenuAction(@NonNull Menu menu);

    MenuAction createMenuActionForId(@NonNull String actionId);

}
