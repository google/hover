package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.NavigatorContent;

/**
 * TODO:
 */
class HoverMenu {

    private static final String TAG = "HoverMenu";

    private HoverMenuAdapter mAdapter;
    private List<Section> mSections = new ArrayList<>();
    private ListUpdateCallback mListUpdateCallback;

    public HoverMenu(@NonNull HoverMenuAdapter adapter) {
        mAdapter = adapter;
        mSections = createSections(mAdapter);

        mAdapter.addContentChangeListener(new HoverMenuAdapter.ContentChangeListener() {
            @Override
            public void onContentChange(@NonNull HoverMenuAdapter adapter) {
                updateSections();
            }
        });
    }

    private List<Section> createSections(@NonNull HoverMenuAdapter adapter) {
        List<Section> sections = new ArrayList<>();
        for (int i = 0; i < adapter.getTabCount(); ++i) {
            Section section = new Section(
                    new Section.SectionId(adapter.getTabId(i)),
                    adapter.getTabView(i),
                    adapter.getNavigatorContent(i)
            );

            Log.d(TAG, "Creating new Section: " + (i) + ", ID: " + section.getId());
            Log.d(TAG, " - tab View: " + section.getTabView().hashCode());
            Log.d(TAG, " - screen: " + section.getContent().hashCode());

            sections.add(section);
        }
        return sections;
    }

    private void updateSections() {
        List<Section> oldSections = mSections;
        List<Section> newSections = createSections(mAdapter);
        mSections = newSections;

        if (null != mListUpdateCallback) {
            DiffUtil.Callback diffCallback = new MenuDiffCallback(oldSections, newSections);
            // calculateDiff() can be long-running.  We let it run synchronously because we don't
            // expect many Sections.
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(diffCallback, true);
            result.dispatchUpdatesTo(mListUpdateCallback);
        }
    }

    public int getSectionCount() {
        return mSections.size();
    }

    public Section getSection(int index) {
        return mSections.get(index);
    }

    public void setUpdatedCallback(@Nullable ListUpdateCallback listUpdatedCallback) {
        mListUpdateCallback = listUpdatedCallback;
    }

    public static class Section {

        private final SectionId mId;
        private final View mTabView;
        private final NavigatorContent mContent;

        private Section(@NonNull SectionId id, @NonNull View tabView, @NonNull NavigatorContent content) {
            mId = id;
            mTabView = tabView;
            mContent = content;
        }


        @NonNull
        public SectionId getId() {
            return mId;
        }

        @NonNull
        public View getTabView() {
            return mTabView;
        }

        @NonNull
        public NavigatorContent getContent() {
            return mContent;
        }

        public static class SectionId {

            private String mId;

            private SectionId(@NonNull String id) {
                mId = id;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                SectionId sectionId = (SectionId) o;

                return mId.equals(sectionId.mId);

            }

            @Override
            public int hashCode() {
                return mId.hashCode();
            }

            @Override
            public String toString() {
                return mId;
            }
        }
    }

    private static class MenuDiffCallback extends DiffUtil.Callback {

        private final List<Section> mOldList;
        private final List<Section> mNewList;

        private MenuDiffCallback(@NonNull List<Section> oldList, @NonNull List<Section> newList) {
            mOldList = oldList;
            mNewList = newList;
        }

        @Override
        public int getOldListSize() {
            return mOldList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldList.get(oldItemPosition).getId().equals(mNewList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Section oldSection = mOldList.get(oldItemPosition);
            Section newSection = mNewList.get(newItemPosition);

            return oldSection.mTabView.equals(newSection.getTabView()) &&
                    oldSection.getContent().equals(newSection.getContent());
        }
    }
}
