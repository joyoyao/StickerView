package com.abcew.stickerview.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.abcew.stickerview.R;
import com.abcew.stickerview.utils.BitmapFactoryUtils;
import com.abcew.stickerview.utils.ImageViewUtil;
import com.abcew.stickerview.utils.ScaledMotionEventWrapper;

import java.util.concurrent.ExecutionException;

/**
 * Created by laputan on 2017/1/11.
 */

public class StickerView extends ImageView {

    private static final String TAG = "StickerView";

    private static final int CACHE_THRESHOLD = 256 * 256;

    private Paint paint;
    private Paint uiPaint;

    private float imageScale = 1;
    private float translationX = 0;
    private float translationY = 0;

    private boolean reinitializedAspect = true;

    private float memoryScaleDown = 1f; //Down scale the cache to avoid OutOfMemory exception

    private Bitmap resizeBitmap;

    private Bitmap delBitmap;

    @Nullable
    private Bitmap stickerPictureCache;

    private RectF dst_resize;

    private RectF del_resize;

    private int resizeBitmapWidth;
    private int resizeBitmapHeight;
    private int delBitmapWidth;
    private int delBitmapHeight;

    private Paint localPaint;
    private int screenWidth, screenHeight;

    private final Matrix stickerMatrix = new Matrix();
    private final Matrix drawStickerMatrix = new Matrix();

    private boolean isInEdit = true;

    private float minScale = 0.5f;
    private float maxScale = 1.2f;

    private DisplayMetrics dm;

    private float currentX = 0f;
    private float currentY = 0f;
    private float currentScale = 1f;
    private float currentRotation = 0f;
    private boolean isHorizonMirrored = false;

    private final StickerConfigInterface config;

    private int stickerCacheWidth = -1;
    private int stickerCacheHeight = -1;

    private boolean cacheIsLoading = false;

    private boolean isStickerImageInitialized = false;

    private int maxTextWidth = 1;

    @Nullable
    private View holderView;

    private long cacheNewPixelSize = -1;
    private long cachePixelSize = -1;

    private final StickerHolderView holder;


    private Path boxPath = new Path();

    public StickerView(Context context, StickerConfigInterface config, StickerHolderView holder) {
        super(context);
        this.config = config;
        this.holder = holder;
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        setWillNotDraw(false);

        init();
    }

    protected void refresh() {
        cachePixelSize = -1;
        cacheIsLoading = false;
        reinitializedAspect = true;
        loadBitmapCache();
    }

    private void init() {
        dm = getResources().getDisplayMetrics();

        paint = new Paint();

        uiPaint = new Paint();
        uiPaint.setAlpha(255);

        dst_resize = new RectF();
        del_resize = new RectF();

        localPaint = new Paint();
//        localPaint.setColor(0x66FFFFFF);
//        localPaint.setAntiAlias(true);
//        localPaint.setDither(true);
//        PathEffect effects = new DashPathEffect(new float[]{12, 12,}, 1);
//        localPaint.setPathEffect(effects);
//        localPaint.setStyle(Paint.Style.STROKE);
//        localPaint.setStrokeWidth(4);
//
//
        localPaint.setColor(Color.WHITE);
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setAntiAlias(true);
        localPaint.setStrokeWidth(4);
        PathEffect effects = new DashPathEffect(new float[]{12, 12,}, 1);
        localPaint.setPathEffect(effects);

//        localPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
//        localPaint.setStyle(Paint.Style.STROKE);
//        localPaint.setColor(Color.WHITE);
//        localPaint.setStrokeWidth(4);
//        PathEffect effects = new DashPathEffect(new float[] { 1, 2, 4, 8}, 1);
//        localPaint.setPathEffect(effects);

//        localPaint = new Paint();
//        localPaint.setStyle(Paint.Style.FILL);
//        localPaint.setColor(Color.BLUE);
//        localPaint.setStrokeWidth(1);
//        PathEffect effects = new DashPathEffect(new float[] { 1, 2, 4, 8}, 1);
//        localPaint.setPathEffect(effects);
//        localPaint.setAntiAlias(true);

        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        maxTextWidth = Math.max(screenWidth, screenHeight) * 2;

        loadBitmapCache();
        initButtonBitmaps();
    }

