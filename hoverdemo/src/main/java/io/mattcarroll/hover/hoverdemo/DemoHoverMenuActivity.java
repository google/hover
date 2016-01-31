package io.mattcarroll.hover.hoverdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.io.IOException;

import io.mattcarroll.hover.defaulthovermenu.view.ViewHoverMenu;
import io.mattcarroll.hover.hoverdemo.menu.DemoHoverMenuAdapter;

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
            final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this, R.style.AppTheme);
            DemoHoverMenuAdapter adapter = new DemoHoverMenuFactory().createDemoMenuFromCode(contextThemeWrapper, Bus.getInstance());
//            DemoHoverMenuAdapter adapter = new DemoHoverMenuFactory().createDemoMenuFromFile(contextThemeWrapper);

            mHoverMenuView = (ViewHoverMenu) findViewById(R.id.hovermenu);
            mHoverMenuView.setAdapter(adapter);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create demo menu from file.");
            e.printStackTrace();
        }
    }

}
