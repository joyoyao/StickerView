package com.abcew.stickerview.utils;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Created by laputan on 2017/1/11.
 */

public class ImageViewUtil {

    /**
     * Gets the rectangular position of a Bitmap if it were placed inside a View
     * with scale type set to {@link ImageView.ScaleType #CENTER_INSIDE}.
     *
     * @param imageWidth the Bitmap's width
     * @param imageHeight the Bitmap's height
     * @param viewWidth the parent View's width
     * @param viewHeight the parent View's height
     * @return the rectangular position of the Bitmap
     */
    @NonNull
    public static Rect getBitmapRectCenterInside(int imageWidth, int imageHeight, int viewWidth, int viewHeight) {

        return getBitmapRectCenterInsideHelper(imageWidth, imageHeight, viewWidth, viewHeight);
    }

    /**
     * Helper that does the work of the above functions. Gets the rectangular
     * position of a Bitmap if it were placed inside a View with scale type set
     * to {@link ImageView.ScaleType #CENTER_INSIDE}.
     *
     * @param imageWidth the Bitmap's width
     * @param imageHeight the Bitmap's height
     * @param viewWidth the parent View's width
     * @param viewHeight the parent View's height
     * @return the rectangular position of the Bitmap
     */
    @NonNull
    private static Rect getBitmapRectCenterInsideHelper(int imageWidth, int imageHeight, int viewWidth, int viewHeight) {
        double resultWidth;
        double resultHeight;
        int resultX;
        int resultY;

        double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
        double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;

        // Checks if either width or height needs to be fixed
        //if (viewWidth < imageWidth) {
        viewToBitmapWidthRatio = (double) viewWidth / (double) imageWidth;
        //}
        //if (viewHeight < imageHeight) {
        viewToBitmapHeightRatio = (double) viewHeight / (double) imageHeight;
        //}

        // If either needs to be fixed, choose smallest ratio and calculate from
        // there
        if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY)
        {
            if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                resultWidth = viewWidth;
                resultHeight = (imageHeight * resultWidth / imageWidth);
            }
            else {
                resultHeight = viewHeight;
                resultWidth = (imageWidth * resultHeight / imageHeight);
            }
        }
        // Otherwise, the picture is within frame layout bounds. Desired width
        // is simply picture size
        else {
            resultHeight = imageHeight;
            resultWidth = imageWidth;
        }

        // Calculate the position of the bitmap inside the ImageView.
        if (resultWidth == viewWidth) {
            resultX = 0;
            resultY = (int) Math.round((viewHeight - resultHeight) / 2);
        } else if (resultHeight == viewHeight) {
            resultX = (int) Math.round((viewWidth - resultWidth) / 2);
            resultY = 0;
        }
        else {
            resultX = (int) Math.round((viewWidth - resultWidth) / 2);
            resultY = (int) Math.round((viewHeight - resultHeight) / 2);
        }

        return new Rect(
                resultX,
                resultY,
                resultX + (int) Math.ceil(resultWidth),
                resultY + (int) Math.ceil(resultHeight)
        );
    }
}
