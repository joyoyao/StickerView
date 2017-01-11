package com.abcew.stickerview.sticker;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

/**
 * Created by laputan on 2017/1/11.
 */

public class TextStickerConfig implements StickerConfigInterface {

    private String text;
    private FontConfigInterface font;
    private int color;
    private int backgroundColor;
    private Paint.Align align;

    private final String identifierId = UUID.randomUUID().toString();

    public TextStickerConfig(String name, Paint.Align align, FontConfigInterface font, int color, int backgroundColor) {
        this.text = name;
        this.color = color;
        this.font = font;
        this.backgroundColor = backgroundColor;
        this.align = align;
    }

    @NonNull
    @Override
    public STICKER_TYPE getType() {
        return STICKER_TYPE.TEXT;
    }

    @Override
    public int getStickerId() {
        return -1;
    }


    /**
     * Get the Text-Sticker Text
     * @return text string
     */
    public String getText() {
        return text;
    }

    /**
     * Get the Text-Sticker Font
     * @return font config model
     */
    public FontConfigInterface getFont() {
        return font;
    }

    /**
     * Get the text align of the Text-Sticker
     * @deprecated is not implemented
     * @return align
     */
    public Paint.Align getAlign() {
        return align;
    }

    /**
     * Get the text align
     * @deprecated is not implemented
     * @param align the align of the Text-Sticker
     */
    public void setAlign(Paint.Align align) {
        this.align = align;
    }

    /**
     * Get the font Typeface
     * @return the Typeface
     */
    @Nullable
    public Typeface getTypeface() {
        if (font == null) {
            return null;
        }
        return font.getTypeface();
    }

    /**
     * Get the Foreground Color
     * @return 32bit rgba color value
     */
    public int getColor() {
        return color;
    }

    /**
     * Get the Background Color
     * @return 32bit rgba color value
     */
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Set Text-Sticker text
     * @param text text string
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Set Text-Sticker text and align
     * @deprecated not implemented
     * @param text text string
     * @param align text align
     */
    public void setText(String text, Paint.Align align) {
        this.text = text;
        this.align = align;
    }

    /**
     * Set the Text-Sticker font
     * @param font font config object
     */
    public void setFont(FontConfigInterface font) {
        this.font = font;
    }

    /**
     * Set foreground Color
     * @param color 32bit rgba color value
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Set background Color
     * @param color 32bit rgba color value
     */
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextStickerConfig that = (TextStickerConfig) o;

        return identifierId.equals(that.identifierId);

    }

    @Override
    public int hashCode() {
        return identifierId.hashCode();
    }






    @NonNull
    @Override
    public String toString() {
        return "TextStickerConfig{" +
                "text='" + text + '\'' +
                ", font=" + font +
                ", color=" + color +
                ", backgroundColor=" + backgroundColor +
                ", align=" + align +
                ", identifierId='" + identifierId + '\'' +
                '}';
    }
}
