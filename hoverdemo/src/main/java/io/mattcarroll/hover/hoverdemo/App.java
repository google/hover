package io.mattcarroll.hover.hoverdemo;

import android.app.ActivityManager;
import android.app.Application;
import android.support.v4.content.ContextCompat;

import io.mattcarroll.hover.hoverdemo.appstate.AppStateTracker;
import io.mattcarroll.hover.hoverdemo.theming.HoverTheme;
import io.mattcarroll.hover.hoverdemo.theming.HoverThemeManager;

/**
 * Application class.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setupTheme();
        setupAppStateTracking();
    }

    private void setupTheme() {
        HoverTheme defaultTheme = new HoverTheme(
                ContextCompat.getColor(this, R.color.hover_accent),
                ContextCompat.getColor(this, R.color.hover_base));
        HoverThemeManager.init(Bus.getInstance(), defaultTheme);
    }

    private void setupAppStateTracking() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        AppStateTracker.init(this, Bus.getInstance());
        AppStateTracker.getInstance().trackTask(activityManager.getAppTasks().get(0).getTaskInfo());
    }
}
