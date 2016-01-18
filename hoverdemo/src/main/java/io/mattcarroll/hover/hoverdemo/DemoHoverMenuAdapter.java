package io.mattcarroll.hover.hoverdemo;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import java.util.UUID;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuItem;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuListNavigatorContent;
import io.mattcarroll.hover.hoverdemo.menu.EmptyListView;

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

        MenuItem locationMenu = new MenuItem(MenuItem.Type.MENU, UUID.randomUUID(), "User Location", null);
        locationMenu.addItems(
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "GPS", null),
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Cell Tower Triangulation", null),
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Location Services", null)
        );
        mRedContent = new MenuListNavigatorContent(context, locationMenu, new EmptyListView(context));

        MenuItem bluetoothMenu = new MenuItem(MenuItem.Type.MENU, UUID.randomUUID(), "Bluetooth Devices", null);
        bluetoothMenu.addItems(
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Estimote Beacon", null),
                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Moto 360", null)
        );
        mGreenContent = new MenuListNavigatorContent(context, bluetoothMenu, new EmptyListView(context));

        MenuItem wifiMenu = new MenuItem(MenuItem.Type.MENU, UUID.randomUUID(), "WiFi", null);
//        wifiMenu.addItems(
//                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Wireless Access Point", null),
//                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "ATT-483759", null),
//                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID(), "Linksys-1294562", null)
//        );
        mBlueContent = new MenuListNavigatorContent(context, wifiMenu, new EmptyListView(context));
    }

    @Override
    public int getTabCount() {
        return 3;
    }

    @Override
    public View getTabView(int index) {
        switch (index) {
            case 0:
                return createTabView(R.drawable.ic_tab_location);
            case 1:
                return createTabView(R.drawable.ic_tab_bluetooth);
            case 2:
                return createTabView(R.drawable.ic_tab_wifi);
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
