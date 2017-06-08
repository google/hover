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
    protected HoverMenu createHoverMenu(@NonNull Intent intent) {
        String menuType = intent.getStringExtra(EXTRA_MENU_TYPE);

        if (TYPE_REORDERING_SECTIONS.equals(menuType)) {
            return new ReorderingSectionHoverMenu(getApplicationContext());
        } else if (TYPE_MULTI_SECTIONS.equals(menuType)) {
            return new MultiSectionHoverMenu(getApplicationContext());
        } else if (TYPE_CHANGING_SECTIONS.equals(menuType)) {
            return new MutatingHoverMenu(getApplicationContext());
        } else {
            return new SingleSectionHoverMenu(getApplicationContext());
        }
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

    private static class MutatingHoverMenu extends HoverMenu {

        private final Context mContext;
        private final List<Section> mSections = new ArrayList<>();

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

        MutatingHoverMenu(@NonNull Context context) {
            mContext = context;

            insertTab(0);

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
        public String getId() {
            return "mutatingmenu";
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

        private void insertTab(int position) {
            String id = Integer.toString(mSections.size());
            mSections.add(position, new Section(
                    new SectionId(id),
                    createTabView(),
                    new HoverMenuScreen(mContext, "Screen " + id)
            ));
            notifyMenuChanged();
        }

        private void removeTab(int position) {
            mSections.remove(position);
            notifyMenuChanged();
        }
    }

    private static class ReorderingSectionHoverMenu extends HoverMenu {

        private final Context mContext;
        private final List<Section> mSections = new ArrayList<>();

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

        ReorderingSectionHoverMenu(@NonNull Context context) {
            mContext = context;

            insertTab(0);
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
        public String getId() {
            return "reorderingmenu";
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

        private void insertTab(int position) {
            String id = Integer.toString(mSections.size());
            mSections.add(position, new Section(
                    new SectionId(id),
                    createTabView(),
                    new HoverMenuScreen(mContext, "Screen " + id)
            ));
            notifyMenuChanged();
        }

        private void moveTab(int startPosition, int endPosition) {
            Section section = mSections.remove(startPosition);
            mSections.add(endPosition, section);
            notifyMenuChanged();
        }
    }
}
