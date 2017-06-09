package io.mattcarroll.hover.hoverdemo.helloworld;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
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
 * This demo menu adds and removes sections at scheduled times to demonstrate how Hover deals with
 * content coming and going.
 */
public class MutatingSectionsHoverMenuService extends HoverMenuService {

    private static final String TAG = "MutatingSectionsHoverMenuService";

    @Override
    protected HoverMenu createHoverMenu(@NonNull Intent intent) {
        return new MutatingHoverMenu(getApplicationContext());
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
                        insertTab(0);
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
                        removeTab(0);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        changeTab(1);
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
                        changeTab(0);
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

        private View createDifferentTabView() {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.tab_background_blue);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return imageView;
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

        private void changeTab(int position) {
            Section oldSection = mSections.get(position);
            Section newSection = new Section(
                    oldSection.getId(),
                    createDifferentTabView(),
                    new HoverMenuScreen(mContext, "This is a new screen!")
            );
            mSections.remove(position);
            mSections.add(position, newSection);

            notifyMenuChanged();
        }

        private void removeTab(int position) {
            mSections.remove(position);
            notifyMenuChanged();
        }
    }
}
