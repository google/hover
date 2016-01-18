package io.mattcarroll.hover.defaulthovermenu.menus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Represents a menu item that can act as a composite with submenu items.
 */
public class MenuItem {

    private Type mType;
    private String mId;
    private String mTitle;
    private List<MenuItem> mItems;
    private MenuItem mParent;
    private String mPayload;

    public MenuItem(@NonNull Type type, @NonNull String id, @NonNull String title, @Nullable MenuItem parent) {
        this(type, id, title, parent, null);
    }

    public MenuItem(@NonNull Type type, @NonNull String id, @NonNull String title, @Nullable MenuItem parent, @Nullable String payload) {
        mType = type;
        mId = id;
        mTitle = title;
        mItems = new ArrayList<>();
        mParent = parent;
        mPayload = payload;
    }

    public String getId() {
        return mId;
    }

    public Type getType() {
        return mType;
    }

    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getPayload() {
        return mPayload;
    }

    public List<MenuItem> getItems() {
        return new ArrayList<>(mItems);
    }

    public void addItems(MenuItem... items) {
        mItems.addAll(Arrays.asList(items));
    }

    public void removeItems(MenuItem... items) {
        mItems.removeAll(Arrays.asList(items));
    }

    public MenuItem getParent() {
        return mParent;
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

    public enum Type {
        MENU,
        DO_ACTION,
        SHOW_VIEW,
        GENERATE_MENU
    }
}
