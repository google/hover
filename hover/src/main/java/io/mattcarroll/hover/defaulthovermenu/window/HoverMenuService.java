package io.mattcarroll.hover.defaulthovermenu.window;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import io.mattcarroll.hover.HoverMenuAdapter;

/**
 * {@code Service} that presents a {@code HoverMenu} within a {@code Window}.
 *
 * The Hover menu is displayed whenever any Intent is received by this {@code Service}. The Hover
 * menu is removed and destroyed whenever this {@code Service} is destroyed.
 *
 * A {@link Service} is required for displaying a {@code HoverMenu} in a {@code Window} because there
 * is no {@code Activity} to associate with the {@code HoverMenu}'s UI. This {@code Service} is the
 * application's link to the device's {@code Window} to display the {@code HoverMenu}.
 */
public abstract class HoverMenuService extends Service {

    private static final String TAG = "HoverMenuService";

    private static final String PREF_FILE = "hover_menu";
    private static final String PREF_ICON_POSITION_X = "icon_position_x";
    private static final String PREF_ICON_POSITION_Y = "icon_position_y";

    private SharedPreferences mPrefs;
    private WindowHoverMenu mWindowHoverMenu;
    private boolean mIsRunning;
    private WindowHoverMenu.MenuExitListener mWindowHoverMenuMenuExitListener = new WindowHoverMenu.MenuExitListener() {
        @Override
        public void onHoverMenuAboutToExit() {
            Log.d(TAG, "Menu exit requested.");
            savePreferredLocation();
            stopSelf();
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        mPrefs = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        ContextThemeWrapper menuThemedContext = new ContextThemeWrapper(this, getMenuTheme());
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowHoverMenu = new WindowHoverMenu(menuThemedContext, windowManager, loadPreferredLocation());
        mWindowHoverMenu.setAdapter(createHoverMenuAdapter());
        mWindowHoverMenu.setMenuExitListener(mWindowHoverMenuMenuExitListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mIsRunning) {
            Log.d(TAG, "onStartCommand() - showing Hover menu.");
            mIsRunning = true;
            mWindowHoverMenu.show();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        mWindowHoverMenu.exit();
        mIsRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    abstract protected int getMenuTheme();

    abstract protected HoverMenuAdapter createHoverMenuAdapter();

    private void savePreferredLocation() {
        PointF preferredLocation = mWindowHoverMenu.getAnchorState();
        mPrefs.edit()
                .putFloat(PREF_ICON_POSITION_X, preferredLocation.x)
                .putFloat(PREF_ICON_POSITION_Y, preferredLocation.y)
                .apply();
    }

    private PointF loadPreferredLocation() {
        return new PointF(mPrefs.getFloat(PREF_ICON_POSITION_X, 0), mPrefs.getFloat(PREF_ICON_POSITION_Y, 0));
    }
}
