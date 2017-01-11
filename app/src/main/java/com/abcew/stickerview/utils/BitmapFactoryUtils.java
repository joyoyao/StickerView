package com.abcew.stickerview.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.util.TypedValue;

/**
 * Created by laputan on 2017/1/11.
 */

public class BitmapFactoryUtils {

    private BitmapFactoryUtils() {}

    /**
     * Decode a image file into a Bitmap.
     * @param filename  image file path.
     * @param minSize minWidth the image must have.
     * @param square width and height of the image should be same.
     * @return the source file decoded with a minimum sample size.
     */
    public static Bitmap decodeFile(final String filename, final int minSize, final boolean square) {
        return decodeFile(filename, minSize, square, true);
    }

    /**
     * Decode a image file into a Bitmap.
     * @param filename  image file path.
     * @param minSize minWidth the image must have.
     * @param square width and height of the image should be same.
     * @param fixRotation fix image rotation based on the exif orientation information.
     * @return the source file decoded with a minimum sample size.
     */
    public static Bitmap decodeFile(final String filename, final int minSize, final boolean square, boolean fixRotation) {
        final int angle = getImageRotation(filename);

        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, opts);

        final int size = Math.max(opts.outWidth, opts.outHeight);
        if (size > minSize && minSize > 0) {
            opts.inSampleSize = size / minSize;
        } else {
            opts.inSampleSize = 1;
        }

        Bitmap bitmap = decodeFile(filename, opts.inSampleSize);

        if (bitmap == null) return null;

        if (angle != 0 && fixRotation) {
            final Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        }

        if (square && bitmap.getWidth() != bitmap.getHeight()) {
            if (bitmap.getWidth() > bitmap.getHeight()) {
                bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - bitmap.getHeight()) / 2, 0, bitmap.getHeight(), bitmap.getHeight());
            } else if (bitmap.getWidth() < bitmap.getHeight()) {
                bitmap = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() - bitmap.getWidth()) / 2, bitmap.getWidth(), bitmap.getWidth());
            }
        }
        return bitmap;
    }

    /**
     * Get the orientation of the image file base on the exif information.
     * @param filename image file path.
     * @return angle in degree.
     */
    public static int getImageRotation(final String filename) {
        return ExifUtils.getAngle(filename);
    }

    /**
     * Decode a drawable or a raw image resource.
     * @param res resources @see #ImgLySdk.getAppResource().
     * @param resId resource id.
     * @param minSize minWidth the image must have.
     * @return the source resource image decoded with a minimum sample size.
     */
    public static Bitmap decodeResource(final Resources res, @DrawableRes @RawRes final int resId, final int minSize) {

        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, opts);

        final int size = Math.max(opts.outWidth, opts.outHeight);

        if (size > minSize && minSize > 0) {
            opts.inSampleSize = size / minSize;
        } else {
            opts.inSampleSize = 1;
        }

        limitMemoryUsage(opts);

        opts.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res, resId, opts);
    }

    @NonNull
    public static float[] decodeSize(final Resources res, @DrawableRes @RawRes final int resId) {
        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, opts);

        return new float[]{opts.outWidth, opts.outHeight};
    }

    @NonNull
    public static float[] decodeSize(final String filename) {
        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, opts);

        return new float[]{opts.outWidth, opts.outHeight};
    }

    private static void limitMemoryUsage(@NonNull BitmapFactory.Options options) {

        float bufferScale = 2f;

        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }

        if (freeMemory() < ((options.outWidth * options.outHeight * 4) / (options.inSampleSize * options.inSampleSize)) * 1.5f) {
            System.gc();
            System.gc();
        }

        while (freeMemory() < ((options.outWidth * options.outHeight * 4) / (options.inSampleSize * options.inSampleSize)) * bufferScale) {
            options.inSampleSize += 1;
        }
    }

    private static long freeMemory(){
        return  Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    /**
     * Decode a drawable or a raw image resource.
     * @param res resources @see #ImgLySdk.getAppResource().
     * @param resId resource id.
     * @return the source resource image in full size
     */
    public static Bitmap decodeResource(final Resources res, @DrawableRes @RawRes final int resId) {
        final BitmapFactory.Options opts = new BitmapFactory.Options();

        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, opts);
        limitMemoryUsage(opts);
        opts.inJustDecodeBounds = false;

        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(res, resId, opts);
    }

    /**
     * Draw a XML Drawable resource to a Bitmap because a XML Drawable not can be decoded by #decodeResource
     * @param res Application Resource
     * @param resId Drawable Resource id.
     * @param width destination width
     * @param height destination height
     * @return A transparent bitmap with the drawable painted to it.
     */
    public static Bitmap drawResource(@NonNull final Resources res, @DrawableRes final int resId, int width, int height){
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable d = res.getDrawable(resId);
        if (d != null) {
            d.setBounds(0, 0, width, height);
            d.draw(canvas);
        }
        return bitmap;
    }

    private static Bitmap decodeFile(final String pathName, final int startInSampleSize) {
        final BitmapFactory.Options opts = new BitmapFactory.Options();

        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opts);
        limitMemoryUsage(opts);
        opts.inJustDecodeBounds = false;

        int inSampleSize = startInSampleSize;
        opts.inSampleSize = inSampleSize;
        opts.inDither  = false;
        opts.inMutable = true;

        return BitmapFactory.decodeFile(pathName, opts);
    }

    /*private static Bitmap setReplaceColor(Bitmap bitmap, final int startInSampleSize, final int add, final int multi) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(ImgLySdk.getAppContext());
        Allocation in = Allocation.createFromBitmap(rs, bitmap);
        Allocation out = Allocation.createFromBitmap(rs, output);

        ScriptC_replace_color replace_color = new ScriptC_replace_color(rs);
        replace_color.forEach_root();

        return output;
    }*/

    /**
     * Check if the raw resource is a svg file, by looking at the file_extension
     * @param resourceID resource id.
     * @return true if teh resource is an svg.
     */
    public static boolean checkIsSvgResource(@DrawableRes @RawRes int resourceID) {
        boolean isSvg=false;
        try {
//            final Resources resources = ImgLySdk.getAppResource();
//            final String resourceTypeName = resources.getResourceTypeName(resourceID);
//
//            if (resourceTypeName.contains("raw")) {
//                final TypedValue value = new TypedValue();
//                resources.getValue(resourceID, value, true); //Get file Name
//                isSvg = value.string.toString().toLowerCase().endsWith(".svg"); //Is file suffix .SVG
//            } else {
//                isSvg = false; //SVG must be RAW
//            }

        } catch (Resources.NotFoundException notFound){
            isSvg = false;
        }

        return isSvg;
    }
}
