package io.mattcarroll.hover.defaulthovermenu.menus;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that displays a {@link Menu} using {@link MenuItemView}s.
 */
public class MenuListAdapter extends BaseAdapter {

    private Menu mMenu;

    public void setMenu(@Nullable Menu menu) {
        mMenu = menu;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return null == mMenu ? 0 : mMenu.getMenuItemList().size();
    }

    @Override
    public MenuItem getItem(int index) {
        return mMenu.getMenuItemList().get(index);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (null == view) {
            view = new MenuItemView(viewGroup.getContext());
        }

        ((MenuItemView) view).setTitle(getItem(i).getTitle());

        return view;
    }
}
