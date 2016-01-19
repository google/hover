package io.mattcarroll.hover.defaulthovermenu.menus;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * A {@code Menu} contains {@link MenuItem}s.
 */
public class Menu {

    private final String mTitle;
    private final List<MenuItem> mMenuItemList;

    public Menu(@NonNull String title, @NonNull List<MenuItem> menuItemList) {
        mTitle = title;
        mMenuItemList = menuItemList;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @NonNull
    public List<MenuItem> getMenuItemList() {
        return mMenuItemList;
    }

}