    protected boolean hasStickerSize() {
        return stickerCacheHeight > 0 && stickerCacheWidth > 0;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (holderView != null) {
            holderView.invalidate(); //Workaround InvalidationBug when clipChildren = false
        }
    }

    @Override
    public void postInvalidate() {
        super.postInvalidate();
        if (holderView != null) {
            holderView.postInvalidate(); //Workaround InvalidationBug when clipChildren = false
        }
    }

    @Override
    protected void onAttachedToWindow() {
        holderView = (View) getParent();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        holderView = null;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (hasStickerSize()) {
            drawToCanvas(canvas, imageScale, translationX, translationY);
        }
    }

    protected void drawStickerToCanvas(@NonNull Canvas canvas, int x, int y) {
        Rect imageRect = ImageViewUtil.getBitmapRectCenterInside(canvas.getWidth(), canvas.getHeight(), getWidth(), getHeight());

        final float scale = Math.min(canvas.getWidth() / (float) imageRect.width(), canvas.getHeight() / (float) imageRect.height());

        x -= imageRect.left;
        y -= imageRect.top;

        maxTextWidth = Math.max(canvas.getWidth(), canvas.getHeight()) * 2;

        this.isInEdit = false;

        //LoadSync

        loadBitmapCache(Math.round(cachePixelSize * (double) scale * scale), true);

        drawToCanvas(canvas, scale, x, y);
    }

    protected StickerConfigInterface getConfig() {
        return config;
    }

    @Nullable
    public StickerConfigInterface.STICKER_TYPE getType() {
        return config == null ? null : getConfig().getType();
    }

    protected void setScale(float scale) {
        this.imageScale = scale;
        postInvalidate();
    }

    @Override
    public void setTranslationX(float translationX) {
        this.translationX = translationX;
        postInvalidate();
    }

    @Override
    public void setTranslationY(float translationY) {
        this.translationY = translationY;
        postInvalidate();
    }


    private boolean requestRedraw = false;

