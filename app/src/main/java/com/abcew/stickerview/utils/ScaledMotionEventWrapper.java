package com.abcew.stickerview.utils;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laputan on 2017/1/11.
 */

public class ScaledMotionEventWrapper {

    private static final int MAX_CLICK_DURATION = 200;
    private static final int MAX_CLICK_DISTANCE = 15;
    private final float scale;
    private final float offsetX;
    private final float offsetY;
    private final MotionEvent event;

    @Nullable
    private static TransformState startTransformState = null;

    private boolean isCheckpoint = false;

    private static long pressStartTime;

    private static boolean isClicked = false;
    private static float pressedX;
    private static float pressedY;

    @Nullable
    private Point fixedCenterPoint = null;

    MotionEvent lastEvent;

    public ScaledMotionEventWrapper(MotionEvent event, float scale, float offsetX, float offsetY) {
        this.event = event;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

        switch (getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                saveTransformState();
                if (lastEvent == null || !lastEvent.equals(event)) {
                    lastEvent = event;
                    pressStartTime = System.currentTimeMillis();
                    pressedX = getX(0);
                    pressedY = getY(0);

                    isClicked = false;
                }
                break;

            case MotionEvent.ACTION_UP:
                long pressDuration = System.currentTimeMillis() - pressStartTime;

                if (pressDuration < MAX_CLICK_DURATION && distance(pressedX, pressedY, getX(0), getY(0)) < MAX_CLICK_DISTANCE) {
                    isClicked = true;
                }
                break;
        }

        if (getPointerCount() != 1) {
            pressStartTime = 0;
        }

        if (startTransformState != null && startTransformState.getPointCount() != getPointerCount()) {
            saveTransformState();
        }
    }

    /**
     * Set a center point to emulate a multitouch rotating and scaling Event
     * @param x center point x.
     * @param y center point y.
     */
    public void setFixedCenterPoint(float x, float y) {
        fixedCenterPoint = new Point(x, y);
        if (isCheckpoint()) {
            saveTransformState();
        }
    }

    /**
     * Return if the event has a fixed center point to emulate a multitouch rotating and scaling Event.
     * @return true if teh event has a fixed center point.
     */
    public boolean hasFixedCenterPoint() {
        return fixedCenterPoint != null;
    }

    /**
     * Check if the event is a click.
     * @return true it the object was clicked.
     */
    public boolean hasClicked() {
        return isClicked;
    }

