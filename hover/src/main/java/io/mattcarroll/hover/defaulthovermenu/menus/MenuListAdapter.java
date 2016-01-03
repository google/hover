package io.mattcarroll.hover.defaulthovermenu.menus;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that displays {@link MenuItem}s using {@link MenuItemView}s.
 */
public class MenuListAdapter extends BaseAdapter {

    private List<MenuItem> mMenuItems = new ArrayList<>();

    public void setMenuItems(List<MenuItem> menuItems) {
        mMenuItems.clear();
        mMenuItems.addAll(menuItems);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMenuItems.size();
    }

    @Override
    public MenuItem getItem(int i) {
        return mMenuItems.get(i);
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
