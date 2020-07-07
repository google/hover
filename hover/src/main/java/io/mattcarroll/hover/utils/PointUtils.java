package io.mattcarroll.hover.utils;

import android.graphics.Point;
import android.graphics.PointF;

public class PointUtils {

    private PointUtils() {

    }

    public static float calculateDistance(Point pointA, Point pointB) {
        return (float) Math.sqrt((pointA.x - pointB.x) * (pointA.x - pointB.x) + (pointA.y - pointB.y) * (pointA.y - pointB.y));
    }

    public static Point parse(PointF pointf) {
        return new Point((int) pointf.x, (int) pointf.y);
    }
}
