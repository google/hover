package io.mattcarroll.hover.hoverdemo.menu.config.file;

import android.content.Context;
import android.support.annotation.NonNull;

import io.mattcarroll.hover.defaulthovermenu.menus.DoNothingMenuAction;
import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuAction;
import io.mattcarroll.hover.defaulthovermenu.menus.ShowSubmenuMenuAction;
import io.mattcarroll.hover.defaulthovermenu.menus.serialization.MenuActionFactory;
import io.mattcarroll.hover.hoverdemo.menu.ui.EmptyListView;

/**
 * Creates {@link MenuAction}s based on IDs.
 */
public class DemoMenuActionFactory implements MenuActionFactory {

    private final Context mContext;

    public DemoMenuActionFactory(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public MenuAction createShowSubmenuMenuAction(@NonNull Menu menu) {
        return new ShowSubmenuMenuAction(menu, new EmptyListView(mContext));
    }

    @Override
    public MenuAction createMenuActionForId(@NonNull String actionId) {
        return new DoNothingMenuAction();
    }
}
