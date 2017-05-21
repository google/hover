package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Context;
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
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.defaulthovermenu.window.HoverMenuService;

/**
 * Extend {@link HoverMenuService} to get a Hover menu that displays the tabs and content
 * in your custom {@link HoverMenuAdapter}.
 */
public class HelloWorldHoverMenuService extends HoverMenuService {

    @Override
    protected HoverMenuAdapter createHoverMenuAdapter() {
//        return new HelloWorldHoverMenuAdapter(getApplicationContext());
        return new MutatingAdapter(getApplicationContext());
    }

    private static class HelloWorldHoverMenuAdapter implements HoverMenuAdapter {

        private final Context mContext;
        private final View mTab1;
        private final HoverMenuScreen mScreen1;
        private final View mTab2;
        private final HoverMenuScreen mScreen2;
        private final View mTab3;
        private final HoverMenuScreen mScreen3;

        public HelloWorldHoverMenuAdapter(@NonNull Context context) {
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
        private final List<Long> mTabIds = new ArrayList<>();
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
            mTabIds.add(1L);
            mTabViews.add(createTabView());
            mTabContents.add(new HoverMenuScreen(mContext, "Screen 1"));

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMutationSteps.get(mNextStep).run();
                    ++mNextStep;

                    if (mNextStep < mMutationSteps.size()) {
                        new Handler(Looper.getMainLooper()).postDelayed(this, 400);
                    }
                }
            }, 2000);
        }

        private View createTabView() {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background);

            int tabSize = mContext.getResources().getDimensionPixelSize(R.dimen.tab_size);
            FrameLayout tabView = new FrameLayout(mContext);
            tabView.addView(imageView, new FrameLayout.LayoutParams(tabSize, tabSize));

            return tabView;
        }

        @Override
        public int getTabCount() {
            return mTabIds.size();
        }

        @Override
        public long getTabId(int position) {
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
            long id = (long) (mTabIds.size() + 1);
            mTabIds.add(position, id);
            mTabViews.add(createTabView());
            mTabContents.add(new HoverMenuScreen(mContext, "Screen " + id));

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
}
