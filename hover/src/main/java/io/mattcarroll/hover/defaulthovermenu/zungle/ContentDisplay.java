package io.mattcarroll.hover.defaulthovermenu.zungle;

import android.content.Context;
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
import io.mattcarroll.hover.defaulthovermenu.DefaultNavigator;
import io.mattcarroll.hover.defaulthovermenu.HoverMenuTabSelectorView;

/**
 * TODO
 */
public class ContentDisplay extends RelativeLayout {

    private static final String TAG = "ContentDisplay";

    private View mContainer;
    private FrameLayout mContentView;
    private Drawable mContentBackground;
    private HoverMenuTabSelectorView mTabSelectorView;
    private Tab mSelectedTab;
    private Navigator mNavigator;

    public ContentDisplay(@NonNull Context context) {
        super(context);
        setBackgroundColor(0x88FFFF00);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_hover_menu_content, this, true);

        mContainer = findViewById(R.id.container);
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) mContainer.getLayoutParams();
        layoutParams.height = 0;
        layoutParams.addRule(ALIGN_PARENT_TOP);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        mContainer.setLayoutParams(layoutParams);

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

    void activeTabIs(@Nullable Tab tab) {
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
    }

    void anchorTo(@NonNull final View anchor) {
//        setY(500);
//        setY(anchor.getBottom());
        setPadding(0, anchor.getBottom(), 0, 0);

        anchor.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.d(TAG, "Updating anchor position to: " + (anchor.getY() + anchor.getHeight()));
//                setY((anchor.getY() + anchor.getHeight()));
                setPadding(0, anchor.getBottom(), 0, 0);
            }
        });
    }

    void expandToScreenBounds() {
        // TODO: full width, height to bottom of screen
    }

    void wrapContent() {
        // TODO: full width, height based on content
    }
}
