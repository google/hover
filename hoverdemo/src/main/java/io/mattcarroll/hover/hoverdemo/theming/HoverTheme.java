package io.mattcarroll.hover.hoverdemo.theming;

import android.support.annotation.ColorInt;

/**
 * POJO representing a theme configuration.
 */
public class HoverTheme {

    @ColorInt
    private int mAccentColor;

    @ColorInt
    private int mBaseColor;

    public HoverTheme(@ColorInt int accentColor, @ColorInt int baseColor) {
        mAccentColor = accentColor;
        mBaseColor = baseColor;
    }

    @ColorInt
    public int getAccentColor() {
        return mAccentColor;
    }

    @ColorInt
    public int getBaseColor() {
        return mBaseColor;
    }
}
