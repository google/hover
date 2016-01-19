package io.mattcarroll.hover.hoverdemo;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuAction;

/**
 * Demo implementation of a {@link HoverMenuAdapter}.
 */
public class DemoHoverMenuAdapter implements HoverMenuAdapter {

    private static final String LOCATION_ID = "location";
    private static final String BLUETOOTH_ID = "bluetooth";
    private static final String WIFI_ID = "wifi";

    private final Context mContext;
    private final Menu mMenu;

    public DemoHoverMenuAdapter(@NonNull Context context, @NonNull Menu demoMenu) throws IOException {
        mContext = context;
        mMenu = demoMenu;
    }

    @Override
    public int getTabCount() {
        return mMenu.getMenuItemList().size();
    }

    @Override
    public View getTabView(int index) {
        String menuItemId = mMenu.getMenuItemList().get(index).getId();
        if (LOCATION_ID.equals(menuItemId)) {
            return createTabView(R.drawable.ic_tab_location);
        } else if (BLUETOOTH_ID.equals(menuItemId)) {
            return createTabView(R.drawable.ic_tab_bluetooth);
        } else if (WIFI_ID.equals(menuItemId)) {
            return createTabView(R.drawable.ic_tab_wifi);
        } else {
            throw new RuntimeException("Unknown tab selected: " + index);
        }
    }

    @Override
    public MenuAction getTabMenuAction(int index) {
        return mMenu.getMenuItemList().get(index).getMenuAction();
    }

    private View createTabView(@DrawableRes int tabBitmapRes) {
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mContext.getResources().getDisplayMetrics());

        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(tabBitmapRes);
        imageView.setPadding(padding, padding, padding, padding);
        return imageView;
    }
}
