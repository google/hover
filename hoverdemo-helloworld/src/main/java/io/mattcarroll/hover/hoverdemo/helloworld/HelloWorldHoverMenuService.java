package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.codecanon.hover.hoverdemo.helloworld.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.content.NavigatorContent;
import io.mattcarroll.hover.defaulthovermenu.window.HoverMenuService;

/**
 * Extend {@link HoverMenuService} to get a Hover menu that displays the tabs and content
 * in your custom {@link HoverMenuAdapter}.
 */
public class HelloWorldHoverMenuService extends HoverMenuService {

    private static final String TAG = "HelloWorldHoverMenuService";

    private static final String EXTRA_MENU_TYPE = "menu_type";
    private static final String TYPE_SINGLE_SECTION = "single_section";
    private static final String TYPE_MULTI_SECTIONS = "multi_sections";
    private static final String TYPE_CHANGING_SECTIONS = "changing_sections";
    private static final String TYPE_REORDERING_SECTIONS = "reordering_sections";

    public static Intent intentForSingleSection(@NonNull Context context) {
        return new Intent(context, HelloWorldHoverMenuService.class)
                .putExtra(EXTRA_MENU_TYPE, TYPE_SINGLE_SECTION);
    }

    public static Intent intentForMultiSection(@NonNull Context context) {
        return new Intent(context, HelloWorldHoverMenuService.class)
                .putExtra(EXTRA_MENU_TYPE, TYPE_MULTI_SECTIONS);
    }

    public static Intent intentForChangingSections(@NonNull Context context) {
        return new Intent(context, HelloWorldHoverMenuService.class)
                .putExtra(EXTRA_MENU_TYPE, TYPE_CHANGING_SECTIONS);
    }

    public static Intent intentForReorderingSections(@NonNull Context context) {
        return new Intent(context, HelloWorldHoverMenuService.class)
                .putExtra(EXTRA_MENU_TYPE, TYPE_REORDERING_SECTIONS);
    }

    @Override
    protected HoverMenuAdapter createHoverMenuAdapter(@NonNull Intent intent) {
        String menuType = intent.getStringExtra(EXTRA_MENU_TYPE);

        if (TYPE_CHANGING_SECTIONS.equals(menuType)) {
            return new MutatingAdapter(getApplicationContext());
        } else if (TYPE_MULTI_SECTIONS.equals(menuType)) {
            return new MultiSectionHoverMenuAdapter(getApplicationContext());
        } else if (TYPE_REORDERING_SECTIONS.equals(menuType)) {
            return new ReorderingSectionHoverMenuAdapter(getApplicationContext());
        } else {
            return new SingleSectionHoverMenuAdapter(getApplicationContext());
        }
    }

    private static class SingleSectionHoverMenuAdapter implements HoverMenuAdapter {

        private final Context mContext;
        private final View mTab1;
        private final HoverMenuScreen mScreen1;

