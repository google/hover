package io.mattcarroll.hover.defaulthovermenu.menus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;

/**
 * Implementation of {@link NavigatorContent} that displays a {@link MenuItem} as a list.
 */
public class MenuListNavigatorContent implements NavigatorContent {

    private static final String TAG = "MenuListNavigatorContent";

    private MenuItem mMenu;
    private MenuListView mMenuListView;
    private Navigator mNavigator;

    public MenuListNavigatorContent(@NonNull Context context, @NonNull final MenuItem menu) {
        this(context, menu, null);
    }

    public MenuListNavigatorContent(@NonNull Context context, @NonNull final MenuItem menu, @Nullable View emptyView) {
        mMenu = menu;
        mMenuListView = new MenuListView(context);
        mMenuListView.setMenu(menu);
        mMenuListView.setMenuItemSelectionListener(new MenuListView.MenuItemSelectionListener() {
            @Override
            public void onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getType()) {
                    case MENU:
                        MenuListNavigatorContent submenu = new MenuListNavigatorContent(getView().getContext(), menuItem);
                        mNavigator.pushContent(submenu);
                        break;
                    case DO_ACTION:
                    case SHOW_VIEW:
                        try {
                            runDevActionFromClasspath(menuItem.getPayload());
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to run action for menu item: " + menuItem.getTitle());
                            e.printStackTrace();
                        }
                        break;
                }
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
        Log.d(TAG, "Setting title: " + mMenu.getTitle());
        mNavigator.setTitle(mMenu.getTitle());
    }

    @Override
    public void onHidden() {
        mNavigator = null;
    }

    private void runDevActionFromClasspath(@NonNull String devActionClassPath) {
        try {
            MenuAction menuAction = (MenuAction) Class.forName(devActionClassPath).newInstance();
            menuAction.execute(getView().getContext(), mNavigator);
        } catch(ClassNotFoundException e) {
            Log.w(TAG, "Could not locate class: " + devActionClassPath);
            e.printStackTrace();
        } catch (InstantiationException e) {
            Log.w(TAG, "InstantiationException: " + devActionClassPath + ", error: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException: " + devActionClassPath);
            e.printStackTrace();
        } catch (ClassCastException e) {
            Log.w(TAG, "Menu item's action is not a DevAction implementation: " + devActionClassPath);
        }
    }
}
