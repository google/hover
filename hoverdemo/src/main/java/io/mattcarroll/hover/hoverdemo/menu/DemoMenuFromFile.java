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

import java.io.IOException;

import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.menus.serialization.MenuActionFactory;
import io.mattcarroll.hover.defaulthovermenu.menus.serialization.MenuDeserializer;

/**
 * Example of how to deserialize a menu from a file.
 */
public class DemoMenuFromFile {

    private final Context mContext;
    private final MenuActionFactory mMenuActionFactory;

    public DemoMenuFromFile(@NonNull Context context, @NonNull MenuActionFactory menuActionFactory) {
        mContext = context;
        mMenuActionFactory = menuActionFactory;
    }

    public Menu createFromFile(@NonNull String assetFileName) throws IOException {
        MenuDeserializer menuDeserializer = new MenuDeserializer(mMenuActionFactory);
        return menuDeserializer.deserializeMenu(mContext.getAssets().open(assetFileName));
    }

}
