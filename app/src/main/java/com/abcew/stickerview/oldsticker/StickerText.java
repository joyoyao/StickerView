package com.abcew.stickerview.oldsticker;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.View;

import com.abcew.stickerview.utils.LocalDisplay;


/**
 * 图片旋转
 */
public class StickerText extends StickerItem {


    protected float mMinTextSize = 14f;
    TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    TextPaint paintBorder = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    float scalew = 1, scaleh = 1;
    int lastBitWidth, lastBitHeight;
    private String text;
    private int textSize = 90;
    private int color = Color.WHITE;
    private int colorBorder = Color.BLACK;
    private int viewWidth;
    private int viewHeight;

    public StickerText(Context context) {
        super(context);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.bitmap, this.matrix, null);// 贴图元素绘制
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));
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
    }


    public void init(String text, View parentView) {
        this.text = text;
        this.viewWidth = parentView.getWidth();
        this.viewHeight = parentView.getHeight();
        regenerateBitmap();


    }

    /**
     * 旋转 缩放 更新
     *
     * @param dx
     * @param dy
     */
    public void updateRotateAndScale(final float oldx, final float oldy,
                                     final float dx, final float dy) {
        float c_x = dstRect.centerX();
        float c_y = dstRect.centerY();

        float x = this.detectRotateRect.centerX();
        float y = this.detectRotateRect.centerY();

        // float x = oldx;
        // float y = oldy;

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        // System.out.println("srcLen--->" + srcLen + "   curLen---->" +
        // curLen);

        float scale = curLen / srcLen;// 计算缩放比

        float newWidth = dstRect.width() * scale;
        if (newWidth / initWidth < MIN_SCALE) {// 最小缩放值检测
            return;
        }
        this.matrix.postScale(scale, scale, this.dstRect.centerX(),
                this.dstRect.centerY());// 存入scale矩阵
        // this.matrix.postRotate(5, this.dstRect.centerX(),
        // this.dstRect.centerY());
        scaleRect(this.dstRect, scale);// 缩放目标矩形

        // 重新计算工具箱坐标
        helpBox.set(dstRect);
        updateHelpBoxRect();// 重新计算
        rotateRect.offsetTo(helpBox.right - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH);
        deleteRect.offsetTo(helpBox.left - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH);

        detectRotateRect.offsetTo(helpBox.right - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH);
        detectDeleteRect.offsetTo(helpBox.left - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH);

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        // System.out.println("angle--->" + angle);

        // 拉普拉斯定理
        float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        // System.out.println("angle--->" + angle);
        roatetAngle += angle;
        this.matrix.postRotate(angle, this.dstRect.centerX(),
                this.dstRect.centerY());

        rotateRect(this.detectRotateRect, this.dstRect.centerX(),
                this.dstRect.centerY(), roatetAngle);
        rotateRect(this.detectDeleteRect, this.dstRect.centerX(),
                this.dstRect.centerY(), roatetAngle);
        // System.out.println("angle----->" + angle + "   " + flag);

        // System.out
        // .println(srcLen + "     " + curLen + "    scale--->" + scale);

    }


    /**
     * 绘画出字体
     */
    public void regenerateBitmap() {
        paint.setAntiAlias(true);
        textSize= LocalDisplay.dp2px(64);
        paint.setTextSize(textSize);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setDither(true);
        paint.setSubpixelText(true);
        paint.setFlags(Paint.SUBPIXEL_TEXT_FLAG);
        paint.setHinting(Paint.HINTING_ON);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setFakeBoldText(true);
        paint.setFilterBitmap(true);
        paintBorder.setTextSize(textSize);
        paintBorder.setColor(colorBorder);
        paintBorder.setStrokeWidth(LocalDisplay.dp2px(12));
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setDither(true);
        paintBorder.setSubpixelText(true);
        paintBorder.setFlags(Paint.SUBPIXEL_TEXT_FLAG);
        paintBorder.setHinting(Paint.HINTING_ON);
        paintBorder.setStrokeCap(Paint.Cap.ROUND);
        paintBorder.setStrokeJoin(Paint.Join.ROUND);
        paintBorder.setFakeBoldText(true);
        paintBorder.setAntiAlias(true);
        paintBorder.setFilterBitmap(true);
        String lines[] = text.split("\n");
        int textWidth = 0;
        for (String str : lines) {
            int temp = (int) paint.measureText(str);
            if (temp > textWidth)
                textWidth = temp;
        }
        if (textWidth < 1)
            textWidth = 1;
        if (this.bitmap != null && !this.bitmap.isRecycled())
            this.bitmap.recycle();
        this.bitmap = Bitmap.createBitmap(textWidth+LocalDisplay.dp2px(8), textSize * (lines.length) + LocalDisplay.sp2px(18),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);

        for (int i = 1; i <= lines.length; i++) {
            canvas.drawText(lines[i - 1], LocalDisplay.dp2px(2), i * textSize, paintBorder);
        }
        for (int i = 1; i <= lines.length; i++) {
            canvas.drawText(lines[i - 1], LocalDisplay.dp2px(2), i * textSize, paint);
        }
        this.srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        int bitWidth = Math.min(bitmap.getWidth(), viewWidth >> 1);
        int bitHeight = (int) bitWidth * bitmap.getHeight() / bitmap.getWidth();

        roatetAngle=0;
//        if (lastBitWidth != 0) {
//            scalew = (float) bitWidth / lastBitWidth;
//            scaleh = (float) bitHeight / lastBitHeight;
//        }
        scalew=1;
        scaleh=1;
        lastBitWidth = bitmap.getWidth();
        lastBitHeight = bitmap.getHeight();
        int left = (viewWidth >> 1) - (lastBitWidth >> 1);
        int top = (viewHeight >> 1) - (lastBitHeight >> 1);
//        if (this.dstRect == null) {
            this.dstRect = new RectF(left, top, left + lastBitWidth, top + lastBitHeight);
//        } else {
//            scaleRect(dstRect, scalew, scaleh);
//        }
//        if (this.matrix == null) {
            this.matrix = new Matrix();
            this.matrix.postTranslate(this.dstRect.left, this.dstRect.top);
//            this.matrix.postScale((float) bitWidth / bitmap.getWidth(),
//                    (float) bitHeight / bitmap.getHeight(), this.dstRect.left,
//                    this.dstRect.top);
//        } else {
//            this.matrix.postScale(scalew, scaleh, this.dstRect.left,
//                    this.dstRect.top);// 存入scale矩阵
//        }

        initWidth = this.dstRect.width();// 记录原始宽度
        // item.matrix.setScale((float)bitWidth/addBit.getWidth(),
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * 设置属性值后，提交方法
     */
    public void commit() {
        regenerateBitmap();
    }
}// end class
