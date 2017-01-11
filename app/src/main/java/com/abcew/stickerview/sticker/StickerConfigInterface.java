package com.abcew.stickerview.sticker;

/**
 * Created by laputan on 2017/1/11.
 */

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;

public   interface StickerConfigInterface {

    /**
     * Type of sticker.
     */
    enum STICKER_TYPE {
        IMAGE,
        TEXT
    }




    /**
     * Return the type of the sticker
     * @return sticker typ
     * @see STICKER_TYPE
     */
    @Nullable
    STICKER_TYPE getType();


    public int width = 0;

    public int height = 0;
        /*
         * Check if the Sticker is a SVG sticker
         * @return true if it is a SVG sticker
         */
    //boolean isSvg();


    /**
     * Get sticker drawable resource id.
     * @return
     */
    @DrawableRes
    @RawRes
    int getStickerId();
}
