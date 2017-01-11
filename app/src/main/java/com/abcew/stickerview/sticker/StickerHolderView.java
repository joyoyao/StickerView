package com.abcew.stickerview.sticker;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.abcew.stickerview.utils.ScaledMotionEventWrapper;

import java.util.ArrayList;

/**
 * Created by laputan on 2017/1/11.
 */

public class StickerHolderView extends RelativeLayout {


    @NonNull
    private final ArrayList<StickerView> stickerViews;

    @Nullable
    private StickerView currentStickerView;

    private float scale = 1;
    private float translationX = 0;
    private float translationY = 0;


    private OnStickerSelectionCallback callback;

    public StickerHolderView(Context context) {
        this(context, null);
    }

    public StickerHolderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerHolderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        stickerViews = new ArrayList<>();
    }

    /**
     * Redraw Sticker
     * @param config sticker that should be redraw.
     */
    public void refreshStickerView(@NonNull TextStickerConfig config) {
        for (StickerView sticker : stickerViews) {
            if (config.equals(sticker.getConfig())) {
                sticker.refresh();
            }
        }
    }

    /**
     * Add a Text Sticker
     * @param config sticker configuration.
     */
    public void addStickerView(@NonNull TextStickerConfig config) {
        if (config.getText().trim().length() != 0) {
            addStickerView((StickerConfigInterface) config, true);
        }
    }

    /**
     * Add a Image Sticker
     * @param config sticker configuration.
     */
    public void addStickerView(ImageStickerConfig config) {
        addStickerView((StickerConfigInterface) config, true);
    }

    /**
     * Add a Image or Text Sticker
     * @param config sticker configuration.
     */
    private void addStickerView(StickerConfigInterface config, boolean isNew) {
        final StickerView sticker = new StickerView(this.getContext(), config, this);

        sticker.setScale(scale);
        sticker.setTranslationX(translationX);
        sticker.setTranslationY(translationY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(sticker, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(sticker, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(sticker, "scaleY", 0f, 1f)
        );

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        this.addView(sticker, lp);
        stickerViews.add(sticker);

        animatorSet.start();

        setCurrentEdit(sticker, isNew);
    }


    long maxStickerMemory = Runtime.getRuntime().maxMemory() / 2;

    protected float takeStickerMemory(StickerView view) {
        long allocatedMemory = 0;
        long requestedMemory = 0;

        for (StickerView stickerView : stickerViews) {
            allocatedMemory += stickerView.getAllocatedByteCount();
            requestedMemory += stickerView.getRequestedByteCount();

        }

        //Scale to shrink all Bitmaps
        long newStickerMemory = (Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - allocatedMemory)) / 2;

        if (maxStickerMemory > newStickerMemory) {
            maxStickerMemory = newStickerMemory;
        }

        float reallocateScale = (float) (maxStickerMemory / (double) requestedMemory);

        reallocateScale = reallocateScale > 0f ? reallocateScale < 1f ? reallocateScale : 1f : 0f; //Range 0 - 1f

        for (StickerView stickerView : stickerViews) if (view != stickerView) {
            stickerView.rescaleCache(reallocateScale);
        }

        return reallocateScale;
    }


    @Override
    public void removeView(@NonNull final View view) {

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "alpha",  view.getAlpha(), 0f),
                ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 0f),
                ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 0f)
        );
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation)  {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                StickerHolderView.super.removeView(view);
            }
        });

        animatorSet.setDuration(500);

        animatorSet.start();
    }

    /**
     * Sets the amount that the view is scaled, as a proportion of
     * the stages unscaled size. A value of 1 means that no scaling is applied.
     * @param scale The scaling factor.
     */
    public void setScale(float scale) {
        this.scale = scale;
        for (StickerView sticker : stickerViews) {
            sticker.setScale(scale);
        }
    }

    /**
     * Sets the horizontal location of this view relative to its left position.
     * This effectively positions the object post-layout, in addition to wherever the object's
     * layout placed it.
     *
     * @param translationX The horizontal position of the stage relative to its left position,
     * in pixels.
     */
    @Override
    public void setTranslationX(float translationX) {
        this.translationX = translationX;
        for (StickerView sticker : stickerViews) {
            sticker.setTranslationX(translationX);
        }
    }

    /**
     * Sets the vertical location of this view relative to its top position.
     * This effectively positions the object post-layout, in addition to wherever the object's
     * layout placed it.
     *
     * @param translationY The vertical position of the stage relative to its top position,
     * in pixels.
     */
    @Override
    public void setTranslationY(float translationY) {
        this.translationY = translationY;
        for (StickerView sticker : stickerViews) {
            sticker.setTranslationY(translationY);
        }
    }

    @Override
    public float getTranslationX() {
        return translationX;
    }

    @Override
    public float getTranslationY() {
        return translationY;
    }

    /**
     * The amount that the stickers are scaled , as a proportion of
     * the stage unscaled size. A value of 1, the default, means that no scaling is applied.
     * @return scale The scaling factor.
     */
    public float getScale() {
        return scale;
    }

    public void deleteSticker() {
        if(currentStickerView != null) {
            stickerViews.remove(currentStickerView);
            StickerConfigInterface config = currentStickerView.getConfig();
            if (config != null) {
                switch (config.getType()) {
                    case IMAGE:
                        break;
                    case TEXT:
                        break;

                }
            }

            removeView(currentStickerView);

            setCurrentEdit(null, false);
        }
    }

    /**
     * Enable sticker interaction mode.
     * @param enable true to enter edit mode.
     */
    public void enableSelectableMode(boolean enable) {

        if (currentStickerView != null) {
            currentStickerView.setInEdit(false);
            currentStickerView = null;
        }

        setEnabled(enable);
    }

    public void leaveSticker() {
        /*if (currentStickerView != null) {
            currentStickerView.setInEdit(false);
            currentStickerView = null;
        }*/

        //currentStickerView = null;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View view = getChildAt(i);
            if (view instanceof StickerView) {
                ((StickerView) view).setInEdit(false);
            }
        }
    }

    private float startStickerX = 0;
    private float startStickerY = 0;
    private float startStickerScale = 0;
    private float startStickerRotation = 0;
    private boolean startWithFixedCenterPoint = false;

    @Override
    public boolean onTouchEvent(MotionEvent rawEvent) {

        if (isEnabled()) {

            Rect stickerDrawRegion = new Rect(0, 0, getWidth(), getHeight());

            ScaledMotionEventWrapper event = new ScaledMotionEventWrapper(rawEvent, scale, translationX, translationY);
            StickerView stickerInEditMode  = (currentStickerView != null && currentStickerView.isInEdit()) ? currentStickerView : null;

            if (stickerInEditMode == null) {
                for (StickerView stickerView : stickerViews) {
                    if (stickerView.isInEdit()) {
                        stickerInEditMode = stickerView;
                        currentStickerView = stickerInEditMode;
                    }
                }
            }

            if (event.hasClicked()) {
                for (int i = stickerViews.size() - 1; i >= 0; i--) {
                    StickerView stickerView = stickerViews.get(i);
                    if (stickerView.isInBitmap(event)) {
                        setCurrentEdit(stickerView, false);
                        return true;
                    }
                }
                setCurrentEdit(null, false);
                return true;
            }

            if (stickerInEditMode != null) {
                /*switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        //showIndicator(false);
                        break;
                }*/

                if (event.isCheckpoint()) {
                    float[] stickerTransform = stickerInEditMode.getCurrentTransformState();
                    startStickerX = stickerTransform[0];
                    startStickerY = stickerTransform[1];
                    startStickerScale = stickerTransform[2];
                    startStickerRotation = stickerTransform[3];
                    startWithFixedCenterPoint = stickerInEditMode.isOnResizeButton(event);
                    if (startWithFixedCenterPoint) {
                        event.setFixedCenterPoint(startStickerX, startStickerY);
                    }
                } else {

                    if (startWithFixedCenterPoint) {
                        event.setFixedCenterPoint(startStickerX, startStickerY);
                    }

                    ScaledMotionEventWrapper.TransformDiff diff = event.getTransformDifference();
                    float stickerX = startStickerX + diff.xDiff;
                    float stickerY = startStickerY + diff.yDiff;
                    float stickerScale = startStickerScale * diff.scale;
                    float stickerRotation = startStickerRotation + diff.angleDiff;

                    if (stickerDrawRegion.left > stickerX) {
                        startStickerX += stickerDrawRegion.left - stickerX;
                        stickerX = stickerDrawRegion.left;
                    }

                    if (stickerDrawRegion.right < stickerX) {
                        startStickerX += stickerDrawRegion.right - stickerX;
                        stickerX = stickerDrawRegion.right;
                    }

                    if (stickerDrawRegion.top > stickerY) {
                        startStickerY += stickerDrawRegion.top - stickerY;
                        stickerY = stickerDrawRegion.top;
                    }

                    if (stickerDrawRegion.bottom < stickerY) {
                        startStickerY += stickerDrawRegion.bottom - stickerY;
                        stickerY = stickerDrawRegion.bottom;
                    }

                    stickerInEditMode.setTransformation(stickerX, stickerY, stickerScale, stickerRotation);

                }

                invalidate();
                return true;
            }
            return true;
        } else {
            return super.onTouchEvent(rawEvent);
        }
    }

    public void flipSticker(boolean vertical){
        if (currentStickerView != null) {
            currentStickerView.flip(vertical);
        }
    }

    @Nullable
    public StickerConfigInterface getCurrentStickerConfig() {
        return currentStickerView != null ? currentStickerView.getConfig() : null;
    }

    public void bringStickerToFront() {
        if (currentStickerView != null) {
            int position = stickerViews.indexOf(currentStickerView);
            if (position == stickerViews.size() - 1) {
                return;
            }

            StickerConfigInterface config = currentStickerView.getConfig();
            if (config != null) {
                switch (config.getType()) {
                    case IMAGE:
                        break;
                    case TEXT:
                        break;
                }
            }

            StickerView stickerTemp = stickerViews.remove(position);
            stickerViews.add(stickerViews.size(), stickerTemp);
            currentStickerView.bringToFront();
        }
    }

    /*@Override
    public void onEdit(StickerView stickerView) {
        setCurrentEdit(stickerView, false);
    }*/

    @Nullable
    private synchronized StickerView setCurrentEdit(@Nullable StickerView stickerView, final boolean isNew) {

        if (stickerView == null) {
            leaveSticker();
            callback.onNoneStickerSelected();
        } else if (!stickerView.equals(currentStickerView) || !stickerView.isInEdit()) {
            leaveSticker();
            currentStickerView = stickerView;
            if (callback != null) {
                if (currentStickerView.getConfig().getType() == StickerConfigInterface.STICKER_TYPE.TEXT) {
                    callback.onTextStickerSelected((TextStickerConfig) currentStickerView.getConfig(), isNew);
                } else {
                    callback.onImageStickerSelected((ImageStickerConfig) currentStickerView.getConfig(), isNew);
                }
            }
            post(new Runnable() {
                @Override
                public void run() {
                    if (currentStickerView != null) {
                        currentStickerView.setInEdit(true);
                    }
                }
            });
        }
        return currentStickerView;
    }

    /**
     * Draw the result to a Bitmap.
     * @param bitmap result bitmap.
     * @param x --
     * @param y --
     * @return the result bitmap.
     */
    @NonNull
    public synchronized Bitmap drawToBitmap(@NonNull Bitmap bitmap, int x, int y) {
        if (bitmap != null) { //Declared as NonNull but it is sometimes null
            Canvas canvas = new Canvas(bitmap);

            for (StickerView sticker : stickerViews) {
                sticker.rescaleCache(0);
            }

            for (StickerView sticker : stickerViews) {
                sticker.drawStickerToCanvas(canvas, x, y);
            }
        }
        return bitmap;
    }

    /**
     * Set a callback for sticker selection.
     * @param callback Sticker selection callback.
     */
    public void setTextStickerSelectionCallback(OnStickerSelectionCallback callback){
        this.callback = callback;
    }

    /**
     * Text-Sticker selection callback.
     */
    public interface OnStickerSelectionCallback {
        /**
         * Sticker selected or deselected.
         * @param config Text-Sticker that are selected or null if not Text-Sticker are selected.
         */
        void onTextStickerSelected(TextStickerConfig config, boolean isNew);
        void onImageStickerSelected(ImageStickerConfig config, boolean isNew);
        void onNoneStickerSelected();
    }

}
