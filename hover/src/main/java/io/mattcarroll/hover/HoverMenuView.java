package io.mattcarroll.hover;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import io.mattcarroll.hover.view.InViewDragger;
import io.mattcarroll.hover.window.InWindowDragger;
import io.mattcarroll.hover.window.WindowViewController;

/**
 * {@code HoverMenuView} is a floating menu implementation. This implementation displays tabs along
 * the top of its display, from right to left. Below the tabs, filling the remainder of the display
 * is a content region that displays the content for a selected tab.  The content region includes
 * a visual indicator showing which tab is currently selected.  Each tab's content includes a title
 * and a visual area.  The visual area can display any {@code View}.
 *
 * {@code HoverMenuView} cannot be used in XML because it requires additional parameters in its
 * constructor.
 */
public class HoverMenuView extends RelativeLayout {

    private static final String TAG = "HoverMenuView";

    private static final String PREFS_FILE = "HoverMenuView";
    private static final String SAVED_STATE_DOCK_POSITION = "_dock_position";
    private static final String SAVED_STATE_DOCKS_SIDE = "_dock_side";
    private static final String SAVED_STATE_SELECTED_SECTION = "_selected_section";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ LEFT, RIGHT })
    private @interface DockSide { }
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    @NonNull
    public static HoverMenuView createForWindow(@NonNull Context context,
                                                @NonNull WindowViewController windowViewController) {
        return createForWindow(context, windowViewController, null);
    }

    @NonNull
    public static HoverMenuView createForWindow(@NonNull Context context,
                                                @NonNull WindowViewController windowViewController,
                                                @Nullable SideDock initialDock) {
        int touchDiameter = context.getResources().getDimensionPixelSize(R.dimen.hover_exit_radius);
        int slop = ViewConfiguration.get(context).getScaledTouchSlop();
        InWindowDragger dragger = new InWindowDragger(
                context,
                windowViewController,
                touchDiameter,
                slop
        );

        return new HoverMenuView(context, dragger, windowViewController, initialDock);
    }

    @NonNull
    public static HoverMenuView createForView(@NonNull Context context) {
        return new HoverMenuView(context, null);
    }

    private final WindowViewController mWindowViewController;
    private final Dragger mDragger;
    private final Screen mScreen;
    private HoverMenuViewStateCollapsed mCollapsedMenu;
    private SideDock mCollapsedDock;
    private HoverMenuViewStateExpanded mExpandedMenu;
    private HoverMenu.SectionId mSelectedSectionId;
    private HoverMenu mMenu;
    private boolean mIsAddedToWindow;
    private boolean mIsExpanded = false;
    private boolean mIsDebugMode = false;
    private OnExitListener mOnExitListener;
    private final Set<OnExpandAndCollapseListener> mOnExpandAndCollapseListeners = new CopyOnWriteArraySet<>();

    // Public for use with XML inflation. Clients should use static methods for construction.
    public HoverMenuView(@NonNull Context context,
                          @Nullable AttributeSet attrs) {
        super(context, attrs);
        int touchDiameter = context.getResources().getDimensionPixelSize(R.dimen.hover_exit_radius);
        int slop = ViewConfiguration.get(context).getScaledTouchSlop();
        mDragger = new InViewDragger(
                this,
                touchDiameter,
                slop
        );
        mScreen = new Screen(this);
        mWindowViewController = null;

        if (null != attrs) {
            applyAttributes(attrs);
        }

        init();
    }

    private HoverMenuView(@NonNull Context context,
                          @NonNull Dragger dragger,
                          @Nullable WindowViewController windowViewController,
                          @Nullable SideDock initialDockPosition) {
        super(context);
        mDragger = dragger;
        mScreen = new Screen(this);
        mWindowViewController = windowViewController;
        mCollapsedDock = initialDockPosition;
        init();
    }

    private void applyAttributes(@NonNull AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HoverMenuView);

        try {
            @DockSide
            int dockSide = a.getInt(R.styleable.HoverMenuView_dockSide, LEFT);
            float dockPosition = a.getFraction(R.styleable.HoverMenuView_dockPosition, 1, 1, 0.5f);
            mCollapsedDock = new SideDock(dockPosition, LEFT == dockSide ? SideDock.LEFT : SideDock.RIGHT);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        restoreVisualState();
        setFocusableInTouchMode(true); // For handling hardware back button presses.
    }

    // TODO: when to call this?
    public void release() {
        Log.d(TAG, "Released.");
        mDragger.deactivate();
        // TODO: should we also release the screen?
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                removeOnLayoutChangeListener(this);
                initAfterInitialLayout();
            }
        });
    }

    private void initAfterInitialLayout() {
        // Start collapsed.
        if (null != mWindowViewController) {
            mWindowViewController.makeUntouchable(this);
        }
        if (null == mSelectedSectionId) {
            mSelectedSectionId = mMenu.getSection(0).getId();
        }
        createCollapsedMenu();
        mCollapsedMenu.takeControl(mScreen, mSelectedSectionId.toString());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        if (null != mCollapsedMenu) {
            mCollapsedDock = mCollapsedMenu.getDock();
        }
        Log.d(TAG, "onSaveInstanceState(). Dock: " + mCollapsedDock
                + ", Selected section: " + mSelectedSectionId);
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.setCollapsedDock(mCollapsedDock);
        savedState.setSelectedSectionId(mSelectedSectionId);
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCollapsedDock = savedState.getCollapsedDock();
        HoverMenu.SectionId savedSelectedSectionId = savedState.getSelectedSectionId();
        Log.d(TAG, "onRestoreInstanceState(). Dock: " + mCollapsedDock
                + ", Selected section: " + savedSelectedSectionId);

        // If no menu is set on this HoverMenuView then we should hold onto this saved section
        // selection in case we get a menu that has this section.  If we do have a menu set on
        // this HoverMenuView, then we should only restore this selection if the given section
        // exists in our menu.
        if (null == mMenu || (null != savedSelectedSectionId && null != mMenu.getSection(savedSelectedSectionId))) {
            mSelectedSectionId = savedSelectedSectionId;
        }
    }

    public void saveVisualState() {
        if (null != mCollapsedMenu) {
            mCollapsedDock = mCollapsedMenu.getDock();
        }
        SharedPreferences.Editor editor = getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE).edit();
        editor.putFloat(mMenu.getId() + SAVED_STATE_DOCK_POSITION, mCollapsedDock.getVerticalDockPositionPercentage());
        editor.putInt(mMenu.getId() + SAVED_STATE_DOCKS_SIDE, mCollapsedDock.getSide());
        editor.putString(mMenu.getId() + SAVED_STATE_SELECTED_SECTION, null != mSelectedSectionId ? mSelectedSectionId.toString() : null);
        editor.apply();

        Log.d(TAG, "saveVisualState(). Position: "
                + mCollapsedDock.getVerticalDockPositionPercentage()
                + ", Side: " + mCollapsedDock.getSide()
                + ", Section ID: " + mSelectedSectionId);
    }

    private void restoreVisualState() {
        if (null != mMenu) {
            SharedPreferences savedState = getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

            mCollapsedDock = new SideDock(
                    savedState.getFloat(mMenu.getId() + SAVED_STATE_DOCK_POSITION, 0.5f),
                    savedState.getInt(mMenu.getId() + SAVED_STATE_DOCKS_SIDE, SideDock.LEFT)
            );
            mSelectedSectionId = savedState.contains(mMenu.getId() + SAVED_STATE_SELECTED_SECTION)
                    ? new HoverMenu.SectionId(savedState.getString(mMenu.getId() + SAVED_STATE_SELECTED_SECTION, null))
                    : null;

            Log.d(TAG, "restoreStateFromBundle(). Position: "
                    + mCollapsedDock.getVerticalDockPositionPercentage()
                    + ", Side: " + mCollapsedDock.getSide()
                    + ", Section ID: " + mSelectedSectionId);
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        // Intercept the hardware back button press if we're expanded. When it's
        // pressed, we'll collapse.
        if (mIsExpanded && KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
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

    private void onBackPressed() {
        collapse();
    }

    public void enableDebugMode(boolean debugMode) {
        mIsDebugMode = debugMode;

        mDragger.enableDebugMode(debugMode);
        mScreen.enableDrugMode(debugMode);
    }

    public void setMenu(@Nullable HoverMenu menu) {
        mMenu = menu;

        restoreVisualState();

        if (null == mSelectedSectionId || null == mMenu.getSection(mSelectedSectionId)) {
            mSelectedSectionId = mMenu.getSection(0).getId();
        }
    }

    private void expand() {
        if (!mIsExpanded) {
            mIsExpanded = true;
            if (null != mWindowViewController) {
                mWindowViewController.makeTouchable(this);
            }
            requestFocus(); // For handling hardware back button presses.

            createExpandedMenu();

            mCollapsedDock = mCollapsedMenu.getDock();
            mCollapsedMenu.giveControlTo(mExpandedMenu);
            mCollapsedMenu = null;
        }
    }

    private void createExpandedMenu() {
        Log.d(TAG, "Creating expanded menu. Selected section: " + mSelectedSectionId);
        mExpandedMenu = new HoverMenuViewStateExpanded(mSelectedSectionId);
        mExpandedMenu.setMenu(mMenu);
        mExpandedMenu.setListener(new HoverMenuViewStateExpanded.Listener() {
            @Override
            public void onExpanding() {
                notifyListenersExpanding();
            }

            @Override
            public void onExpanded() {
                mScreen.getContentDisplay().setVisibility(VISIBLE);
                notifyListenersExpanded();
            }

            @Override
            public void onCollapseRequested() {
                collapse();
            }
        });
    }

    private void collapse() {
        if (mIsExpanded) {
            mIsExpanded = false;
            if (null != mWindowViewController) {
                mWindowViewController.makeUntouchable(this);
            }
            clearFocus(); // For handling hardware back button presses.

            createCollapsedMenu();

            mSelectedSectionId = mExpandedMenu.getActiveSectionId();
            mExpandedMenu.giveControlTo(mCollapsedMenu);
            mExpandedMenu = null;

            mScreen.getContentDisplay().setVisibility(GONE);
        }
    }

    private void createCollapsedMenu() {
        Log.d(TAG, "Creating collapsed menu. Dock: " + mCollapsedDock);
        mCollapsedMenu = new HoverMenuViewStateCollapsed(mDragger, mCollapsedDock);
        mCollapsedMenu.setMenu(mMenu);
        mCollapsedMenu.setListener(new HoverMenuViewStateCollapsed.Listener() {
            @Override
            public void onCollapsing() {
                notifyListenersCollapsing();
            }

            @Override
            public void onCollapsed() {
                notifyListenersCollapsed();
            }

            @Override
            public void onDragStart() {
                mScreen.getExitView().setVisibility(VISIBLE);
            }

            @Override
            public void onDragEnd() {
                mScreen.getExitView().setVisibility(GONE);
            }

            @Override
            public void onDocked() {
                Log.d(TAG, "Floating tab has docked.");
                mCollapsedDock = mCollapsedMenu.getDock();
                saveVisualState();
            }

            @Override
            public void onTap() {
                Log.d(TAG, "Expanding the hover menu.");
                expand();
            }

            @Override
            public void onDroppedOnExit() {
                Log.d(TAG, "Floating tab dropped on exit.");
                if (null != mOnExitListener) {
                    mOnExitListener.onExit();
                }
            }
        });
    }

    public void setOnExitListener(@Nullable OnExitListener listener) {
        mOnExitListener = listener;
    }

    public void addOnExpandAndCollapseListener(@NonNull OnExpandAndCollapseListener listener) {
        mOnExpandAndCollapseListeners.add(listener);
    }

    public void removeOnExpandAndCollapseListener(@NonNull OnExpandAndCollapseListener listener) {
        mOnExpandAndCollapseListeners.remove(listener);
    }

    private void notifyListenersExpanding() {
        Log.i(TAG, "Notifying listeners that Hover is expanding.");
        for (OnExpandAndCollapseListener listener : mOnExpandAndCollapseListeners) {
            listener.onExpanding();
        }
    }

    private void notifyListenersExpanded() {
        Log.i(TAG, "Notifying listeners that Hover is now expanded.");
        for (OnExpandAndCollapseListener listener : mOnExpandAndCollapseListeners) {
            listener.onExpanded();
        }
    }

    private void notifyListenersCollapsing() {
        Log.i(TAG, "Notifying listeners that Hover is collapsing.");
        for (OnExpandAndCollapseListener listener : mOnExpandAndCollapseListeners) {
            listener.onCollapsing();
        }
    }

    private void notifyListenersCollapsed() {
        Log.i(TAG, "Notifying listeners that Hover is now collapsed.");
        for (OnExpandAndCollapseListener listener : mOnExpandAndCollapseListeners) {
            listener.onCollapsed();
        }
    }

    // Only call this if using HoverMenuView directly in a window.
    public void addToWindow() {
        if (!mIsAddedToWindow) {
            mWindowViewController.addView(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    false,
                    this
            );

            mIsAddedToWindow = true;
        }
    }

    // Only call this if using HoverMenuView directly in a window.
    public void removeFromWindow() {
        if (null != mWindowViewController) {
            mWindowViewController.removeView(this);
        }
    }

    private static class SavedState extends BaseSavedState {

        private static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SideDock mCollapsedDock;
        private HoverMenu.SectionId mSelectedSectionId;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel in) {
            super(in);

            if (in.dataAvail() > 0) {
                float dockVerticalPositionPercent = in.readFloat();
                int side = in.readInt();
                mCollapsedDock = new SideDock(dockVerticalPositionPercent, side);
            }
            if (in.dataAvail() > 0) {
                mSelectedSectionId = new HoverMenu.SectionId(in.readString());
            }
        }

        @Nullable
        public SideDock getCollapsedDock() {
            return mCollapsedDock;
        }

        public void setCollapsedDock(@Nullable SideDock collapsedDock) {
            mCollapsedDock = collapsedDock;
        }

        @Nullable
        public HoverMenu.SectionId getSelectedSectionId() {
            return mSelectedSectionId;
        }

        public void setSelectedSectionId(@Nullable HoverMenu.SectionId selectedSectionId) {
            mSelectedSectionId = selectedSectionId;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            if (null != mCollapsedDock) {
                out.writeFloat(mCollapsedDock.getVerticalDockPositionPercentage());
                out.writeInt(mCollapsedDock.getSide());
            }
        }
    }
}
