package io.mattcarroll.hover.hoverdemo;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;

import java.io.IOException;
import java.io.InputStream;

import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.menus.serialization.MenuDeserializer;
import io.mattcarroll.hover.defaulthovermenu.window.HoverMenuService;
import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.hoverdemo.menu.DemoMenuFromCode;

/**
 * Demo {@link HoverMenuService}.
 */
public class DemoHoverMenuService extends HoverMenuService {

    private static final String TAG = "DemoHoverMenuService";

    public static void showFloatingMenu(Context context) {
        context.startService(new Intent(context, DemoHoverMenuService.class));
    }

    @Override
    protected int getMenuTheme() {
        return R.style.AppTheme;
    }

    @Override
    protected HoverMenuAdapter createHoverMenuAdapter() {
        final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.AppTheme);
        try {
            return new DemoHoverMenuAdapter(contextThemeWrapper, createDemoMenuFromFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Example of how to create a menu from a configuration file.
     *
     * @return Menu
     * @throws IOException
     */
    private Menu createDemoMenuFromFile() throws IOException {
        InputStream in = getAssets().open("demo_menu.json");
        MenuDeserializer menuDeserializer = new MenuDeserializer(new DemoMenuActionFactory(this));
        return menuDeserializer.deserializeMenu(in);
    }

    /**
     * Example of how to create a menu in code.
     * @return Menu
     */
    private Menu createDemoMenuFromCode() {
        DemoMenuFromCode demoMenuFromCode = new DemoMenuFromCode(this);
        return demoMenuFromCode.createMenu();
    }

}