    private static float distance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float distanceInPx = (float) Math.sqrt(dx * dx + dy * dy);
        return pxToDp(distanceInPx);
    }

    private static float pxToDp(float px) {
        return px / LocalDisplay.SCREEN_DENSITY;
    }

    private void saveTransformState() {
        startTransformState = new TransformState(this);
        isCheckpoint = true;
    }

    /**
     * If this return true you must save your current state. The TransformDifference values will be at the start values. (xDiff = 0, yDiff = 0; angleDiff = 0; distanceDiff, scale = 1;)
     * @see #getTransformDifference()
     * @return true if this is a checkpoint event.
     */
    public boolean isCheckpoint(){
        return isCheckpoint;
    }

    /**
     * Get the scaled x pos of the point.
     * @param index point index @see #getPointerCount
     * @return scaled x pos of the point.
     */
    public float getX(int index) {
        return event.getX(index) / scale - offsetX;// * scale;
    }

    /**
     * Get the scaled y pos of the point.
     * @param index point index @see #getPointerCount
     * @return scaled y pos of the point.
     */
    public float getY(int index) {
        return event.getY(index) / scale - offsetY;// * scale;
    }

    /**
     * The number of pointers of data contained in this event. Always
     * &gt;= 1.
     * @return number of pointers of data contained in this event.
     */
    public int getPointerCount() {
        return event.getPointerCount();
    }

    /**
     * Return the kind of action being performed.
     * Consider using {@link #getActionMasked} and {@link MotionEvent#getActionIndex} to retrieve
     * the separate masked action and pointer index.
     * @return The action, such as {@link MotionEvent#ACTION_DOWN} or
     * the combination of {@link MotionEvent#ACTION_POINTER_DOWN} with a shifted pointer index.
     */
    public int getActionMasked() {
        return event.getAction() & 0xff; // replace for MotionEventCompat.getActionMasked(event);
    }

    /**
     * Return the differences to the checkpoint. The initial values are (xDiff = 0, yDiff = 0; angleDiff = 0; distanceDiff, scale = 1)
     * @return a TransformDiff object.
     */
    @NonNull
    public TransformDiff getTransformDifference() {
        if (startTransformState == null) {
            startTransformState = new TransformState(this);
        }
        return startTransformState.calculateDiff(ScaledMotionEventWrapper.this);
    }


    private static class TransformState {
        private final List<Point> points = new ArrayList<>();

        private final boolean hasFixedCenterPoint;

        public TransformState(@NonNull ScaledMotionEventWrapper wrapper) {

            for (int i = 0, l = wrapper.getPointerCount(); i < l; i++) {
                Point point = (points.size() > i) ? points.get(i) : null;
                if (point == null) {
                    point = new Point();
                    points.add(point);
                }
                point.x = wrapper.getX(i);
                point.y = wrapper.getY(i);
            }

            hasFixedCenterPoint = wrapper.hasFixedCenterPoint();
            if (hasFixedCenterPoint) {
                points.add(1, wrapper.fixedCenterPoint);
            }
        }

        public int getPointCount() {
            return hasFixedCenterPoint ? 1 : points.size();
        }

        public float getDistance() {
            if (points.size() == 2) {
                Point p1 = points.get(0);
                Point p2 = points.get(1);
                return Math.max((float) Math.sqrt((p1.x - p2.x) *  (p1.x - p2.x) + (p1.y - p2.y) *  (p1.y - p2.y)), 1);
            } else {
                return 1;
            }
        }

        public float getAngle() {
            float angle = 0;
            if (points.size() == 2) {
                Point p1 = points.get(0);
                Point p2 = points.get(1);
                angle = (float) Math.toDegrees(Math.atan2(p1.y - p2.y, p1.x - p2.x));

                if (angle < 0) {
                    angle += 360;
                }
            }
            return angle;
        }

        public Point getCenterPoint() {
            if (hasFixedCenterPoint) {
                return points.get(1);
            } else if (points.size() == 2) {
                Point p1 = points.get(0);
                Point p2 = points.get(1);
                RectF rectF = new RectF(p1.x, p1.y, p2.x, p2.y);
                return new Point(rectF.centerX(), rectF.centerY());
            } else {
                Point p1 = points.get(0);
                return new Point(p1.x, p1.y);
            }
        }

        @NonNull
        public TransformDiff calculateDiff(@NonNull ScaledMotionEventWrapper wrapper) {
            TransformState latestState = new TransformState(wrapper);

            Point centerPoint = getCenterPoint();
            Point latestCenterPoint = latestState.getCenterPoint();

            return new TransformDiff(
                    (latestState.getDistance() - getDistance()),
                    (latestState.getAngle()    - getAngle()),
                    (latestCenterPoint.x       - centerPoint.x),
                    (latestCenterPoint.y       - centerPoint.y),
                    (latestState.getDistance() / getDistance())
            );
        }
    }

    public static class TransformDiff {
        public final float distanceDiff;
        public final float angleDiff;
        public final float xDiff;
        public final float yDiff;
        public final float scale;

        public TransformDiff(float distanceDiff, float angleDiff, float xDiff, float yDiff, float scale) {
            this.distanceDiff = distanceDiff;
            this.angleDiff = angleDiff;
            this.xDiff = xDiff;
            this.yDiff = yDiff;
            this.scale = scale;
        }
    }

    private static class Point {
        public float x = 0;
        public float y = 0;

        public Point() {}

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
