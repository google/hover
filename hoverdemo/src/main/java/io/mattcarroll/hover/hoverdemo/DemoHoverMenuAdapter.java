package io.mattcarroll.hover.hoverdemo;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuItem;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuListNavigatorContent;

import java.util.UUID;

/**
 * Demo implementation of a {@link HoverMenuAdapter}.
 */
public class DemoHoverMenuAdapter implements HoverMenuAdapter {

    private final Context mContext;
    private MenuListNavigatorContent mRedContent;
    private MenuListNavigatorContent mGreenContent;
    private MenuListNavigatorContent mBlueContent;

    public DemoHoverMenuAdapter(@NonNull Context context) {
        mContext = context;

        MenuItem redMenu = new MenuItem(MenuItem.Type.MENU, UUID.randomUUID(), "Red", null);
        redMenu.addItems(
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Red Menu Item 1", null),
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Red Menu Item 2", null),
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Red Menu Item 3", null)
        );
        mRedContent = new MenuListNavigatorContent(context, redMenu);

        MenuItem greenMenu = new MenuItem(MenuItem.Type.MENU, UUID.randomUUID(), "Green", null);
        greenMenu.addItems(
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Green Menu Item 1", null),
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Green Menu Item 2", null)
        );
        mGreenContent = new MenuListNavigatorContent(context, greenMenu);

        MenuItem blueMenu = new MenuItem(MenuItem.Type.MENU, UUID.randomUUID(), "Blue", null);
        blueMenu.addItems(
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Blue Menu Item 1", null),
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Blue Menu Item 2", null),
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Blue Menu Item 3", null)
        );
        mBlueContent = new MenuListNavigatorContent(context, blueMenu);
    }

    @Override
    public int getTabCount() {
        return 3;
    }

    @Override
    public View getTabView(int index) {
        switch (index) {
            case 0:
                return createTabView(R.drawable.ic_tab_red);
            case 1:
                return createTabView(R.drawable.ic_tab_green);
            case 2:
                return createTabView(R.drawable.ic_tab_blue);
            default:
                throw new RuntimeException("Unknown tab selected: " + index);
        }
    }

    @Override
    public NavigatorContent getContentView(int index) {
        switch (index) {
            case 0:
                return mRedContent;
            case 1:
                return mGreenContent;
            case 2:
                return mBlueContent;
            default:
                throw new RuntimeException("Unknown content selected: " + index);
        }
    }

    private View createTabView(@DrawableRes int tabBitmapRes) {
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mContext.getResources().getDisplayMetrics());

        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(tabBitmapRes);
        imageView.setPadding(padding, padding, padding, padding);
        return imageView;
    }
}
