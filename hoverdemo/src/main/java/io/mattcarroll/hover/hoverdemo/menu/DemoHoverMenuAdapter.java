package io.mattcarroll.hover.hoverdemo.menu;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.hoverdemo.R;
import io.mattcarroll.hover.hoverdemo.menu.ui.DemoTabViewHack;
import io.mattcarroll.hover.hoverdemo.theming.HoverTheme;

/**
 * Demo implementation of a {@link HoverMenuAdapter}.
 */
public class DemoHoverMenuAdapter implements HoverMenuAdapter {

    public static final String INTRO_ID = "intro";
    public static final String SELECT_COLOR_ID = "select_color";
    public static final String APP_STATE_ID = "app_state";
    public static final String MENU_ID = "menu";
    public static final String PLACEHOLDER_ID = "placeholder";

    private final Context mContext;
//    private final Menu mMenu;
    private HoverTheme mTheme;
    private final List<String> mTabIds;
    private final Map<String, NavigatorContent> mData;
    private final Set<ContentChangeListener> mContentChangeListeners = new HashSet<>();

    public DemoHoverMenuAdapter(@NonNull Context context, @NonNull Map<String, NavigatorContent> data, @NonNull HoverTheme theme) throws IOException {
        mContext = context;
        mData = data;
        mTheme = theme;

        mTabIds = new ArrayList<>();
        for (String tabId : mData.keySet()) {
            mTabIds.add(tabId);
        }
    }

    public void setTheme(@NonNull HoverTheme theme) {
        mTheme = theme;
        notifyDataSetChanged();
    }

    @Override
    public int getTabCount() {
//        return mMenu.getMenuItemList().size();
        return mTabIds.size();
    }

    @Override
    public View getTabView(int index) {
//        String menuItemId = mMenu.getMenuItemList().get(index).getId();
        String menuItemId = mTabIds.get(index);
        if (INTRO_ID.equals(menuItemId)) {
            return createTabView(R.drawable.ic_orange_circle, mTheme.getAccentColor(), null);
//            return createTabView(R.drawable.ic_tab_location);
        } else if (SELECT_COLOR_ID.equals(menuItemId)) {
            return createTabView(R.drawable.ic_paintbrush, mTheme.getAccentColor(), mTheme.getBaseColor());
//            return createTabView(R.drawable.ic_tab_bluetooth);
        } else if (APP_STATE_ID.equals(menuItemId)) {
            return createTabView(R.drawable.ic_stack, mTheme.getAccentColor(), mTheme.getBaseColor());
//            return createTabView(R.drawable.ic_tab_wifi);
        } else if (MENU_ID.equals(menuItemId)) {
            return createTabView(R.drawable.ic_menu, mTheme.getAccentColor(), mTheme.getBaseColor());
        } else if (PLACEHOLDER_ID.equals(menuItemId)) {
            return createTabView(R.drawable.ic_pen, mTheme.getAccentColor(), mTheme.getBaseColor());
        } else {
            throw new RuntimeException("Unknown tab selected: " + index);
        }
    }

    @Override
    public long getTabId(int position) {
        return position;
    }

    @Override
    public NavigatorContent getNavigatorContent(int index) {
//        return mMenu.getMenuItemList().get(index).getMenuAction();
        String tabId = mTabIds.get(index);
        return mData.get(tabId);
    }

    @Override
    public void addContentChangeListener(@NonNull ContentChangeListener listener) {
        mContentChangeListeners.add(listener);
    }

    @Override
    public void removeContentChangeListener(@NonNull ContentChangeListener listener) {
        mContentChangeListeners.remove(listener);
    }

    protected void notifyDataSetChanged() {
        for (ContentChangeListener listener : mContentChangeListeners) {
            listener.onContentChange(this);
        }
    }

    private View createTabView(@DrawableRes int tabBitmapRes, @ColorInt int backgroundColor, @ColorInt Integer iconColor) {
//        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mContext.getResources().getDisplayMetrics());
//
//        ImageView imageView = new ImageView(mContext);
//        imageView.setImageResource(tabBitmapRes);
//        imageView.setPadding(padding, padding, padding, padding);
//        return imageView;

        Resources resources = mContext.getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, resources.getDisplayMetrics());

        DemoTabViewHack view = new DemoTabViewHack(mContext, resources.getDrawable(R.drawable.tab_background), resources.getDrawable(tabBitmapRes));
        view.setTabBackgroundColor(backgroundColor);
        view.setTabForegroundColor(iconColor);
        view.setPadding(padding, padding, padding, padding);
        view.setElevation(padding);
        return view;
    }
}
