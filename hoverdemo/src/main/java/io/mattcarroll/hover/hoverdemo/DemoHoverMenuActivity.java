package io.mattcarroll.hover.hoverdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.view.ViewHoverMenu;
import io.mattcarroll.hover.hoverdemo.menu.DemoMenuFromCode;
import io.mattcarroll.hover.hoverdemo.menu.DemoMenuFromFile;

/**
 * Presents a Hover menu within an Activity (instead of presenting it on top of all other Windows).
 */
public class DemoHoverMenuActivity extends Activity {

    private static final String TAG = "HoverMenuActivity";

    private ViewHoverMenu mHoverMenuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hover_menu);

        try {
            mHoverMenuView = (ViewHoverMenu) findViewById(R.id.hovermenu);
            mHoverMenuView.setAdapter(new DemoHoverMenuAdapter(this, createDemoMenuFromFile()));
        } catch (IOException e) {
            Log.e(TAG, "Failed to create demo menu from file.");
            e.printStackTrace();
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
