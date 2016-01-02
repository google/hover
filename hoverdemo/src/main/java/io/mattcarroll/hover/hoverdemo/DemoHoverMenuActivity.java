package io.mattcarroll.hover.hoverdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import io.mattcarroll.hover.defaulthovermenu.view.ViewHoverMenu;

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

        mHoverMenuView = (ViewHoverMenu) findViewById(R.id.hovermenu);
        mHoverMenuView.setAdapter(new DemoHoverMenuAdapter(this));
    }
}
