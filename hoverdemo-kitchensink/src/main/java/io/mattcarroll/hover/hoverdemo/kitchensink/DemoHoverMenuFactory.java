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
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.content.menus.DoNothingMenuAction;
import io.mattcarroll.hover.content.menus.Menu;
import io.mattcarroll.hover.content.menus.MenuItem;
import io.mattcarroll.hover.content.menus.MenuListContent;
import io.mattcarroll.hover.content.menus.ShowSubmenuMenuAction;
import io.mattcarroll.hover.content.toolbar.ToolbarNavigator;
import io.mattcarroll.hover.hoverdemo.kitchensink.appstate.AppStateContent;
import io.mattcarroll.hover.hoverdemo.kitchensink.colorselection.ColorSelectionContent;
import io.mattcarroll.hover.hoverdemo.kitchensink.introduction.HoverIntroductionContent;
import io.mattcarroll.hover.hoverdemo.kitchensink.placeholder.PlaceholderContent;
import io.mattcarroll.hover.hoverdemo.kitchensink.theming.HoverThemeManager;

/**
 * Can create a Hover menu from code or from file.
 */
public class DemoHoverMenuFactory {

    /**
     * Example of how to create a menu in code.
     * @return HoverMenu
     */
    public DemoHoverMenu createDemoMenuFromCode(@NonNull Context context, @NonNull EventBus bus) throws IOException {
        Menu drillDownMenuLevelTwo = new Menu("Demo Menu - Level 2", Arrays.asList(
                new MenuItem(UUID.randomUUID().toString(), "Google", new DoNothingMenuAction()),
                new MenuItem(UUID.randomUUID().toString(), "Amazon", new DoNothingMenuAction())
        ));
        ShowSubmenuMenuAction showLevelTwoMenuAction = new ShowSubmenuMenuAction(drillDownMenuLevelTwo);

        Menu drillDownMenu = new Menu("Demo Menu", Arrays.asList(
                new MenuItem(UUID.randomUUID().toString(), "GPS", new DoNothingMenuAction()),
                new MenuItem(UUID.randomUUID().toString(), "Cell Tower Triangulation", new DoNothingMenuAction()),
                new MenuItem(UUID.randomUUID().toString(), "Location Services", showLevelTwoMenuAction)
        ));

        MenuListContent drillDownMenuNavigatorContent = new MenuListContent(context, drillDownMenu);

        ToolbarNavigator toolbarNavigator = new ToolbarNavigator(context);
        toolbarNavigator.pushContent(drillDownMenuNavigatorContent);

        Map<String, Content> demoMenu = new LinkedHashMap<>();
        demoMenu.put(DemoHoverMenu.INTRO_ID, new HoverIntroductionContent(context, Bus.getInstance()));
        demoMenu.put(DemoHoverMenu.SELECT_COLOR_ID, new ColorSelectionContent(context, Bus.getInstance(), HoverThemeManager.getInstance(), HoverThemeManager.getInstance().getTheme()));
        demoMenu.put(DemoHoverMenu.APP_STATE_ID, new AppStateContent(context));
        demoMenu.put(DemoHoverMenu.MENU_ID, toolbarNavigator);
        demoMenu.put(DemoHoverMenu.PLACEHOLDER_ID, new PlaceholderContent(context, bus));

        return new DemoHoverMenu(context, "kitchensink", demoMenu, HoverThemeManager.getInstance().getTheme());
    }

}
