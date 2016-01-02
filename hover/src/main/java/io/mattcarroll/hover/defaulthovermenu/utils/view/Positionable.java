package io.mattcarroll.hover.defaulthovermenu.utils.view;

import android.graphics.Point;
import android.support.annotation.NonNull;

/**
 * Represents a visual object that can be positioned at (X,Y) coordinates.
 */
public interface Positionable {

    void setPosition(@NonNull Point position);

}
