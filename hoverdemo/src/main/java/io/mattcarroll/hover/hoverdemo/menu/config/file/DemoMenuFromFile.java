package io.mattcarroll.hover.hoverdemo.menu.config.file;

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