        public SingleSectionHoverMenuAdapter(@NonNull Context context) {
            mContext = context.getApplicationContext();

            mTab1 = createTabView();
            mScreen1 = new HoverMenuScreen(mContext, "Screen 1");
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
        public int getTabCount() {
            return 1;
        }

        @Override
        public String getTabId(int position) {
            return Integer.toString(position);
        }

        @Override
        public View getTabView(int position) {
                return mTab1;
        }

        @Override
        public NavigatorContent getNavigatorContent(int position) {
                return mScreen1;
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

    private static class MultiSectionHoverMenuAdapter implements HoverMenuAdapter {

        private final Context mContext;
        private final View mTab1;
        private final HoverMenuScreen mScreen1;
        private final View mTab2;
        private final HoverMenuScreen mScreen2;
        private final View mTab3;
        private final HoverMenuScreen mScreen3;

        public MultiSectionHoverMenuAdapter(@NonNull Context context) {
            mContext = context.getApplicationContext();

            mTab1 = createTabView();
            mScreen1 = new HoverMenuScreen(mContext, "Screen 1");

            mTab2 = createTabView();
            mScreen2 = new HoverMenuScreen(mContext, "Screen 2");

            mTab3 = createTabView();
            mScreen3 = new HoverMenuScreen(mContext, "Screen 3");
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
        public int getTabCount() {
            return 3;
        }

        @Override
        public String getTabId(int position) {
            return Integer.toString(position);
        }

        @Override
        public View getTabView(int position) {
            switch (position) {
                case 0:
                    return mTab1;
                case 1:
                    return mTab2;
                case 2:
                    return mTab3;
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
                case 2:
                    return mScreen3;
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

    private static class MutatingAdapter implements HoverMenuAdapter {

        private final Context mContext;
        private final List<String> mTabIds = new ArrayList<>();
        private final List<View> mTabViews = new ArrayList<>();
        private final List<NavigatorContent> mTabContents = new ArrayList<>();
        private final Set<ContentChangeListener> mContentChangeListeners = new CopyOnWriteArraySet<>();

        private int mNextStep = 0;
        private final List<Runnable> mMutationSteps = Arrays.asList(
                new Runnable() {
                    @Override
                    public void run() {
                        insertTab(1);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        insertTab(1);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        insertTab(1);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        insertTab(1);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        insertTab(1);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        removeTab(3);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        removeTab(1);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        removeTab(1);
                    }
                }
        );

        MutatingAdapter(@NonNull Context context) {
            mContext = context;
            mTabIds.add("PRIMARY");
            mTabViews.add(createTabView());
            mTabContents.add(new HoverMenuScreen(mContext, "Screen 1"));

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMutationSteps.get(mNextStep).run();
                    ++mNextStep;

                    if (mNextStep < mMutationSteps.size()) {
                        new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
                    }
                }
            }, 2000);
        }

        private View createTabView() {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return imageView;

//            int tabSize = mContext.getResources().getDimensionPixelSize(R.dimen.tab_size);
//            FrameLayout tabView = new FrameLayout(mContext);
//            tabView.addView(imageView, new FrameLayout.LayoutParams(tabSize, tabSize));
//
//            Log.d(TAG, "Created new tab view: " + tabView.hashCode());
//            return tabView;
        }

        @Override
        public int getTabCount() {
            return mTabIds.size();
        }

        @Override
        public String getTabId(int position) {
            return mTabIds.get(position);
        }

        @Override
        public View getTabView(int position) {
            return mTabViews.get(position);
        }

        @Override
        public NavigatorContent getNavigatorContent(int position) {
            return mTabContents.get(position);
        }

        @Override
        public void addContentChangeListener(@NonNull ContentChangeListener listener) {
            mContentChangeListeners.add(listener);
        }

        @Override
        public void removeContentChangeListener(@NonNull ContentChangeListener listener) {
            mContentChangeListeners.remove(listener);
        }

        private void insertTab(int position) {
            String id = Integer.toString(mTabIds.size() + 1);
            mTabIds.add(position, id);
            mTabViews.add(position, createTabView());
            mTabContents.add(position, new HoverMenuScreen(mContext, "Screen " + id));

            for (ContentChangeListener listener : mContentChangeListeners) {
                listener.onContentChange(this);
            }
        }

        private void removeTab(int position) {
            mTabIds.remove(position);
            mTabViews.remove(position);
            mTabContents.remove(position);

            for (ContentChangeListener listener : mContentChangeListeners) {
                listener.onContentChange(this);
            }
        }
    }

    private static class ReorderingSectionHoverMenuAdapter implements HoverMenuAdapter {

        private final Context mContext;
        private final List<String> mTabIds = new ArrayList<>();
        private final List<View> mTabViews = new ArrayList<>();
        private final List<NavigatorContent> mTabContents = new ArrayList<>();
        private final Set<ContentChangeListener> mContentChangeListeners = new CopyOnWriteArraySet<>();

        private int mNextStep = 0;
        private final List<Runnable> mMutationSteps = Arrays.asList(
                new Runnable() {
                    @Override
                    public void run() {
                        moveTab(1, 2);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        moveTab(3, 1);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        moveTab(2, 3);
                    }
                }
        );

        ReorderingSectionHoverMenuAdapter(@NonNull Context context) {
            mContext = context;
            mTabIds.add("PRIMARY");
            mTabViews.add(createTabView());
            mTabContents.add(new HoverMenuScreen(mContext, "Screen 1"));

            insertTab(1);
            insertTab(2);
            insertTab(3);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMutationSteps.get(mNextStep).run();
                    ++mNextStep;

                    if (mNextStep < mMutationSteps.size()) {
                        new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
                    }
                }
            }, 5000);
        }

        private View createTabView() {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return imageView;

//            int tabSize = mContext.getResources().getDimensionPixelSize(R.dimen.tab_size);
//            FrameLayout tabView = new FrameLayout(mContext);
//            tabView.addView(imageView, new FrameLayout.LayoutParams(tabSize, tabSize));
//
//            Log.d(TAG, "Created new tab view: " + tabView.hashCode());
//            return tabView;
        }

        @Override
        public int getTabCount() {
            return mTabIds.size();
        }

        @Override
        public String getTabId(int position) {
            return mTabIds.get(position);
        }

        @Override
        public View getTabView(int position) {
            return mTabViews.get(position);
        }

        @Override
        public NavigatorContent getNavigatorContent(int position) {
            return mTabContents.get(position);
        }

        @Override
        public void addContentChangeListener(@NonNull ContentChangeListener listener) {
            mContentChangeListeners.add(listener);
        }

        @Override
        public void removeContentChangeListener(@NonNull ContentChangeListener listener) {
            mContentChangeListeners.remove(listener);
        }

        private void insertTab(int position) {
            String id = Integer.toString(mTabIds.size() + 1);
            mTabIds.add(position, id);
            mTabViews.add(position, createTabView());
            mTabContents.add(position, new HoverMenuScreen(mContext, "Screen " + id));

            for (ContentChangeListener listener : mContentChangeListeners) {
                listener.onContentChange(this);
            }
        }

        private void moveTab(int startPosition, int endPosition) {
            String tabId = mTabIds.remove(startPosition);
            mTabIds.add(endPosition, tabId);

            View tabView = mTabViews.remove(startPosition);
            mTabViews.add(endPosition, tabView);

            NavigatorContent content = mTabContents.remove(startPosition);
            mTabContents.add(endPosition, content);

            for (ContentChangeListener listener : mContentChangeListeners) {
                listener.onContentChange(this);
            }
        }
    }
}
