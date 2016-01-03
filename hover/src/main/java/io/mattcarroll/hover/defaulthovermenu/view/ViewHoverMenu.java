package io.mattcarroll.hover.defaulthovermenu.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import io.mattcarroll.hover.BuildConfig;
import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverMenuAdapter;
import io.mattcarroll.hover.defaulthovermenu.HoverMenuView;
import io.mattcarroll.hover.defaulthovermenu.utils.view.InViewGroupDragger;

/**
 * {@link HoverMenu} implementation that can be embedded in traditional view hierarchies.
 */
public class ViewHoverMenu extends FrameLayout implements HoverMenu {

    private static final String PREFS_FILE = "viewhovermenu";
    private static final String PREFS_KEY_ANCHOR_SIDE = "anchor_side";
    private static final String PREFS_KEY_ANCHOR_Y = "anchor_y";

    private HoverMenuView mHoverMenuView;
    private InViewGroupDragger mDragger;
    private HoverMenuAdapter mAdapter;
    private SharedPreferences mPrefs;

    public ViewHoverMenu(Context context) {
        super(context);
        init();
    }

    public ViewHoverMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewHoverMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPrefs = getContext().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mDragger = new InViewGroupDragger(this, ViewConfiguration.get(getContext()).getScaledTouchSlop());
        mDragger.setDebugMode(BuildConfig.DEBUG);
        mHoverMenuView = new HoverMenuView(getContext(), mDragger, loadSavedAnchorState());
        addView(mHoverMenuView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (null != mAdapter) {
            mHoverMenuView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        saveAnchorState();
        removeView(mHoverMenuView);
        mHoverMenuView = null;
        mDragger.deactivate(); // TODO: should be called by HoverMenuView in some kind of release() method.
        super.onDetachedFromWindow();
    }

    @Override
    public PointF getAnchorState() {
        return mHoverMenuView.getAnchorState();
    }

    @Override
    public void setAdapter(@Nullable HoverMenuAdapter adapter) {
        mAdapter = adapter;
        if (null != mAdapter && null != mHoverMenuView) {
            mHoverMenuView.setAdapter(adapter);
        }
    }

    @Override
    public void expandMenu() {
        mHoverMenuView.expand();
    }

    @Override
    public void collapseMenu() {
        mHoverMenuView.collapse();
    }

    private void saveAnchorState() {
        PointF anchorState = mHoverMenuView.getAnchorState();
        mPrefs.edit()
            .putFloat(PREFS_KEY_ANCHOR_SIDE, anchorState.x)
            .putFloat(PREFS_KEY_ANCHOR_Y, anchorState.y)
            .apply();
    }

    private PointF loadSavedAnchorState() {
        return new PointF(
                mPrefs.getFloat(PREFS_KEY_ANCHOR_SIDE, 2),
                mPrefs.getFloat(PREFS_KEY_ANCHOR_Y, 0.5f)
        );
    }
}
