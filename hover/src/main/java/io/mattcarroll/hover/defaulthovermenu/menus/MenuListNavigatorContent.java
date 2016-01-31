package io.mattcarroll.hover.defaulthovermenu.menus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.ToolbarNavigator;

/**
 * Implementation of {@link NavigatorContent} that displays a {@link MenuItem} as a list.
 */
public class MenuListNavigatorContent implements NavigatorContent {

    private static final String TAG = "MenuListNavigatorContent";

    private Menu mMenu;
    private MenuListView mMenuListView;
    private Navigator mNavigator;

    public MenuListNavigatorContent(@NonNull Context context, @NonNull final Menu menu) {
        this(context, menu, null);
    }

    public MenuListNavigatorContent(@NonNull Context context, @NonNull final Menu menu, @Nullable View emptyView) {
        mMenu = menu;
        mMenuListView = new MenuListView(context);
        mMenuListView.setMenu(menu);
        mMenuListView.setMenuItemSelectionListener(new MenuListView.MenuItemSelectionListener() {
            @Override
            public void onMenuItemSelected(@NonNull MenuItem menuItem) {
                menuItem.getMenuAction().execute(getView().getContext(), mNavigator);
            }
        });

        setEmptyView(emptyView);
    }

    public void setEmptyView(@Nullable View emptyView) {
        mMenuListView.setEmptyView(emptyView);
    }

    @NonNull
    @Override
    public View getView() {
        return mMenuListView;
    }

    @Override
    public void onShown(@NonNull Navigator navigator) {
        mNavigator = navigator;
        if (navigator instanceof ToolbarNavigator) {
            ((ToolbarNavigator) navigator).getToolbar().setTitle(mMenu.getTitle());
        }
    }

    @Override
    public void onHidden() {
        mNavigator = null;
    }

}
