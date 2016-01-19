package io.mattcarroll.hover.hoverdemo;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;

import java.io.IOException;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.window.HoverMenuService;
import io.mattcarroll.hover.hoverdemo.menu.DemoMenuFromCode;
import io.mattcarroll.hover.hoverdemo.menu.DemoMenuFromFile;

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
        DemoMenuFromFile demoMenuFromFile = new DemoMenuFromFile(this, new DemoMenuActionFactory(this));
        return demoMenuFromFile.createFromFile("demo_menu.json");
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
