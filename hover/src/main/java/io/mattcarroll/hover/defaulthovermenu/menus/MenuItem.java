package io.mattcarroll.hover.defaulthovermenu.menus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a menu item that can act as a composite with submenu items.
 */
public class MenuItem {

    private final String mId;
    private final String mTitle;
    private final MenuAction mMenuAction;

    public MenuItem(@NonNull String id, @NonNull String title, @NonNull MenuAction menuAction) {
        mId = id;
        mTitle = title;
        mMenuAction = menuAction;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @NonNull
    public MenuAction getMenuAction() {
        return mMenuAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuItem menuItem = (MenuItem) o;

        return mId.equals(menuItem.mId);

    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

}
