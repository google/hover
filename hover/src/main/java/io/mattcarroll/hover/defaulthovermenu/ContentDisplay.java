package io.mattcarroll.hover.defaulthovermenu;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import io.mattcarroll.hover.Navigator;
import io.mattcarroll.hover.NavigatorContent;
import io.mattcarroll.hover.R;

/**
 * TODO
 */
class ContentDisplay extends RelativeLayout {

    private static final String TAG = "ContentDisplay";

    private View mContainer;
    private FrameLayout mContentView;
    private Drawable mContentBackground;
    private HoverMenuTabSelectorView mTabSelectorView;
    private Tab mSelectedTab;
    private Navigator mNavigator;

    public ContentDisplay(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_content, this, true);

        mContainer = findViewById(R.id.container);
        expandToScreenBounds();

        int backgroundCornerRadiusPx = (int) getResources().getDimension(R.dimen.popup_corner_radius);
        mTabSelectorView = (HoverMenuTabSelectorView) findViewById(R.id.tabselector);
        mTabSelectorView.setPadding(backgroundCornerRadiusPx, 0, backgroundCornerRadiusPx, 0);

        mContentView = (FrameLayout) findViewById(R.id.view_content_container);
        mContentBackground = ContextCompat.getDrawable(getContext(), R.drawable.round_rect_white);
        mContentView.setBackgroundDrawable(mContentBackground);

        mNavigator = new DefaultNavigator(getContext(), true);
        mContentView.addView(mNavigator.getView());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        setLayoutParams(layoutParams);
    }

    public void enableDebugMode(boolean debugMode) {
        if (debugMode) {
            setBackgroundColor(0x88FFFF00);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void activeTabIs(@Nullable Tab tab) {
        // TODO: handle nullable
        mSelectedTab = tab;

        updateTabSelectorPosition();
        tab.addOnPositionChangeListener(new Tab.OnPositionChangeListener() {
            @Override
            public void onPositionChange(@NonNull Point position) {
                updateTabSelectorPosition();
            }
        });
    }

    private void updateTabSelectorPosition() {
        Point tabPosition = mSelectedTab.getPosition();
        mTabSelectorView.setSelectorPosition(tabPosition.x);
    }

    // TODO: change to Content after Navigator is working
//    void displayContent(@NonNull Content content) {
//        // TODO:
//    }

    public void displayContent(@Nullable NavigatorContent content) {
        mNavigator.clearContent();
        mNavigator.pushContent(content);

        if (content.isFullscreen()) {
            expandToScreenBounds();
        } else {
            wrapContent();
        }
    }

    public void anchorTo(@NonNull final View anchor) {
        setPadding(0, anchor.getBottom(), 0, 0);

        anchor.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.d(TAG, "Updating anchor position to: " + (anchor.getY() + anchor.getHeight()));
                setPadding(0, anchor.getBottom(), 0, 0);
            }
        });
    }

    public void expandToScreenBounds() {
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mContainer.getLayoutParams();
        layoutParams.height = 0;
        layoutParams.addRule(ALIGN_PARENT_TOP);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        mContainer.setLayoutParams(layoutParams);
    }

    public void wrapContent() {
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mContainer.getLayoutParams();
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.addRule(ALIGN_PARENT_TOP);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM, 0); // This means "remove rule". Can't use removeRule() until API 17.
        mContainer.setLayoutParams(layoutParams);
    }
}
