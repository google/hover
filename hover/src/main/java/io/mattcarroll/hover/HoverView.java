/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mattcarroll.hover;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.mattcarroll.hover.view.InViewDragger;
import io.mattcarroll.hover.window.InWindowDragger;
import io.mattcarroll.hover.window.WindowViewController;

import static io.mattcarroll.hover.SideDock.SidePosition.LEFT;

/**
 * {@code HoverMenuView} is a floating menu implementation. This implementation displays tabs along
 * the top of its display, from right to left. Below the tabs, filling the remainder of the display
 * is a content region that displays the content for a selected tab.  The content region includes
 * a visual indicator showing which tab is currently selected.  Each tab's content includes a title
 * and a visual area.  The visual area can display any {@link Content}.
 */
public class HoverView extends RelativeLayout {

    private static final String TAG = "HoverView";

    private static final String PREFS_FILE = "hover";
    private static final String SAVED_STATE_DOCK_POSITION = "_dock_position";
    private static final String SAVED_STATE_DOCKS_SIDE = "_dock_side";
    private static final String SAVED_STATE_SELECTED_SECTION = "_selected_section";

    @NonNull
    public static HoverView createForWindow(@NonNull Context context,
                                            @NonNull WindowViewController windowViewController) {
        return createForWindow(context, windowViewController, null);
    }

    @NonNull
    public static HoverView createForWindow(@NonNull Context context,
                                            @NonNull WindowViewController windowViewController,
                                            @Nullable SideDock.SidePosition initialDockPosition) {
        Dragger dragger = createWindowDragger(context, windowViewController);
        return new HoverView(context, dragger, windowViewController, initialDockPosition);
    }

    @NonNull
    private static Dragger createWindowDragger(@NonNull Context context,
                                               @NonNull WindowViewController windowViewController) {
        int touchDiameter = context.getResources().getDimensionPixelSize(R.dimen.hover_exit_radius);
        int slop = ViewConfiguration.get(context).getScaledTouchSlop();
        return new InWindowDragger(
                context,
                windowViewController,
                touchDiameter,
                slop
        );
    }

    @NonNull
    public static HoverView createForView(@NonNull Context context) {
        return new HoverView(context, null);
    }

    final HoverViewState mClosed = new HoverViewStateClosed();
    final HoverViewState mCollapsed = new HoverViewStateCollapsed();
    final HoverViewState mExpanded = new HoverViewStateExpanded();
    final WindowViewController mWindowViewController;
    final Dragger mDragger;
    final Screen mScreen;
    HoverViewState mState;
    HoverMenu mMenu;
    HoverMenu.SectionId mSelectedSectionId;
    SideDock mCollapsedDock;
    boolean mIsAddedToWindow;
    boolean mIsTouchableInWindow;
    boolean mIsDebugMode = false;
    int mTabSize;
    OnExitListener mOnExitListener;
    final Set<Listener> mListeners = new CopyOnWriteArraySet<>();

    // Public for use with XML inflation. Clients should use static methods for construction.
    public HoverView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mDragger = createInViewDragger(context);
        mScreen = new Screen(this);
        mWindowViewController = null;

        init();

