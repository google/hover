/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mattcarroll.hover.hoverdemo.menu;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.mattcarroll.hover.defaulthovermenu.menus.DoNothingMenuAction;
import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuItem;
import io.mattcarroll.hover.defaulthovermenu.menus.ShowSubmenuMenuAction;

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
