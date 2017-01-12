package com.abcew.stickerview.sticker;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;

import com.abcew.stickerview.utils.BitmapFactoryUtils;

/**
 * Created by laputan on 2017/1/11.
 */

public class ImageStickerConfig implements StickerConfigInterface{

    private final @DrawableRes
    @RawRes
    int stickerId;

    private final boolean isSvg;




    public ImageStickerConfig(@StringRes int name, @DrawableRes @RawRes int drawableId, @DrawableRes @RawRes int stickerId) {

        this.stickerId  = stickerId;
        this.isSvg      = BitmapFactoryUtils.checkIsSvgResource(stickerId);
    }

    /**
     * Get sticker drawable resource it, it can be a drawable or raw
     * @return the drawable resource id;
     */
    public @DrawableRes @RawRes int getStickerId() {
        return stickerId;
    }

    /*
     * Check if the Sticker is a SVG sticker
     * @return true if it is a SVG sticker
     */
    /*public boolean isSvg() {
        return isSvg;
    }*/

    @NonNull
    @Override
    public STICKER_TYPE getType() {
        return STICKER_TYPE.IMAGE;
    }


}
