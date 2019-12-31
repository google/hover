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
package io.mattcarroll.hover.content.menus.serialization;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.mattcarroll.hover.content.menus.DoNothingMenuAction;
import io.mattcarroll.hover.content.menus.Menu;
import io.mattcarroll.hover.content.menus.MenuAction;
import io.mattcarroll.hover.content.menus.MenuItem;

/**
 * Creates a {@link Menu} from a text-based configuration.
 */
public class MenuDeserializer {

    private final MenuActionFactory mMenuActionFactory;

    public MenuDeserializer(@NonNull MenuActionFactory menuActionFactory) {
        mMenuActionFactory = menuActionFactory;
    }

    public Menu deserializeMenu(InputStream in) throws IOException {
        return doDeserialization(new BufferedReader(new InputStreamReader(in)));
    }

    public Menu deserializeMenu(@NonNull String json) throws IOException {
        try {
            JSONArray jsonArray = new JSONArray(json);
            return createMenuFromJson(jsonArray);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private Menu doDeserialization(@NonNull BufferedReader br) throws IOException {
        // Read in all the menu configuration JSON.
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String jsonContent = sb.toString();

        // Create Menu from JSON.
        try {
            JSONArray menuJson = new JSONArray(jsonContent);
            return createMenuFromJson(menuJson);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private Menu createMenuFromJson(@NonNull JSONArray menuJson) throws JSONException {
        List<MenuItem> menuItemList = new ArrayList<>();
        JSONObject menuItemJson;
        MenuItem menuItem;
        for (int i = 0; i < menuJson.length(); ++i) {
            menuItemJson = menuJson.getJSONObject(i);
            menuItem = recursivelyConstructMenuItem(menuItemJson);
            menuItemList.add(menuItem);
        }

        return new Menu("", menuItemList);
    }

    private MenuItem recursivelyConstructMenuItem(@NonNull JSONObject menuItemJson) throws JSONException {
        MenuItem menuItem;

        if (menuItemJson.has("items")) {
            // This menu item contains a submenu. Recursively construct the submenu.
            JSONArray submenuItemsJson = menuItemJson.getJSONArray("items");
            List<MenuItem> submenuItems = new ArrayList<>();
            MenuItem submenuItem;
            for (int i = 0; i < submenuItemsJson.length(); ++i) {
                submenuItem = recursivelyConstructMenuItem(submenuItemsJson.getJSONObject(i));
                submenuItems.add(submenuItem);
            }

            String id = menuItemJson.has("id") ? menuItemJson.getString("id") : UUID.randomUUID().toString();
            String title = menuItemJson.getString("title");
            Menu submenu = new Menu(title, submenuItems);
            MenuAction showMenuAction = mMenuActionFactory.createShowSubmenuMenuAction(submenu);
            menuItem = new MenuItem(id, title, showMenuAction);
        } else if (menuItemJson.has("action")) {
            // This item does not have a submenu, it just has a menu action.
            String id = menuItemJson.has("id") ? menuItemJson.getString("id") : UUID.randomUUID().toString();
            String title = menuItemJson.getString("title");
            String menuActionId = menuItemJson.getString("action");
            MenuAction menuAction = mMenuActionFactory.createMenuActionForId(menuActionId);
            menuItem = new MenuItem(id, title, menuAction);
        } else {
            // This menu item must a stub without any action.
            String id = menuItemJson.has("id") ? menuItemJson.getString("id") : UUID.randomUUID().toString();
            String title = menuItemJson.getString("title");
            menuItem = new MenuItem(id, title, new DoNothingMenuAction());
        }

        return menuItem;
    }

}
