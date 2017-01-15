/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
