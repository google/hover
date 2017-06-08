package io.mattcarroll.hoverdemo.nonfullscreen;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;

import java.util.Collections;
import java.util.List;

import io.mattcarroll.hover.HoverMenu;

/**
 * A HoverMenu provides the Sections that are displayed in the HoverMenuView. Each Section has an
 * ID, tab View, and visual content.
 */
public class DemoHoverMenu extends HoverMenu {

    private final Context mContext;
    private final String mMenuId;
    private final Section mSection;

    public DemoHoverMenu(@NonNull Context context, @NonNull String menuId) {
        mContext = context.getApplicationContext();
        mMenuId = menuId;
        mSection = new Section(
                new SectionId("0"),
                createTabView(),
                new NonFullscreenContent(context)
        );
    }

    private View createTabView() {
        Resources resources = mContext.getResources();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, resources.getDisplayMetrics());

        DemoTabView view = new DemoTabView(
                mContext,
                resources.getDrawable(R.drawable.tab_background),
                resources.getDrawable(R.drawable.ic_orange_circle)
        );
        view.setTabBackgroundColor(0xFFFF9600);
        view.setTabForegroundColor(null);
        view.setPadding(padding, padding, padding, padding);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(padding);
        }
        return view;
    }

    @Override
    public String getId() {
        return mMenuId;
    }

    @Override
    public int getSectionCount() {
        return 1;
    }

    @Nullable
    @Override
    public Section getSection(int index) {
        return mSection;
    }

    @Nullable
    @Override
    public Section getSection(@NonNull SectionId sectionId) {
        return mSection;
    }

    @NonNull
    @Override
    public List<Section> getSections() {
        return Collections.singletonList(mSection);
    }
}
