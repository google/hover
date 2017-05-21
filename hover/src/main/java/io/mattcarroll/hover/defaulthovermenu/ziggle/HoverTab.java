package io.mattcarroll.hover.defaulthovermenu.ziggle;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * TODO
 */
public interface HoverTab {

    int getTabWidth();

    int getTabHeight();

    Point getPosition();

    void moveTo(@NonNull Point position);

    void slideTo(@NonNull Point position);

    void slideTo(@NonNull Point position, @Nullable Runnable callback);

    void setTabView(@NonNull View view);

    void setOnClickListener(@Nullable View.OnClickListener onClickListener);
}