        if (null != attrs) {
            applyAttributes(attrs);
        }
    }

    @NonNull
    private Dragger createInViewDragger(@NonNull Context context) {
        int touchDiameter = context.getResources().getDimensionPixelSize(R.dimen.hover_exit_radius);
        int slop = ViewConfiguration.get(context).getScaledTouchSlop();
        return new InViewDragger(
                this,
                touchDiameter,
                slop
        );
    }

    private HoverView(@NonNull Context context,
                      @NonNull Dragger dragger,
                      @Nullable WindowViewController windowViewController,
                      @Nullable SideDock.SidePosition initialDockPosition) {
        super(context);
        mDragger = dragger;
        mScreen = new Screen(this);
        mWindowViewController = windowViewController;

        init();

        if (null != initialDockPosition) {
            mCollapsedDock = new SideDock(
                    this,
                    mTabSize,
                    initialDockPosition
            );
        }
    }

    private void applyAttributes(@NonNull AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HoverView);
        try {
            createCollapsedDockFromAttrs(a);
        } finally {
            a.recycle();
        }
    }

    private void createCollapsedDockFromAttrs(@NonNull TypedArray a) {
        int tabSize = getResources().getDimensionPixelSize(R.dimen.hover_tab_size);
        @SideDock.SidePosition.Side
        int dockSide = a.getInt(R.styleable.HoverView_dockSide, LEFT);
        float dockPosition = a.getFraction(R.styleable.HoverView_dockPosition, 1, 1, 0.5f);
        SideDock.SidePosition sidePosition = new SideDock.SidePosition(dockSide, dockPosition);
        mCollapsedDock = new SideDock(
                this,
                tabSize,
                sidePosition
        );
    }

    private void init() {
        mTabSize = getResources().getDimensionPixelSize(R.dimen.hover_tab_size);
        restoreVisualState();
        setFocusableInTouchMode(true); // For handling hardware back button presses.
        setState(new HoverViewStateClosed());
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        // Intercept the hardware back button press if needed. When it's pressed, we'll collapse.
        if (mState.respondsToBackButton() && KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            KeyEvent.DispatcherState state = getKeyDispatcherState();
            if (state != null) {
                if (KeyEvent.ACTION_DOWN == event.getAction()) {
                    state.startTracking(event, this);
                    return true;
                } else if (KeyEvent.ACTION_UP == event.getAction()) {
                    onBackPressed();
                    return true;
                }
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        VisualState visualState = new VisualState(superState);
        visualState.save(this);
        return visualState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        VisualState visualState = (VisualState) state;
        super.onRestoreInstanceState(visualState.getSuperState());

        visualState.restore(this);
    }

    public void saveVisualState() {
        if (null == mMenu) {
            // Nothing to save.
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        PersistentState persistentState = new PersistentState(prefs);
        persistentState.save(mMenu, mCollapsedDock.sidePosition(), mSelectedSectionId);
    }

    void restoreVisualState() {
        if (null == mMenu) {
            Log.d(TAG, "Tried to restore visual state but no menu set.");
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        PersistentState persistentState = new PersistentState(prefs);
        persistentState.restore(this, mMenu);
    }

    // TODO: when to call this?
    public void release() {
        Log.d(TAG, "Released.");
        mDragger.deactivate();
        // TODO: should we also release the screen?
    }

    public void enableDebugMode(boolean debugMode) {
        mIsDebugMode = debugMode;

        mDragger.enableDebugMode(debugMode);
        mScreen.enableDrugMode(debugMode);
    }

    void setState(@NonNull HoverViewState state) {
        mState = state;
        mState.takeControl(this);
    }

    private void onBackPressed() {
        mState.onBackPressed();
    }

    public void setMenu(@Nullable HoverMenu menu) {
        mState.setMenu(menu);
    }

    public void expand() {
        mState.expand();
    }

    public void collapse() {
        mState.collapse();
    }

    public void close() {
        mState.close();
    }

    public void setOnExitListener(@Nullable OnExitListener listener) {
        mOnExitListener = listener;
    }

    public void addOnExpandAndCollapseListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    public void removeOnExpandAndCollapseListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    void notifyListenersExpanding() {
        Log.d(TAG, "Notifying listeners that Hover is expanding.");
        for (Listener listener : mListeners) {
            listener.onExpanding();
        }
    }

    void notifyListenersExpanded() {
        Log.d(TAG, "Notifying listeners that Hover is now expanded.");
        for (Listener listener : mListeners) {
            listener.onExpanded();
        }
    }

    void notifyListenersCollapsing() {
        Log.d(TAG, "Notifying listeners that Hover is collapsing.");
        for (Listener listener : mListeners) {
            listener.onCollapsing();
        }
    }

    void notifyListenersCollapsed() {
        Log.d(TAG, "Notifying listeners that Hover is now collapsed.");
        for (Listener listener : mListeners) {
            listener.onCollapsed();
        }
    }

    void notifyListenersClosing() {
        Log.d(TAG, "Notifying listeners that Hover is closing.");
        for (Listener listener : mListeners) {
            listener.onClosing();
        }
    }

    void notifyListenersClosed() {
        Log.d(TAG, "Notifying listeners that Hover is closed.");
        for (Listener listener : mListeners) {
            listener.onClosed();
        }
    }

    // Only call this if using HoverMenuView directly in a window.
    public void addToWindow() {
        mState.addToWindow();
    }

    // Only call this if using HoverMenuView directly in a window.
    public void removeFromWindow() {
        mState.removeFromWindow();
    }

    void makeTouchableInWindow() {
        mState.makeTouchableInWindow();
    }

    void makeUntouchableInWindow() {
        mState.makeUntouchableInWindow();
    }

    // State of the HoverMenuView that is persisted across configuration change and other brief OS
    // interruptions.  This state is written/read when HoverMenuView's onSaveInstanceState() and
    // onRestoreInstanceState() are called.
    @SuppressWarnings("WeakerAccess")
    private static class VisualState extends BaseSavedState {

        @SuppressWarnings("unused")
        private static final Parcelable.Creator<VisualState> CREATOR = new Creator<VisualState>() {
            @Override
            public VisualState createFromParcel(Parcel source) {
                return new VisualState(source);
            }

            @Override
            public VisualState[] newArray(int size) {
                return new VisualState[size];
            }
        };

        private SideDock.SidePosition mSidePosition;
        private HoverMenu.SectionId mSelectedSectionId;

        VisualState(Parcelable superState) {
            super(superState);
        }

        VisualState(Parcel in) {
            super(in);

            if (in.dataAvail() > 0) {
                int side = in.readInt();
                float dockVerticalPositionPercent = in.readFloat();
                mSidePosition = new SideDock.SidePosition(side, dockVerticalPositionPercent);
            }
            if (in.dataAvail() > 0) {
                mSelectedSectionId = new HoverMenu.SectionId(in.readString());
            }
        }

        public void save(@NonNull HoverView hoverView) {
            setSidePosition(hoverView.mCollapsedDock.sidePosition());
            setSelectedSectionId(hoverView.mSelectedSectionId);

            Log.d(TAG, "Saving instance state. Dock side: " + mSidePosition
                    + ", Selected section: " + mSelectedSectionId);
        }

        private void setSidePosition(@Nullable SideDock.SidePosition sidePosition) {
            mSidePosition = sidePosition;
        }

        private void setSelectedSectionId(@Nullable HoverMenu.SectionId selectedSectionId) {
            mSelectedSectionId = selectedSectionId;
        }

        public void restore(@NonNull HoverView hoverView) {
            SideDock.SidePosition sidePosition = getSidePosition();
            hoverView.mCollapsedDock = new SideDock(
                    hoverView,
                    hoverView.mTabSize,
                    sidePosition
            );

            HoverMenu.SectionId savedSelectedSectionId = getSelectedSectionId();
            Log.d(TAG, "Restoring instance state. Dock: " + hoverView.mCollapsedDock
                    + ", Selected section: " + savedSelectedSectionId);

            // If no menu is set on this HoverMenuView then we should hold onto this saved section
            // selection in case we get a menu that has this section.  If we do have a menu set on
            // this HoverMenuView, then we should only restore this selection if the given section
            // exists in our menu.
            if (null == hoverView.mMenu
                    || (null != savedSelectedSectionId && null != hoverView.mMenu.getSection(savedSelectedSectionId))) {
                mSelectedSectionId = savedSelectedSectionId;
            }
        }

        @Nullable
        private SideDock.SidePosition getSidePosition() {
            return mSidePosition;
        }

        @Nullable
        private HoverMenu.SectionId getSelectedSectionId() {
            return mSelectedSectionId;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            if (null != mSidePosition) {
                out.writeInt(mSidePosition.getSide());
                out.writeFloat(mSidePosition.getVerticalDockPositionPercentage());
            }
        }
    }

    // State of the HoverMenuView that is persisted to disk.  PersistentState is used to restore a
    // HoverMenuView's state across different application runs.
    @SuppressWarnings("WeakerAccess")
    private static class PersistentState {

        private final SharedPreferences mPrefs;

        PersistentState(@NonNull SharedPreferences prefs) {
            mPrefs = prefs;
        }

        public void restore(@NonNull HoverView hoverView, @NonNull HoverMenu menu) {
            SideDock.SidePosition sidePosition = getSidePosition(menu.getId());
            hoverView.mCollapsedDock = new SideDock(
                    hoverView,
                    hoverView.mTabSize,
                    sidePosition
            );

            HoverMenu.SectionId selectedSectionId = getSelectedSectionId(menu.getId());
            hoverView.mSelectedSectionId = selectedSectionId;

            Log.d(TAG, "Restoring from PersistentState. Position: "
                    + sidePosition.getVerticalDockPositionPercentage()
                    + ", Side: " + sidePosition
                    + ", Section ID: " + selectedSectionId);
        }

        private SideDock.SidePosition getSidePosition(@NonNull String menuId) {
            return new SideDock.SidePosition(
                    mPrefs.getInt(menuId + SAVED_STATE_DOCKS_SIDE, LEFT),
                    mPrefs.getFloat(menuId + SAVED_STATE_DOCK_POSITION, 0.5f)
            );
        }

        private HoverMenu.SectionId getSelectedSectionId(@NonNull String menuId) {
            return mPrefs.contains(menuId + SAVED_STATE_SELECTED_SECTION)
                    ? new HoverMenu.SectionId(mPrefs.getString(menuId + SAVED_STATE_SELECTED_SECTION, null))
                    : null;
        }

        public void save(@NonNull HoverMenu menu,
                         @NonNull SideDock.SidePosition sidePosition,
                         @Nullable HoverMenu.SectionId sectionId) {
            String menuId = menu.getId();
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putFloat(menuId + SAVED_STATE_DOCK_POSITION, sidePosition.getVerticalDockPositionPercentage());
            editor.putInt(menuId + SAVED_STATE_DOCKS_SIDE, sidePosition.getSide());
            editor.putString(menuId + SAVED_STATE_SELECTED_SECTION, null != sectionId ? sectionId.toString() : null);
            editor.apply();

            Log.d(TAG, "saveVisualState(). Position: "
                    + sidePosition.getVerticalDockPositionPercentage()
                    + ", Side: " + sidePosition.getSide()
                    + ", Section ID: " + sectionId);
        }

    }

    /**
     * Listener invoked when the corresponding transitions occur within a given {@link HoverView}.
     */
    public interface Listener {

        void onExpanding();

        void onExpanded();

        void onCollapsing();

        void onCollapsed();

        void onClosing();

        void onClosed();

    }
}