    public void rescaleCache(float scaleDown) {
        boolean instantClear = Math.abs(scaleDown - memoryScaleDown) > 0.2 && memoryScaleDown != 1 || memoryScaleDown == 0f;

        memoryScaleDown = scaleDown;
        if (instantClear) {
            if (stickerPictureCache != null && scaleDown != 1) {
                stickerPictureCache.recycle();
                stickerPictureCache = null;
            }

            System.gc();
            if (!requestRedraw && memoryScaleDown != 0) {
                requestRedraw = true;
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() > (cachePixelSize * memoryScaleDown * 4 * 2)) {
                            requestRedraw = false;
                            loadBitmapCache();
                        } else {
                            post(this);
                        }
                    }
                });
            }
        }
    }

    public boolean isInEdit() {
        return isInEdit;
    }

    @NonNull
    private synchronized Matrix getStickerMatrix() {
        synchronized (stickerMatrix) {

            float stickerWidth = (stickerCacheWidth) * currentScale;
            float stickerHeight = (stickerCacheHeight) * currentScale;

            float translateX = currentX - stickerWidth / 2f;
            float translateY = currentY - stickerHeight / 2f;

            stickerMatrix.reset();
            stickerMatrix.postTranslate(translateX, translateY);

            if (isHorizonMirrored) {
                stickerMatrix.postScale(-1.0F, 1.0F, currentX, currentY);
            }

            stickerMatrix.postRotate(currentRotation, currentX, currentY);

            stickerMatrix.preScale(currentScale, currentScale);
        }
        return stickerMatrix;
    }

    /**
     * Return the current transformation state of the Sticker
     *
     * @return float[] {
     * xPos,
     * yPos,
     * scale,
     * rotation
     * }
     */
    @NonNull
    public float[] getCurrentTransformState() {
        float[] transformState = new float[4];
        transformState[0] = currentX;
        transformState[1] = currentY;
        transformState[2] = currentScale;
        transformState[3] = currentRotation;
        return transformState;
    }

    public void setTransformation(float x, float y, float scale, float rotation) {
        this.currentX = x;
        this.currentY = y;
        this.currentScale = scale;
        this.currentRotation = rotation;
        invalidate();
    }

    protected void drawToCanvas(@NonNull Canvas canvas, float canvasScale, float x, float y) {

        if (!isStickerImageInitialized) {
            return;
        }

        Matrix matrix = getStickerMatrix();

        final float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);

        final float f1 = 0.0F * matrixValues[0] + 0.0F * matrixValues[1] + matrixValues[2];
        final float f2 = 0.0F * matrixValues[3] + 0.0F * matrixValues[4] + matrixValues[5];
        final float f3 = matrixValues[0] * this.stickerCacheWidth + 0.0F * matrixValues[1] + matrixValues[2];
        final float f4 = matrixValues[3] * this.stickerCacheWidth + 0.0F * matrixValues[4] + matrixValues[5];
        final float f5 = 0.0F * matrixValues[0] + matrixValues[1] * this.stickerCacheHeight + matrixValues[2];
        final float f6 = 0.0F * matrixValues[3] + matrixValues[4] * this.stickerCacheHeight + matrixValues[5];
        final float f7 = matrixValues[0] * this.stickerCacheWidth + matrixValues[1] * this.stickerCacheHeight + matrixValues[2];
        final float f8 = matrixValues[3] * this.stickerCacheWidth + matrixValues[4] * this.stickerCacheHeight + matrixValues[5];

        if (stickerPictureCache != null && !stickerPictureCache.isRecycled()) {
            final float scale = stickerCacheWidth / (float) this.stickerPictureCache.getWidth();
            drawStickerMatrix.set(matrix);

            drawStickerMatrix.preScale(scale, scale);
            drawStickerMatrix.postTranslate(x, y);
            drawStickerMatrix.postScale(canvasScale, canvasScale);

            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);

            canvas.save();
            canvas.setMatrix(drawStickerMatrix);

            canvas.drawBitmap(stickerPictureCache, 0, 0, paint);
            canvas.restore();
        } else return;

        if (isInEdit) {

            localPaint.setStrokeWidth(dm.density / canvasScale);

            int scaledResizeBitmapWidth = (int) (resizeBitmapWidth / imageScale);
            int scaledResizeBitmapHeight = (int) (resizeBitmapHeight / imageScale);

            //bottom - right
            dst_resize.left = (int) (f7 - scaledResizeBitmapWidth / 2);
            dst_resize.right = (int) (f7 + scaledResizeBitmapWidth / 2);
            dst_resize.top = (int) (f8 - scaledResizeBitmapHeight / 2);
            dst_resize.bottom = (int) (f8 + scaledResizeBitmapHeight / 2);
            canvas.save();
            canvas.scale(canvasScale, canvasScale);
            canvas.translate(x, y);
//            canvas.drawLine(f1, f2, f3, f4, localPaint);
//            canvas.drawLine(f3, f4, f7, f8, localPaint);
//            canvas.drawLine(f5, f6, f7, f8, localPaint);
//            canvas.drawLine(f5, f6, f1, f2, localPaint);
            boxPath.reset();
            boxPath.moveTo(f1, f2);
            boxPath.lineTo(f3, f4);
            boxPath.lineTo(f7, f8);
            boxPath.lineTo(f5, f6);
            boxPath.lineTo(f1, f2);
            canvas.drawPath(boxPath, localPaint);

            canvas.drawBitmap(resizeBitmap, null, dst_resize, uiPaint);


            int scaledDelBitmapWidth = (int) (delBitmapWidth / imageScale);
            int scaledDelBitmapHeight = (int) (delBitmapHeight / imageScale);

            del_resize.left = (int) (f1 - scaledDelBitmapWidth / 2);
            del_resize.right = (int) (f1 + scaledDelBitmapWidth / 2);
            del_resize.top = (int) (f2 - scaledDelBitmapHeight / 2);
            del_resize.bottom = (int) (f2 + scaledDelBitmapHeight / 2);
            canvas.drawBitmap(delBitmap, null, del_resize, uiPaint);

            canvas.restore();
        }

        final double x1 = f1 - f3;
        final double y1 = f2 - f4;

        final double x2 = f3 - f7;
        final double y2 = f4 - f8;

        float lengthX = (int) Math.sqrt(x1 * x1 + y1 * y1);
        float lengthY = (int) Math.sqrt(x2 * x2 + y2 * y2);

        cacheNewPixelSize = Math.round((lengthX * (double) canvasScale) * (lengthY * (double) canvasScale));

        loadBitmapCache();
    }

    protected boolean calculateOnScreenFlip() {
        float rotationX = getRotationX();
        float rotationY = getRotationY();

        final View rootView = getRootView();
        View lastParentView = null;
        View parentView = (View) getParent();
        int level = 0;

        while (parentView != null && !parentView.equals(rootView) && !parentView.equals(lastParentView)) {
            rotationX += parentView.getRotationX();
            rotationY += parentView.getRotationY();

            lastParentView = parentView;
            parentView = (View) parentView.getParent();
        }

        boolean flipX = Math.round(rotationX / 180f) == 1;
        boolean flipY = Math.round(rotationY / 180f) == 1;

        boolean isFlipped = (flipX && !flipY) || (flipY && !flipX);

        return isFlipped;
    }

    protected float calculateOnScreenRotation() {
        float rotation = getRotation();
        final View rootView = getRootView();
        View lastParentView = null;
        View parentView = (View) getParent();

        while (parentView != null && !parentView.equals(rootView) && !parentView.equals(lastParentView)) {
            rotation += parentView.getRotation();
            lastParentView = parentView;
            parentView = (View) parentView.getParent();
        }

        return rotation % 360;
    }

    protected synchronized void loadBitmapCache() {
        if (!cacheIsLoading) {
            int preCacheWidth = getWidth() / 10;
            int preCacheHeight = getHeight() / 10;

            if (cacheNewPixelSize <= 0) {
                cacheNewPixelSize = Math.max(preCacheWidth * preCacheHeight, CACHE_THRESHOLD);
            }
            loadBitmapCache(cacheNewPixelSize, false);
        }
    }

    @NonNull
    protected static Picture drawTextToPicture(@NonNull TextStickerConfig config) {

        final String text = config.getText();

        final Rect textBounds = new Rect();
        final Paint bgPaint = new Paint();
        final Picture picture = new Picture();
        final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        final float height = 1000;
        final float padding = height / 20f;
        final float shadowX = 0;//maxHeight / 40f;
        final float shadowY = 0;//maxHeight / 40f;
        final float shadowRadius = 0;//maxHeight / 20f;

        paint.setColor(config.getColor());
        paint.setTextSize(height / 1.4f);
        paint.setTypeface(config.getTypeface());
        paint.setAntiAlias(true);
        paint.setTextAlign(config.getAlign());
        paint.getTextBounds(text, 0, text.length(), textBounds);
        paint.setSubpixelText(true);
        paint.setHinting(Paint.HINTING_ON);

        bgPaint.setColor(config.getBackgroundColor());

        final int textWidth = (int) (textBounds.width() + shadowRadius + Math.abs(shadowX) + padding * 2);
        final int textHeight = (int) (textBounds.height() + shadowRadius + Math.abs(shadowY) + padding * 2);

        final Rect rect = new Rect(0, 0, textWidth, textHeight);

        final Canvas canvas = picture.beginRecording(textWidth, textHeight);

        canvas.drawRect(rect, bgPaint);

        canvas.save();

        canvas.drawText(
                text,
                -shadowRadius / 2f + shadowX - textBounds.left + padding,
                -shadowRadius / 2f + shadowY - textBounds.top + padding,
                paint
        );

        canvas.restore();

        picture.endRecording();

        return picture;
    }

    protected synchronized void loadBitmapCache(long pixelSize, boolean instantFullSizeLoad) {

        if (instantFullSizeLoad) {

            cachePixelSize = pixelSize;
            LoadPictureCacheTask loadFullSize = new LoadPictureCacheTask(config, true);
            try {
                loadFullSize.onPostExecute(loadFullSize.execute().get());
            } catch (InterruptedException ignored) {
            } catch (ExecutionException ignored) {
            }
        } else {

            if (pixelSize < CACHE_THRESHOLD) {
                pixelSize = CACHE_THRESHOLD;
            }

            if (pixelSize > screenHeight * screenWidth) {
                pixelSize = screenHeight * screenWidth;
            }

            if (cacheIsLoading) return;

            if (stickerPictureCache == null
                    || reinitializedAspect
                    || stickerPictureCache.isRecycled()
                    || (Math.abs(pixelSize * memoryScaleDown - stickerPictureCache.getWidth() * stickerPictureCache.getHeight()) >= CACHE_THRESHOLD)
                    ) {
                cacheIsLoading = true;
                cachePixelSize = pixelSize;
                new LoadPictureCacheTask(config, false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        }
    }

    protected synchronized void setStickerPictureCache(@Nullable Bitmap picture) {
        cacheIsLoading = false;
        if (picture != null) {

            this.stickerPictureCache = picture;
            setImageDimensions(picture.getWidth(), picture.getHeight());
            postInvalidate();
        }
    }

    protected synchronized void setImageDimensions(int w, int h) {

        float oldAspect = (this.stickerCacheWidth / (float) this.stickerCacheHeight);
        float newAspect = (w / (float) h);

        if (!isStickerImageInitialized) {

            reinitializedAspect = false;

            this.stickerCacheWidth = w;
            this.stickerCacheHeight = h;

            if (stickerCacheWidth >= stickerCacheHeight) {
                float minWidth = screenWidth / 8;
                if (stickerCacheWidth < minWidth) {
                    minScale = 1f;
                } else {
                    minScale = 1.0f * minWidth / stickerCacheWidth;
                }

                if (stickerCacheWidth > screenWidth) {
                    maxScale = 1;
                } else {
                    maxScale = 1.0f * screenWidth / stickerCacheWidth;
                }
            } else {

                float minHeight = screenWidth / 8;
                if (stickerCacheHeight < minHeight) {
                    minScale = 1f;
                } else {
                    minScale = 1.0f * minHeight / stickerCacheHeight;
                }

                if (stickerCacheHeight > screenWidth) {
                    maxScale = 1;
                } else {
                    maxScale = 1.0f * screenWidth / stickerCacheHeight;
                }
            }


            float initScale = (minScale + maxScale) / 2;

            currentX = ((getWidth() / 2) / imageScale - translationX);
            currentY = ((getHeight() / 2) / imageScale - translationY);
            currentScale = initScale / imageScale;
            currentRotation = 360 - calculateOnScreenRotation();

            if (calculateOnScreenFlip()) {
                flip(false);
            }

            isStickerImageInitialized = true;
            postInvalidate();
        } else if (reinitializedAspect && oldAspect != newAspect) {
            reinitializedAspect = false;

            double scale = this.stickerCacheHeight / (double) h;

            this.stickerCacheWidth = (int) (w * scale);

            postInvalidate();
        }

    }

    public void flip(boolean vertical) {
        if (vertical) {
            currentRotation = (currentRotation + 180) % 360;
        }
        isHorizonMirrored = !isHorizonMirrored;

        postInvalidate();
    }


    private void initButtonBitmaps() {

        resizeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_resize);
        resizeBitmapWidth = resizeBitmap.getWidth();
        resizeBitmapHeight = resizeBitmap.getHeight();


        delBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_delete);
        delBitmapWidth = delBitmap.getWidth();
        delBitmapHeight = delBitmap.getHeight();


    }

    @Override
    public boolean isEnabled() {
        View parent = (View) getParent();
        return (parent != null) && parent.isEnabled() && super.isEnabled();
    }

    protected boolean isInBitmap(@NonNull ScaledMotionEventWrapper event) {
        float[] arrayOfFloat1 = new float[9];
        getStickerMatrix().getValues(arrayOfFloat1);

        float f1 = 0.0F * arrayOfFloat1[0] + 0.0F * arrayOfFloat1[1] + arrayOfFloat1[2];
        float f2 = 0.0F * arrayOfFloat1[3] + 0.0F * arrayOfFloat1[4] + arrayOfFloat1[5];

        float f3 = arrayOfFloat1[0] * this.stickerCacheWidth + 0.0F * arrayOfFloat1[1] + arrayOfFloat1[2];
        float f4 = arrayOfFloat1[3] * this.stickerCacheWidth + 0.0F * arrayOfFloat1[4] + arrayOfFloat1[5];

        float f5 = 0.0F * arrayOfFloat1[0] + arrayOfFloat1[1] * this.stickerCacheHeight + arrayOfFloat1[2];
        float f6 = 0.0F * arrayOfFloat1[3] + arrayOfFloat1[4] * this.stickerCacheHeight + arrayOfFloat1[5];

        float f7 = arrayOfFloat1[0] * this.stickerCacheWidth + arrayOfFloat1[1] * this.stickerCacheHeight + arrayOfFloat1[2];
        float f8 = arrayOfFloat1[3] * this.stickerCacheWidth + arrayOfFloat1[4] * this.stickerCacheHeight + arrayOfFloat1[5];

        float[] arrayOfFloat2 = new float[4];
        float[] arrayOfFloat3 = new float[4];

        arrayOfFloat2[0] = f1;
        arrayOfFloat2[1] = f3;
        arrayOfFloat2[2] = f7;
        arrayOfFloat2[3] = f5;

        arrayOfFloat3[0] = f2;
        arrayOfFloat3[1] = f4;
        arrayOfFloat3[2] = f8;
        arrayOfFloat3[3] = f6;
        return pointInRect(arrayOfFloat2, arrayOfFloat3, event.getX(0), event.getY(0));
    }

    private boolean pointInRect(float[] xRange, float[] yRange, float x, float y) {

        double a1 = Math.hypot(xRange[0] - xRange[1], yRange[0] - yRange[1]);
        double a2 = Math.hypot(xRange[1] - xRange[2], yRange[1] - yRange[2]);
        double a3 = Math.hypot(xRange[3] - xRange[2], yRange[3] - yRange[2]);
        double a4 = Math.hypot(xRange[0] - xRange[3], yRange[0] - yRange[3]);

        double b1 = Math.hypot(x - xRange[0], y - yRange[0]);
        double b2 = Math.hypot(x - xRange[1], y - yRange[1]);
        double b3 = Math.hypot(x - xRange[2], y - yRange[2]);
        double b4 = Math.hypot(x - xRange[3], y - yRange[3]);

        double u1 = (a1 + b1 + b2) / 2;
        double u2 = (a2 + b2 + b3) / 2;
        double u3 = (a3 + b3 + b4) / 2;
        double u4 = (a4 + b4 + b1) / 2;


        double s = a1 * a2;

        double ss = Math.sqrt(u1 * (u1 - a1) * (u1 - b1) * (u1 - b2))
                + Math.sqrt(u2 * (u2 - a2) * (u2 - b2) * (u2 - b3))
                + Math.sqrt(u3 * (u3 - a3) * (u3 - b3) * (u3 - b4))
                + Math.sqrt(u4 * (u4 - a4) * (u4 - b4) * (u4 - b1));
        return Math.abs(s - ss) < 0.5;
    }


    protected boolean isOnResizeButton(@NonNull ScaledMotionEventWrapper event) {
        float left = -20 + this.dst_resize.left;
        float top = -20 + this.dst_resize.top;
        float right = 20 + this.dst_resize.right;
        float bottom = 20 + this.dst_resize.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }


    protected boolean isOnDelButton(@NonNull ScaledMotionEventWrapper event) {
        float left = -20 + this.del_resize.left;
        float top = -20 + this.del_resize.top;
        float right = 20 + this.del_resize.right;
        float bottom = 20 + this.del_resize.bottom;
        return event.getX(0) >= left && event.getX(0) <= right && event.getY(0) >= top && event.getY(0) <= bottom;
    }

    protected void setInEdit(boolean isInEdit) {
        this.isInEdit = isInEdit;

        invalidate();
    }

    public int getAllocatedByteCount() {
        return stickerPictureCache == null ? 0 : stickerPictureCache.getByteCount();
    }

    public long getRequestedByteCount() {
        return cachePixelSize * 4;
    }

    private class LoadPictureCacheTask extends AsyncTask<Void, Integer, Bitmap> {

        final Context context;

        final boolean isText;

        @Nullable
        final TextStickerConfig textConfig;
        @Nullable
        final ImageStickerConfig imageConfig;

        @Nullable
        final Picture textPicture;
        @Nullable
        final StickerHolderView parent;

        private LoadPictureCacheTask(@NonNull StickerConfigInterface config, boolean ignoreMemoryScale) {
            context = StickerView.this.getContext();

            isText = (config.getType() == StickerConfigInterface.STICKER_TYPE.TEXT);

            memoryScaleDown = ignoreMemoryScale ? 1f : holder.takeStickerMemory(StickerView.this);

            if (isText) {
                textConfig = (TextStickerConfig) config;
                imageConfig = null;
                textPicture = drawTextToPicture((TextStickerConfig) config);
            } else {
                imageConfig = (ImageStickerConfig) config;
                textConfig = null;
                textPicture = null;
            }

            if (getParent() instanceof StickerHolderView) {
                parent = (StickerHolderView) getParent();
            } else {
                parent = null;
            }
        }

        @Nullable
        @Override
        protected Bitmap doInBackground(Void... voids) {
            if (memoryScaleDown == 0) {
                return null;
            } else if (holder != null) {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

                long loadMaxSize = Math.round(cachePixelSize * memoryScaleDown);
                if (isText) {

                    final double aspect = textPicture.getWidth() / (double) textPicture.getHeight();
                    final int width = (int) Math.sqrt(loadMaxSize * aspect);
                    final int height = (int) Math.sqrt(loadMaxSize * (1 / aspect));


                    Bitmap canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(canvasBitmap);
                    Matrix matrix = new Matrix();
                    matrix.setScale(width / (float) textPicture.getWidth(), height / (float) textPicture.getHeight());
                    canvas.setMatrix(matrix);
                    textPicture.draw(canvas);

                    Bitmap bitmap = canvasBitmap;


                    if (bitmap.getWidth() * bitmap.getHeight() > loadMaxSize * 1.01f && width > 0 && height > 0) {
                        bitmap = Bitmap.createScaledBitmap(canvasBitmap, width, height, true);
                        canvasBitmap.recycle();
                    }

                    return bitmap;
                } else {
                    Bitmap bitmap;

                    float[] fullSize = BitmapFactoryUtils.decodeSize(context.getResources(), imageConfig.getStickerId());

                    final double aspect = fullSize[0] / (double) fullSize[1];
                    final int width = (int) Math.sqrt(loadMaxSize * aspect);
                    final int height = (int) Math.sqrt(loadMaxSize * (1 / aspect));

                    Bitmap orgBitmap = BitmapFactoryUtils.decodeResource(context.getResources(), imageConfig.getStickerId(), Math.round(Math.max(width, height)));
                    bitmap = orgBitmap;

                    if (bitmap.getWidth() * bitmap.getHeight() > loadMaxSize * 1.01f && width > 0 && height > 0) {
                        bitmap = Bitmap.createScaledBitmap(orgBitmap, width, height, true);
                        orgBitmap.recycle();
                    }


                    return bitmap;
                }
            } else return null;
        }

        protected void onPostExecute(@Nullable Bitmap result) {
            if (result != null) {
                setStickerPictureCache(result);
            }
            if (result == null || result.getByteCount() > cachePixelSize * memoryScaleDown * 3.9f) {
                loadBitmapCache();
            }

        }
    }

    /*public static class StickerPropertyModel implements Serializable {
        private static final long serialVersionUID = 3800737478616389410L;

        private ImageStickerConfig stickerConfig;

        private String text;

        private float xLocation;

        private float yLocation;

        private float degree;

        private float scaling;

        private int order;

        private int horizonMirror;

        public int getHorizonMirror() {
            return horizonMirror;
        }

        public void setHorizonMirror(int horizonMirror) {
            this.horizonMirror = horizonMirror;
        }


        public ImageStickerConfig getStickerConfig() {
            return stickerConfig;
        }

        public void setStickerConfig(ImageStickerConfig config) {
            this.stickerConfig = config;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public float getxLocation() {
            return xLocation;
        }

        public void setxLocation(float xLocation) {
            this.xLocation = xLocation;
        }

        public float getyLocation() {
            return yLocation;
        }

        public void setyLocation(float yLocation) {
            this.yLocation = yLocation;
        }

        public float getDegree() {
            return degree;
        }

        public void setDegree(float degree) {
            this.degree = degree;
        }

        public float getCurrentTransformState() {
            return scaling;
        }

        public void setScaling(float scaling) {
            this.scaling = scaling;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }*/
}
