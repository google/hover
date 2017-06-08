package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.codecanon.hover.hoverdemo.helloworld.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.window.HoverMenuService;

/**
 * Extend {@link HoverMenuService} to get a Hover menu that displays the tabs and content
 * in your custom {@link HoverMenu}.
 *
 * This demo menu displays multiple sections of content.
 */
public class MultipleSectionsHoverMenuService extends HoverMenuService {

    private static final String TAG = "MultipleSectionsHoverMenuService";

    @Override
    protected HoverMenu createHoverMenu(@NonNull Intent intent) {
        return new MultiSectionHoverMenu(getApplicationContext());
    }

    private static class MultiSectionHoverMenu extends HoverMenu {

        private final Context mContext;
        private final List<Section> mSections;

        public MultiSectionHoverMenu(@NonNull Context context) {
            mContext = context.getApplicationContext();

            mSections = Arrays.asList(
                    new Section(
                            new SectionId("1"),
                            createTabView(),
                            new HoverMenuScreen(mContext, "Screen 1")
                    ),
                    new Section(
                            new SectionId("2"),
                            createTabView(),
                            new HoverMenuScreen(mContext, "Screen 2")
                    ),
                    new Section(
                            new SectionId("3"),
                            createTabView(),
                            new HoverMenuScreen(mContext, "Screen 3")
                    )
            );
        }

        private View createTabView() {
            int tabPadding = mContext.getResources().getDimensionPixelSize(R.dimen.tab_padding);
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background);

            int tabSize = mContext.getResources().getDimensionPixelSize(R.dimen.tab_size);
            FrameLayout tabView = new FrameLayout(mContext);
//            tabView.setPadding(tabPadding, tabPadding, tabPadding, tabPadding);
            tabView.addView(imageView, new FrameLayout.LayoutParams(tabSize, tabSize));

            return tabView;
        }

        @Override
        public String getId() {
            return "multisectionmenu";
        }

        @Override
        public int getSectionCount() {
            return mSections.size();
        }

        @Nullable
        @Override
        public Section getSection(int index) {
            return mSections.get(index);
        }

        @Nullable
        @Override
        public Section getSection(@NonNull SectionId sectionId) {
            for (Section section : mSections) {
                if (section.getId().equals(sectionId)) {
                    return section;
                }
            }
            return null;
        }

        @NonNull
        @Override
        public List<Section> getSections() {
            return new ArrayList<>(mSections);
        }
    }

}
