package com.abcew.stickerview.oldsticker;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

/**
 * 图片旋转
 */
public class StickerImage extends StickerItem {

    public StickerImage(Context context) {
        super(context);
    }


    public void init(Bitmap addBit, View parentView) {
        this.bitmap = addBit;
        this.srcRect = new Rect(0, 0, addBit.getWidth(), addBit.getHeight());
        int bitWidth = Math.min(addBit.getWidth(), parentView.getWidth());
        int bitHeight = (int) bitWidth * addBit.getHeight() / addBit.getWidth();
        int left = (parentView.getWidth() >> 1) - (bitWidth >> 1);
        int top = (parentView.getHeight() >> 1) - (bitHeight >> 1);
        this.dstRect = new RectF(left, top, left + bitWidth, top + bitHeight);
        this.matrix = new Matrix();
        this.matrix.postTranslate(this.dstRect.left, this.dstRect.top);
        this.matrix.postScale((float) bitWidth / addBit.getWidth(),
                (float) bitHeight / addBit.getHeight(), this.dstRect.left,
                this.dstRect.top);
        initWidth = this.dstRect.width();// 记录原始宽度
        // item.matrix.setScale((float)bitWidth/addBit.getWidthelpBox(),
        // (float)bitHeight/addBit.getHeight());
        this.isDrawHelpTool = true;
        this.helpBox = new RectF(this.dstRect);
        updateHelpBoxRect();

        helpToolsRect = new Rect(0, 0, deleteBit.getWidth(),
                deleteBit.getHeight());

        deleteRect = new RectF(helpBox.left - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH, helpBox.left + BUTTON_WIDTH, helpBox.top
                + BUTTON_WIDTH);
        rotateRect = new RectF(helpBox.right - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH, helpBox.right + BUTTON_WIDTH, helpBox.bottom
                + BUTTON_WIDTH);

        detectRotateRect = new RectF(rotateRect);
        detectDeleteRect = new RectF(deleteRect);
    }


    public void init(Bitmap addBit, View parentView, int positionX, int positionY) {
        this.bitmap = addBit;
        this.srcRect = new Rect(0, 0, addBit.getWidth(), addBit.getHeight());
        int bitWidth = Math.min(addBit.getWidth(), parentView.getWidth());
        int bitHeight = (int) bitWidth * addBit.getHeight() / addBit.getWidth();
        int left =positionX;
        int top = positionY;
        this.dstRect = new RectF(left, top, left + bitWidth, top + bitHeight);
        this.matrix = new Matrix();
        this.matrix.postTranslate(this.dstRect.left, this.dstRect.top);
        this.matrix.postScale((float) bitWidth / addBit.getWidth(),
                (float) bitHeight / addBit.getHeight(), this.dstRect.left,
                this.dstRect.top);
        initWidth = this.dstRect.width();// 记录原始宽度
        // item.matrix.setScale((float)bitWidth/addBit.getWidthelpBox(),
        // (float)bitHeight/addBit.getHeight());
        this.isDrawHelpTool = true;
        this.helpBox = new RectF(this.dstRect);
        updateHelpBoxRect();

        helpToolsRect = new Rect(0, 0, deleteBit.getWidth(),
                deleteBit.getHeight());

        deleteRect = new RectF(helpBox.left - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH, helpBox.left + BUTTON_WIDTH, helpBox.top
                + BUTTON_WIDTH);
        rotateRect = new RectF(helpBox.right - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH, helpBox.right + BUTTON_WIDTH, helpBox.bottom
                + BUTTON_WIDTH);

        detectRotateRect = new RectF(rotateRect);
        detectDeleteRect = new RectF(deleteRect);
    }


    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.bitmap, this.matrix, null);// 贴图元素绘制
        // canvas.drawRect(this.dstRect, dstPaint);
        if (this.isDrawHelpTool) {// 绘制辅助工具线
            canvas.save();
            canvas.rotate(roatetAngle, helpBox.centerX(), helpBox.centerY());
//            canvas.drawRoundRect(helpBox, 10, 10, helpBoxPaint);
            canvas.drawRect(helpBox, helpBoxPaint);
            // 绘制工具按钮
            canvas.drawBitmap(deleteBit, helpToolsRect, deleteRect, null);
            canvas.drawBitmap(rotateBit, helpToolsRect, rotateRect, null);
            canvas.restore();

            // canvas.drawRect(deleteRect, dstPaint);
            // canvas.drawRect(rotateRect, dstPaint);
            // canvas.drawRect(detectRotateRect, this.greenPaint);
            // canvas.drawRect(detectDeleteRect, this.greenPaint);
        }// end if

        // detectRotateRect
    }


}// end class
