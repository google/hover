package io.mattcarroll.hover.hoverdemo.theming;

import android.support.annotation.NonNull;

import de.greenrobot.event.EventBus;

/**
 * Global entry point for Hover menu theming.
 */
public class HoverThemeManager implements HoverThemer {

    private static HoverThemeManager sInstance;

    public static synchronized void init(@NonNull EventBus bus, @NonNull HoverTheme theme) {
        if (null == sInstance) {
            sInstance = new HoverThemeManager(bus, theme);
        }
    }

    public static synchronized HoverThemeManager getInstance() {
        if (null == sInstance) {
            throw new RuntimeException("Cannot obtain HoverThemeManager before calling init().");
        }

        return sInstance;
    }

    private EventBus mBus;
    private HoverTheme mTheme;

    public HoverThemeManager(@NonNull EventBus bus, @NonNull HoverTheme theme) {
        mBus = bus;
        setTheme(theme);
    }

    public HoverTheme getTheme() {
        return mTheme;
    }

    @Override
    public void setTheme(@NonNull HoverTheme theme) {
        mTheme = theme;
        mBus.postSticky(theme);
    }

}
