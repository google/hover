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
package io.mattcarroll.hover.hoverdemo.kitchensink;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;

import java.io.IOException;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverMenuView;
import io.mattcarroll.hover.window.HoverMenuService;
import io.mattcarroll.hover.hoverdemo.kitchensink.theming.HoverTheme;

/**
 * Demo {@link HoverMenuService}.
 */
public class DemoHoverMenuService extends HoverMenuService {

    private static final String TAG = "DemoHoverMenuService";

    public static void showFloatingMenu(Context context) {
        context.startService(new Intent(context, DemoHoverMenuService.class));
    }

    private DemoHoverMenu mDemoHoverMenu;

    @Override
    public void onCreate() {
        super.onCreate();
        Bus.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        Bus.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    protected Context getContextForHoverMenu() {
        return new ContextThemeWrapper(this, R.style.AppTheme);
    }

    @Override
    protected HoverMenu createHoverMenu(@NonNull Intent intent) {
        try {
            mDemoHoverMenu = new DemoHoverMenuFactory().createDemoMenuFromCode(getContextForHoverMenu(), Bus.getInstance());
//            mDemoHoverMenuAdapter = new DemoHoverMenuFactory().createDemoMenuFromFile(getContextForHoverMenu());
            return mDemoHoverMenu;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onHoverMenuLaunched(@NonNull HoverMenuView hoverMenuView) {
        hoverMenuView.collapse();
    }

    public void onEventMainThread(@NonNull HoverTheme newTheme) {
        mDemoHoverMenu.setTheme(newTheme);
    }

}
