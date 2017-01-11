package com.abcew.stickerview.oldsticker;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;

import com.abcew.stickerview.R;
import com.abcew.stickerview.utils.LocalDisplay;


/**
 * 图片旋转item
 */
public abstract class StickerItem {
    public static final int BUTTON_WIDTH = LocalDisplay.dp2px(13);
    protected static final float MIN_SCALE = 0.15f;
    private static final int HELP_BOX_PAD = 25;
    protected static Bitmap deleteBit;
    protected static Bitmap rotateBit;
    public Bitmap bitmap;
    public Rect srcRect;// 原始图片坐标
    public RectF dstRect;// 绘制目标坐标
    public RectF deleteRect;// 删除按钮位置
    public RectF rotateRect;// 旋转按钮位置
    public Matrix matrix;// 变化矩阵
    public boolean isDrawHelpTool = false;
    public RectF detectRotateRect;
    public RectF detectDeleteRect;
    public int hashCode;
    protected Rect helpToolsRect;
    protected float roatetAngle = 0;
    protected Paint helpBoxPaint = new Paint();
    protected float initWidth;// 加入屏幕时原始宽度
    RectF helpBox;
    private String stickerId;
    private Paint dstPaint = new Paint();
    private Paint paint = new Paint();
    private Paint greenPaint = new Paint();

    public StickerItem(Context context) {

        helpBoxPaint.setColor(Color.WHITE);
        helpBoxPaint.setStyle(Style.STROKE);
        helpBoxPaint.setAntiAlias(true);
        helpBoxPaint.setStrokeWidth(4);
        PathEffect effects = new DashPathEffect(new float[]{12, 12,}, 1);
        helpBoxPaint.setPathEffect(effects);
        dstPaint = new Paint();
        dstPaint.setColor(Color.RED);
        dstPaint.setAlpha(120);
        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setAlpha(120);
        // 导入工具按钮位图
        if (deleteBit == null) {
            deleteBit = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sticker_delete);
        }// end if
        if (rotateBit == null) {
            rotateBit = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sticker_resize);
        }// end if
    }

    /**
     * 缩放指定矩形
     *
     * @param rect
     * @param scale
     */
    protected static void scaleRect(RectF rect, float scale) {
        float w = rect.width();
        float h = rect.height();

        float newW = scale * w;
        float newH = scale * h;

        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;

        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    //    /**
//     * 缩放指定矩形
//     *
//     * @param rect
//     * @param scale
//     */
    public static void scaleRect(RectF rect, float widthscale, float heightscale) {
        float w = rect.width();
        float h = rect.height();

        float newW = widthscale * w;
        float newH = heightscale * h;

        float dx = (newW - w) / 2;
        float dy = (newH - h) / 2;

        rect.left -= dx;
        rect.top -= dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    /**
     * 矩形绕指定点旋转
     *
     * @param rect
     * @param roatetAngle
     */
    protected static void rotateRect(RectF rect, float center_x, float center_y,
                                     float roatetAngle) {
        float x = rect.centerX();
        float y = rect.centerY();
        float sinA = (float) Math.sin(Math.toRadians(roatetAngle));
        float cosA = (float) Math.cos(Math.toRadians(roatetAngle));
        float newX = center_x + (x - center_x) * cosA - (y - center_y) * sinA;
        float newY = center_y + (y - center_y) * cosA + (x - center_x) * sinA;
        float dx = newX - x;
        float dy = newY - y;
        rect.offset(dx, dy);

        // float w = rect.width();
        // float h = rect.height();
        // rect.left = newX;
        // rect.top = newY;
        // rect.right = newX + w;
        // rect.bottom = newY + h;
    }

    protected void updateHelpBoxRect() {
        this.helpBox.left -= HELP_BOX_PAD;
        this.helpBox.right += HELP_BOX_PAD;
        this.helpBox.top -= HELP_BOX_PAD;
        this.helpBox.bottom += HELP_BOX_PAD;
    }

    /**
     * 位置更新
     *
     * @param dx
     * @param dy
     */
    public void updatePos(final float dx, final float dy) {
        this.matrix.postTranslate(dx, dy);// 记录到矩阵中
        dstRect.offset(dx, dy);
        // 工具按钮随之移动
        helpBox.offset(dx, dy);
        deleteRect.offset(dx, dy);
        rotateRect.offset(dx, dy);
        this.detectRotateRect.offset(dx, dy);
        this.detectDeleteRect.offset(dx, dy);
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
//
//    public static void scaleRect(RectF rect, float newW, float newH) {
//        float w = rect.width();
//        float h = rect.height();
//        float dx = (newW - w) / 2;
//        float dy = (newH - h) / 2;
//        rect.left -= dx;
//        rect.top -= dy;
//        rect.right += dx;
//        rect.bottom += dy;
//    }

    public abstract void draw(Canvas canvas);

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }
}// end class
