package io.mattcarroll.hover.defaulthovermenu.menus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import io.mattcarroll.hover.Navigator;

/**
 * {@link MenuAction} that displays a submenu in a given {@link Navigator}.
 */
public class ShowSubmenuMenuAction implements MenuAction {

    private final Menu mMenu;
    private final View mEmptyView;
    private MenuListNavigatorContent mNavigatorContent;

    public ShowSubmenuMenuAction(@NonNull Menu menu) {
        this(menu, null);
    }

    public ShowSubmenuMenuAction(@NonNull Menu menu, @Nullable View emptyView) {
        mMenu = menu;
        mEmptyView = emptyView;
    }

    @Override
    public void execute(@NonNull Context context, @NonNull Navigator navigator) {
        if (null == mNavigatorContent) {
            // This is our first time being activated. Create our menu display.
            mNavigatorContent = new MenuListNavigatorContent(context, mMenu, mEmptyView);
        }

        navigator.pushContent(mNavigatorContent);
    }

}
