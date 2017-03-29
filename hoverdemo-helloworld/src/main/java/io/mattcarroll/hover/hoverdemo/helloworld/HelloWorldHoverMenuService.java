package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import org.codecanon.hover.hoverdemo.helloworld.R;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.defaulthovermenu.window.HoverMenuService;

/**
 * Extend {@link HoverMenuService} to get a Hover menu that displays the tabs and content
 * in your custom {@link HoverMenuAdapter}.
 */
public class HelloWorldHoverMenuService extends HoverMenuService {

    @Override
    protected HoverMenuAdapter createHoverMenuAdapter() {
        return new HelloWorldHoverMenuAdapter(getApplicationContext());
    }

    private static class HelloWorldHoverMenuAdapter implements HoverMenuAdapter {

        private final Context mContext;
        private final View mTab1;
        private final HoverMenuScreen mScreen1;
        private final View mTab2;
        private final HoverMenuScreen mScreen2;

        public HelloWorldHoverMenuAdapter(@NonNull Context context) {
            mContext = context.getApplicationContext();

            mTab1 = createTabView();
            mScreen1 = new HoverMenuScreen(mContext, "Screen 1");

            mTab2 = createTabView();
            mScreen2 = new HoverMenuScreen(mContext, "Screen 2");
        }

        private View createTabView() {
            int tabPadding = mContext.getResources().getDimensionPixelSize(R.dimen.tab_padding);
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background);
            imageView.setPadding(tabPadding, tabPadding, tabPadding, tabPadding);
            return imageView;
        }

        @Override
        public int getTabCount() {
            return 2;
        }

        @Override
        public long getTabId(int position) {
            return position;
        }

        @Override
        public View getTabView(int position) {
            switch (position) {
                case 0:
                    return mTab1;
                case 1:
                    return mTab2;
                default:
                    throw new RuntimeException("Hover menu tab was requested for non-existent screen.");
            }
        }

        @Override
        public NavigatorContent getNavigatorContent(int position) {
            switch (position) {
                case 0:
                    return mScreen1;
                case 1:
                    return mScreen2;
                default:
                    throw new RuntimeException("Hover menu screen was requested for non-existent screen.");
            }
        }

        @Override
        public void addContentChangeListener(@NonNull ContentChangeListener listener) {
            // No-op. We don't care about content changes in this demo.
        }

        @Override
        public void removeContentChangeListener(@NonNull ContentChangeListener listener) {
            // No-op. We don't care about content changes in this demo.
        }
    }
}
