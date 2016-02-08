package io.mattcarroll.hover.hoverdemo;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import de.greenrobot.event.EventBus;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.defaulthovermenu.menus.DoNothingMenuAction;
import io.mattcarroll.hover.defaulthovermenu.menus.Menu;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuItem;
import io.mattcarroll.hover.defaulthovermenu.menus.MenuListNavigatorContent;
import io.mattcarroll.hover.defaulthovermenu.menus.ShowSubmenuMenuAction;
import io.mattcarroll.hover.defaulthovermenu.toolbar.ToolbarNavigatorContent;
import io.mattcarroll.hover.hoverdemo.appstate.AppStateNavigatorContent;
import io.mattcarroll.hover.hoverdemo.colorselection.ColorSelectionNavigatorContent;
import io.mattcarroll.hover.hoverdemo.introduction.HoverIntroductionNavigatorContent;
import io.mattcarroll.hover.hoverdemo.placeholder.PlaceholderNavigatorContent;
import io.mattcarroll.hover.hoverdemo.theming.HoverThemeManager;

/**
 * Can create a Hover menu from code or from file.
 */
public class DemoHoverMenuFactory {

    /**
     * Example of how to create a menu in code.
     * @return HoverMenuAdapter
     */
    public DemoHoverMenuAdapter createDemoMenuFromCode(@NonNull Context context, @NonNull EventBus bus) throws IOException {
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

        MenuListNavigatorContent drillDownMenuNavigatorContent = new MenuListNavigatorContent(context, drillDownMenu);

        ToolbarNavigatorContent toolbarNavigatorContent = new ToolbarNavigatorContent(context);
        toolbarNavigatorContent.pushContent(drillDownMenuNavigatorContent);

        Map<String, NavigatorContent> demoMenu = new LinkedHashMap<>();
        demoMenu.put(DemoHoverMenuAdapter.INTRO_ID, new HoverIntroductionNavigatorContent(context, Bus.getInstance()));
        demoMenu.put(DemoHoverMenuAdapter.SELECT_COLOR_ID, new ColorSelectionNavigatorContent(context, Bus.getInstance(), HoverThemeManager.getInstance(), HoverThemeManager.getInstance().getTheme()));
        demoMenu.put(DemoHoverMenuAdapter.APP_STATE_ID, new AppStateNavigatorContent(context));
        demoMenu.put(DemoHoverMenuAdapter.MENU_ID, toolbarNavigatorContent);
        demoMenu.put(DemoHoverMenuAdapter.PLACEHOLDER_ID, new PlaceholderNavigatorContent(context, bus));

        return new DemoHoverMenuAdapter(context, demoMenu, HoverThemeManager.getInstance().getTheme());
    }

}
