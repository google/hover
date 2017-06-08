package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.codecanon.hover.hoverdemo.helloworld.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.mattcarroll.hover.Content;
import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.window.HoverMenuService;

/**
 * Extend {@link HoverMenuService} to get a Hover menu that displays the tabs and content
 * in your custom {@link HoverMenu}.
 *
 * This menu presents the simplest possible Hover Menu, a menu with a single Section.
 */
public class SingleSectionHoverMenuService extends HoverMenuService {

    private static final String TAG = "SingleSectionHoverMenuService";

    @Override
    protected HoverMenu createHoverMenu(@NonNull Intent intent) {
        return new SingleSectionHoverMenu(getApplicationContext());
    }

    private static class SingleSectionHoverMenu extends HoverMenu {

        private Context mContext;
        private Section mSection;

        private SingleSectionHoverMenu(@NonNull Context context) {
            mContext = context;

            mSection = new Section(
                    new SectionId("1"),
                    createTabView(),
                    createScreen()
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

        private Content createScreen() {
            return new HoverMenuScreen(mContext, "Screen 1");
        }

        @Override
        public String getId() {
            return "singlesectionmenu";
        }

        @Override
        public int getSectionCount() {
            return 1;
        }

        @Nullable
        @Override
        public Section getSection(int index) {
            if (0 == index) {
                return mSection;
            } else {
                return null;
            }
        }

        @Nullable
        @Override
        public Section getSection(@NonNull SectionId sectionId) {
            if (sectionId.equals(mSection.getId())) {
                return mSection;
            } else {
                return null;
            }
        }

        @NonNull
        @Override
        public List<Section> getSections() {
            return Collections.singletonList(mSection);
        }
    }
}
