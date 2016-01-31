package io.mattcarroll.hover.hoverdemo.menu.config.code;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.mattcarroll.hover.defaulthovermenu.menus.DoNothingMenuAction;
import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuItem;
import io.mattcarroll.hover.defaulthovermenu.menus.ShowSubmenuMenuAction;
import io.mattcarroll.hover.hoverdemo.menu.ui.EmptyListView;

/**
 * Example of creating a {@link Menu} in code.
 */
public class DemoMenuFromCode {

    private static final String LOCATION_ID = "location";
    private static final String BLUETOOTH_ID = "bluetooth";
    private static final String WIFI_ID = "wifi";

    private final Context mContext;

    public DemoMenuFromCode(@NonNull Context context) {
        mContext = context;
    }

    public Menu createMenu() {
        List<MenuItem> locationSubmenuItems = Arrays.asList(
                new MenuItem(UUID.randomUUID().toString(), "GPS", new DoNothingMenuAction()),
                new MenuItem(UUID.randomUUID().toString(), "Cell Tower Triangulation", new DoNothingMenuAction()),
                new MenuItem(UUID.randomUUID().toString(), "Location Services", new DoNothingMenuAction())
        );
        Menu locationMenu = new Menu("User Location", locationSubmenuItems);
        MenuItem locationMenuItem = new MenuItem(LOCATION_ID, "User Location", new ShowSubmenuMenuAction(locationMenu, new EmptyListView(mContext)));

        List<MenuItem> bluetoothSubmenuItems = Arrays.asList(
                new MenuItem(UUID.randomUUID().toString(), "Estimote Beacon", new DoNothingMenuAction()),
                new MenuItem(UUID.randomUUID().toString(), "Moto 360", new DoNothingMenuAction())
        );
        Menu bluetoothMenu = new Menu("Bluetooth Devices", bluetoothSubmenuItems);
        MenuItem bluetoothMenuItem = new MenuItem(BLUETOOTH_ID, "Bluetooth Devices", new ShowSubmenuMenuAction(bluetoothMenu, new EmptyListView(mContext)));

        List<MenuItem> wifiSubmenuItems = Arrays.asList(
//                wifiMenu.addItems(
//                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID().toString(), "Wireless Access Point", null),
//                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID().toString(), "ATT-483759", null),
//                new MenuItem(MenuItem.Type.DO_ACTION, UUID.randomUUID().toString(), "Linksys-1294562", null)
//        );
        );
        Menu wifiMenu = new Menu("WiFi", wifiSubmenuItems);
        MenuItem wifiMenuItem = new MenuItem(WIFI_ID, "WiFi", new ShowSubmenuMenuAction(wifiMenu, new EmptyListView(mContext)));

        return new Menu("", Arrays.asList(locationMenuItem, bluetoothMenuItem, wifiMenuItem));
    }

}
